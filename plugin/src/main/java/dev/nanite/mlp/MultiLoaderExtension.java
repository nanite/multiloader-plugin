package dev.nanite.mlp;

import dev.nanite.mlp.aw2at.AccessWidenerToTransformerTask;
import dev.nanite.mlp.ext.DataGenOptions;
import dev.nanite.mlp.ext.MultiLoaderFabric;
import dev.nanite.mlp.ext.MultiLoaderForge;
import dev.nanite.mlp.loaders.FabricLoader;
import dev.nanite.mlp.loaders.ForgeLoader;
import dev.nanite.mlp.tasks.SingleOutputJar;
import dev.nanite.mlp.ext.MultiLoaderRoot;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.api.ModSettings;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.*;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.flow.FlowProviders;
import org.gradle.api.flow.FlowScope;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.inject.Inject;

public abstract class MultiLoaderExtension {

    public static MultiLoaderExtension getExtension(Project project){
        return project.getExtensions().getByType(MultiLoaderExtension.class);
    }

    public static MultiLoaderExtension getRootExtension(Project project){
        return getExtension(project.getRootProject());
    }

    public static Project getCommonProject(Project project, MultiLoaderRoot extension){
        return project.getRootProject().project(extension.commonProjectName.get());
    }

    private final Project project;
    public abstract Property<MultiLoaderRoot> getRootOptions();
    @Inject
    protected abstract FlowScope getFlowScope();
    @Inject
    protected abstract FlowProviders getFlowProviders();


    public MultiLoaderExtension(Project project) {
        this.project = project;
    }

    public void root(Action<MultiLoaderRoot> action) {
        MultiLoaderRoot rootOptions = project.getObjects().newInstance(MultiLoaderRoot.class, project);
        action.execute(rootOptions);
        if(!rootOptions.commonMixin.isPresent()){
            rootOptions.commonMixin.set(rootOptions.modID.get() + ".mixins.json");
        }
        getRootOptions().set(rootOptions);
        getRootOptions().finalizeValue();
         project.getTasks().register("aw2at", AccessWidenerToTransformerTask.class);

        if(rootOptions.singleOutputJar.get()) {
            SingleOutputJar singleOutputjar = project.getTasks().create("singleOutputJar", SingleOutputJar.class);
            singleOutputjar.setGroup("mlp");
            project.project("forge").afterEvaluate((a) -> {
                singleOutputjar.dependsOn(project.project("fabric").getTasks().getByName("remapJar"));
                singleOutputjar.dependsOn(project.project("forge").getTasks().getByName("jar"));
            });
        }
        if(rootOptions.getDataGenOptions().isPresent()){
            DataGenOptions dataGenOptions = rootOptions.getDataGenOptions().get();
            if(dataGenOptions.mixBoth.get() && (dataGenOptions.useFabric.isPresent()
                    || dataGenOptions.useForge.isPresent())){
                throw new GradleException("Data Gen options are mutually exclusive! You can either have mixBoth or have" +
                        " useFabric and useForge");
            }
        }
    }

    public void common() {
        this.project.getPlugins().apply("java");
        this.project.getPlugins().apply("fabric-loom");
        MultiLoaderRoot multiLoaderRoot = getRootExtension(project).getRootOptions().get();
        LoomGradleExtensionAPI loom = this.project.getExtensions().getByType(LoomGradleExtensionAPI.class);
        DependencyHandler deps = this.project.getDependencies();
        deps.add("minecraft", "com.mojang:minecraft:" + multiLoaderRoot.minecraftVersion.get());
        deps.add("mappings", loom.officialMojangMappings());
        deps.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, multiLoaderRoot.mixinString.get());
        if (multiLoaderRoot.accessWidenerFile.isPresent()) {
            loom.getAccessWidenerPath().set(multiLoaderRoot.accessWidenerFile.get());
        }
        if(multiLoaderRoot.splitSources.get()) {
            loom.splitEnvironmentSourceSets();
            ModSettings modSettings = loom.getMods().maybeCreate(multiLoaderRoot.modID.get());
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            modSettings.sourceSet(sourceSets.getByName("main"));
            modSettings.sourceSet(sourceSets.getByName("client"));
        }
        multiLoaderRoot.commonProjectName.set(this.project.getName());
    }

    public void fabric(Action<MultiLoaderFabric> action){
        MultiLoaderFabric multiLoaderFabric = project.getObjects().newInstance(MultiLoaderFabric.class, project);
        action.execute(multiLoaderFabric);
        FabricLoader.setupFabric(project, multiLoaderFabric);
    }

    public void forge(Action<MultiLoaderForge> action) {
        MultiLoaderForge multiLoaderForge = project.getObjects().newInstance(MultiLoaderForge.class, project);
        MultiLoaderRoot rootExtension = getRootExtension(project).getRootOptions().get();
        action.execute(multiLoaderForge);
        ForgeLoader.setupForge(project, multiLoaderForge);
        if(rootExtension.isForgeATEnabled()) {
            project.afterEvaluate(forgeProject -> {
                try {
                    AccessWidenerToTransformerTask.runTransformer(forgeProject.getRootProject());
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }
}
