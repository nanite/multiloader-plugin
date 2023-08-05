package dev.imabad.mlp;

import dev.imabad.mlp.aw2at.AccessWidenerToTransformerTask;
import dev.imabad.mlp.ext.*;
import dev.imabad.mlp.loaders.FabricLoader;
import dev.imabad.mlp.loaders.ForgeLoader;
import dev.imabad.mlp.tasks.SingleOutputJar;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.minecraftforge.gradle.userdev.UserDevExtension;
import net.minecraftforge.gradle.userdev.UserDevPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.*;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.flow.FlowProviders;
import org.gradle.api.flow.FlowScope;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

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
        getRootOptions().set(rootOptions);
        getRootOptions().finalizeValue();
        TaskProvider<AccessWidenerToTransformerTask> aw2t = project.getTasks().register("aw2at", AccessWidenerToTransformerTask.class);

        if(rootOptions.singleOutputJar.get()) {
            project.project("forge").afterEvaluate((a) -> {
                SingleOutputJar singleOutputjar = project.getTasks().create("singleOutputJar", SingleOutputJar.class);
                singleOutputjar.dependsOn(project.project("fabric").getTasks().getByName("remapJar"));
                singleOutputjar.dependsOn(project.project("forge").getTasks().getByName("jar"));
                singleOutputjar.setGroup("mlp");
            });
        }
        if(rootOptions.getDataGenOptions().isPresent()){
            DataGenOptions dataGenOptions = rootOptions.getDataGenOptions().get();
            if(dataGenOptions.useForge.get() && dataGenOptions.useFabric.get() ||
                    dataGenOptions.useFabric.get() && dataGenOptions.mixBoth.get() ||
                    dataGenOptions.useForge.get() && dataGenOptions.mixBoth.get()){
                throw new GradleException("Data Gen options are mutually exclusive!");
            }
        }
    }

    public void common() {
        this.project.getPlugins().apply("java");
        this.project.getPlugins().apply("fabric-loom");
        MultiLoaderRoot multiLoaderRoot = getRootExtension(project).getRootOptions().get();
        DependencyHandler deps = this.project.getDependencies();
        deps.add("minecraft",
                "com.mojang:minecraft:" + multiLoaderRoot.minecraftVersion.get());
        LoomGradleExtensionAPI loom = this.project.getExtensions()
                .getByType(LoomGradleExtensionAPI.class);
        deps.add("mappings", loom
                .officialMojangMappings());
        deps.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, multiLoaderRoot.mixinString.get());
        if(multiLoaderRoot.splitSources.get()) {
            loom.splitEnvironmentSourceSets();
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
        action.execute(multiLoaderForge);
        ForgeLoader.setupForge(project, multiLoaderForge);
        //Todo make this run more offten
        project.getTasks().named("build").configure(task -> {
            task.dependsOn(project.getRootProject().getTasks().named("aw2at"));
        });

    }
}
