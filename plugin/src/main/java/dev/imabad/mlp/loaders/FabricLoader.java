package dev.imabad.mlp.loaders;

import dev.imabad.mlp.MultiLoaderExtension;
import dev.imabad.mlp.ext.MultiLoaderFabric;
import dev.imabad.mlp.ext.MultiLoaderRoot;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.api.ModSettings;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.ide.RunConfigSettings;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

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
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
                commonProject);
        if(multiLoaderRoot.splitSources.get()){
            LoomGradleExtensionAPI loomGradle = project.getExtensions()
                    .getByType(LoomGradleExtensionAPI.class);
            loomGradle.splitEnvironmentSourceSets();
            SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
            SourceSet clientSourceSet = commonSourceSets.getByName("client");
            deps.add(clientSourceSet.getImplementationConfigurationName(), clientSourceSet.getOutput());
        }
    }

    public static void setupLoom(Project project){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        LoomGradleExtensionAPI loomGradle = project.getExtensions()
                .getByType(LoomGradleExtensionAPI.class);
        RunConfigSettings clientRunConfig = loomGradle.getRuns()
                .maybeCreate("client");
        clientRunConfig.client();
        clientRunConfig.setConfigName("Fabric Client");
        clientRunConfig.ideConfigGenerated(true);
        clientRunConfig.runDir("run");

        RunConfigSettings serverRunConfig = loomGradle.getRuns()
                .maybeCreate("server");
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
        if(multiLoaderRoot.splitSources.get()){
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
            ModSettings modSettings = loomGradle.getMods().maybeCreate(multiLoaderRoot.modID.get());
            modSettings.sourceSet(sourceSets.getByName("main"));
            modSettings.sourceSet(sourceSets.getByName("client"));
            modSettings.sourceSet(commonSourceSets.getByName("main"));
            modSettings.sourceSet(commonSourceSets.getByName("client"));
        }
    }

}
