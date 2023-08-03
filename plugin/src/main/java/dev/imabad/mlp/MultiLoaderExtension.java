package dev.imabad.mlp;

import dev.imabad.mlp.ext.*;
import dev.imabad.mlp.loaders.FabricLoader;
import dev.imabad.mlp.loaders.ForgeLoader;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.minecraftforge.gradle.userdev.UserDevExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;

import java.util.HashMap;

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

    private Project project;
    public abstract Property<MultiLoaderRoot> getRootOptions();

    public MultiLoaderExtension(Project project) {
        this.project = project;
    }

    public void root(Action<MultiLoaderRoot> action) {
        MultiLoaderRoot rootOptions = project.getObjects().newInstance(MultiLoaderRoot.class, project);
        action.execute(rootOptions);
        getRootOptions().set(rootOptions);
        getRootOptions().finalizeValue();
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
    }
}
