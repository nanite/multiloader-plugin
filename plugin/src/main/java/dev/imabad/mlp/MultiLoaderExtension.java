package dev.imabad.mlp;

import dev.imabad.mlp.aw2at.AccessWidenerToTransformerTask;
import dev.imabad.mlp.ext.*;
import dev.imabad.mlp.loaders.FabricLoader;
import dev.imabad.mlp.loaders.ForgeLoader;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.api.ModSettings;
import net.minecraftforge.gradle.userdev.UserDevExtension;
import net.minecraftforge.gradle.userdev.UserDevPlugin;
import net.minecraftforge.gradle.userdev.tasks.AccessTransformJar;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

import java.io.IOException;
import java.net.URISyntaxException;
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

    private final Project project;
    public abstract Property<MultiLoaderRoot> getRootOptions();

    public MultiLoaderExtension(Project project) {
        this.project = project;
    }

    public void root(Action<MultiLoaderRoot> action) {
        MultiLoaderRoot rootOptions = project.getObjects().newInstance(MultiLoaderRoot.class, project);
        action.execute(rootOptions);
        getRootOptions().set(rootOptions);
        getRootOptions().finalizeValue();
        project.getTasks().register("aw2at", AccessWidenerToTransformerTask.class);
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
        if(multiLoaderRoot.splitSources.get()) {
            loom.getAccessWidenerPath().set(multiLoaderRoot.accessWidenerFile.get());
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
