package dev.imabad.mlp.loaders;

import dev.imabad.mlp.MultiLoaderExtension;
import dev.imabad.mlp.ext.MultiLoaderFabric;
import dev.imabad.mlp.ext.MultiLoaderRoot;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;

public class FabricLoader {

    public static void setupFabric(Project project, MultiLoaderFabric multiLoaderFabric){
        applyFabricPlugins(project);
        configureFabricDependencies(project, multiLoaderFabric);
        setupLoom(project);
        GenericLoader.genericGradleSetup(project);
    }

    public static void applyFabricPlugins(Project project){
        project.getPlugins().apply("java");
        project.getPlugins().apply("fabric-loom");
    }

    public static void configureFabricDependencies(Project project, MultiLoaderFabric multiLoaderFabric){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        DependencyHandler deps = project.getDependencies();
        deps.add("minecraft",
                "com.mojang:minecraft:" + multiLoaderRoot.minecraftVersion.get());
        deps.add("mappings", project.getExtensions()
                .getByType(LoomGradleExtensionAPI.class)
                .officialMojangMappings());
        deps.add("modImplementation",
                "net.fabricmc:fabric-loader:" + multiLoaderFabric.fabricLoaderVersion.get());
        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, MultiLoaderExtension.getCommonProject(project, multiLoaderRoot));
    }

    public static void setupLoom(Project project){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        LoomGradleExtensionAPI loomGradle = project.getExtensions()
                .getByType(LoomGradleExtensionAPI.class);
        RunConfigSettings clientRunConfig = loomGradle.getRuns()
                .findByName("client");
        if(clientRunConfig == null){
            clientRunConfig = loomGradle.getRuns().create("client");
        }
        clientRunConfig.client();
        clientRunConfig.setConfigName("Fabric Client");
        clientRunConfig.ideConfigGenerated(true);
        clientRunConfig.runDir("run");

        RunConfigSettings serverRunConfig = loomGradle.getRuns()
                .findByName("server");
        if(serverRunConfig == null){
            serverRunConfig = loomGradle.getRuns().create("server");
        }
        serverRunConfig.server();
        serverRunConfig.setConfigName("Fabric Server");
        serverRunConfig.ideConfigGenerated(true);
        serverRunConfig.runDir("run");

        Project commonProject = project.getRootProject().project(multiLoaderRoot.commonProjectName.get());
        String accessWidenerLoc = String.format(multiLoaderRoot.accessWidenerFile.get(), multiLoaderRoot.modID.get());
        if(commonProject.file(accessWidenerLoc).exists()){
            loomGradle.getAccessWidenerPath().set(commonProject.file(accessWidenerLoc));
        }
        String defaultRefMapName = String.format("%s.refmap.json", multiLoaderRoot.modID.get());;
        loomGradle.getMixin().getDefaultRefmapName().set(defaultRefMapName);
    }

}
