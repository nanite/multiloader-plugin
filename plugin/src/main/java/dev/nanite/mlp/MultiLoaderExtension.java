package dev.nanite.mlp;

import dev.nanite.mlp.aw2at.AW2AT;
import dev.nanite.mlp.ext.*;
import dev.nanite.mlp.loaders.FabricLoader;
import dev.nanite.mlp.loaders.NeoLoader;
import dev.nanite.mlp.tasks.SingleOutputJar;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.api.ModSettings;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.flow.FlowProviders;
import org.gradle.api.flow.FlowScope;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.IOException;
import java.nio.file.Path;
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

        if(rootOptions.singleOutputJar.get()) {
            SingleOutputJar singleOutputjar = project.getTasks().create("singleOutputJar", SingleOutputJar.class);
            singleOutputjar.setGroup("mlp");
            project.project("forge").afterEvaluate((a) -> {
                singleOutputjar.dependsOn(project.project("fabric").getTasks().getByName("remapJar"));
                singleOutputjar.dependsOn(project.project("forge").getTasks().getByName("jar"));
            });
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

        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

        if(multiLoaderRoot.splitSources.get()) {
            loom.splitEnvironmentSourceSets();
            ModSettings modSettings = loom.getMods().maybeCreate(multiLoaderRoot.modID.get());
            modSettings.sourceSet(sourceSets.getByName("main"));
            modSettings.sourceSet(sourceSets.getByName("client"));

            Configuration commonClientJava = this.project.getConfigurations().maybeCreate("commonClientJava");
            commonClientJava.setCanBeResolved(false);
            commonClientJava.setCanBeConsumed(true);
            Configuration commonClientResources = this.project.getConfigurations().maybeCreate("commonClientResources");
            commonClientResources.setCanBeResolved(false);
            commonClientResources.setCanBeConsumed(true);

            SourceSet client = sourceSets.getByName("client");
            this.project.getArtifacts().add("commonClientJava", client.getJava().getSourceDirectories().getSingleFile());
            this.project.getArtifacts().add("commonClientResources", client.getResources().getSourceDirectories().getSingleFile());
        }
        multiLoaderRoot.commonProjectName.set(this.project.getName());

        Configuration commonJava = this.project.getConfigurations().maybeCreate("commonJava");
        commonJava.setCanBeResolved(false);
        commonJava.setCanBeConsumed(true);
        Configuration commonResources = this.project.getConfigurations().maybeCreate("commonResources");
        commonResources.setCanBeResolved(false);
        commonResources.setCanBeConsumed(true);

        SourceSet main = sourceSets.getByName("main");
        this.project.getArtifacts().add("commonJava", main.getJava().getSourceDirectories().getSingleFile());
        this.project.getArtifacts().add("commonResources", main.getResources().getSourceDirectories().getSingleFile());
    }

    public void fabric(Action<MultiLoaderFabric> action){
        MultiLoaderFabric multiLoaderFabric = project.getObjects().newInstance(MultiLoaderFabric.class, project);
        action.execute(multiLoaderFabric);
        FabricLoader.setupFabric(project, multiLoaderFabric);
    }


    public void neo(Action<MultiLoaderNeo> action) {
        MultiLoaderNeo multiLoaderNeo = project.getObjects().newInstance(MultiLoaderNeo.class, project);
        MultiLoaderRoot rootExtension = getRootExtension(project).getRootOptions().get();
        if(rootExtension.isNeoATEnabled()) {
            try {
                Path baseAW = rootExtension.accessWidenerFile.get().toPath();
                Path forgeAW = project.file(NeoLoader.ACCESS_TRANSFORMER_PATH).toPath();
                AW2AT.runConverter(baseAW, forgeAW);
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        action.execute(multiLoaderNeo);
        NeoLoader.setupNeo(project, multiLoaderNeo);
    }
}
