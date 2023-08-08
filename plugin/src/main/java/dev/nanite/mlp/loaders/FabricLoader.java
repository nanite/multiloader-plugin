package dev.nanite.mlp.loaders;

import dev.nanite.mlp.MultiLoaderExtension;
import dev.nanite.mlp.ext.MultiLoaderFabric;
import dev.nanite.mlp.ext.MultiLoaderRoot;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.api.ModSettings;
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
        LoomGradleExtensionAPI loomGradle = project.getExtensions().getByType(LoomGradleExtensionAPI.class);
        deps.add("minecraft", "com.mojang:minecraft:" + multiLoaderRoot.minecraftVersion.get());
        deps.add("mappings", loomGradle.officialMojangMappings());
        deps.add("modImplementation", "net.fabricmc:fabric-loader:" + multiLoaderFabric.fabricLoaderVersion.get());
        if(multiLoaderFabric.fabricApiVersion.isPresent()) {
            deps.add("modImplementation", "net.fabricmc.fabric-api:fabric-api:" + multiLoaderFabric.fabricApiVersion.get());
        }
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, commonProject);
        if(multiLoaderRoot.splitSources.get()) {
            loomGradle.splitEnvironmentSourceSets();
            SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
            SourceSet clientSourceSet = commonSourceSets.getByName("client");
            deps.add(clientSourceSet.getImplementationConfigurationName(), clientSourceSet.getOutput());
        }
    }

    public static void setupLoom(Project project){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        Project commonProject = project.getRootProject().project(multiLoaderRoot.commonProjectName.get());
        LoomGradleExtensionAPI loomGradle = project.getExtensions().getByType(LoomGradleExtensionAPI.class);
        RunConfigSettings clientRunConfig = loomGradle.getRuns().maybeCreate("client");
        clientRunConfig.client();
        clientRunConfig.setConfigName("Fabric Client");
        clientRunConfig.ideConfigGenerated(true);
        clientRunConfig.runDir("run");

        RunConfigSettings serverRunConfig = loomGradle.getRuns().maybeCreate("server");
        serverRunConfig.server();
        serverRunConfig.setConfigName("Fabric Server");
        serverRunConfig.ideConfigGenerated(true);
        serverRunConfig.runDir("run");

        if(multiLoaderRoot.getDataGenOptions().isPresent() &&
                (multiLoaderRoot.getDataGenOptions().get().useFabric.isPresent() || multiLoaderRoot.getDataGenOptions().get().mixBoth.get())){
            RunConfigSettings dataGenRunConfig = loomGradle.getRuns()
                    .maybeCreate("datagenClient");
            dataGenRunConfig.inherit(clientRunConfig);
            dataGenRunConfig.setConfigName("Fabric Data Generation");
            dataGenRunConfig.vmArg("-Dfabric-api.datagen");
            dataGenRunConfig.vmArg("-Dfabric-api.datagen.output-dir=" + multiLoaderRoot.getDataGenOptions().get().useFabric.orElse(commonProject.file("src/generated/resources")));
            dataGenRunConfig.vmArg("-Dfabric-api.datagen.modid=" + multiLoaderRoot.modID.get());
            dataGenRunConfig.runDir("build/datagen");
        }

        if (multiLoaderRoot.accessWidenerFile.isPresent()) {
            loomGradle.getAccessWidenerPath().set(multiLoaderRoot.accessWidenerFile.get());
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
