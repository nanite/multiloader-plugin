package dev.imabad.mlp.loaders;

import dev.imabad.mlp.MultiLoaderExtension;
import dev.imabad.mlp.ext.MultiLoaderForge;
import dev.imabad.mlp.ext.MultiLoaderRoot;
import net.minecraftforge.gradle.common.util.ModConfig;
import net.minecraftforge.gradle.common.util.RunConfig;
import net.minecraftforge.gradle.patcher.tasks.ReobfuscateJar;
import net.minecraftforge.gradle.userdev.UserDevExtension;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.jvm.tasks.Jar;

public class ForgeLoader {

    public static void setupForge(Project project, MultiLoaderForge multiLoaderForge){
        applyForgePlugins(project);
        configureForgeDependencies(project, multiLoaderForge);
        setupForgeGradle(project, multiLoaderForge);
        GenericLoader.genericGradleSetup(project);
        project.getTasks().getByName("jar").finalizedBy("reobfJar");
    }

    public static void applyForgePlugins(Project project){
        project.getPlugins().apply("java");
        project.getPlugins().apply("net.minecraftforge.gradle");
    }

    public static void configureForgeDependencies(Project project, MultiLoaderForge multiLoaderForge){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        var forgePath = multiLoaderForge.isNeo.get() ? "net.neoforged" : "net.minecraftforge";
        DependencyHandler deps = project.getDependencies();
        deps.add("minecraft",
                forgePath + ":forge:" + multiLoaderRoot.minecraftVersion.get() + "-" +
                        multiLoaderForge.forgeVersion.get());

        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
                project.getRootProject().project(multiLoaderRoot.commonProjectName.get()));
    }

    public static void setupForgeGradle(Project project, MultiLoaderForge multiLoaderForge){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        UserDevExtension forgeUserDev = project.getExtensions().getByType(UserDevExtension.class);
        forgeUserDev.mappings("official", multiLoaderRoot.minecraftVersion.get());
        NamedDomainObjectContainer<RunConfig> runs = forgeUserDev.getRuns();
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
        createOrConfigureRunConfig(project, runs, commonSourceSets, "Client");
        createOrConfigureRunConfig(project, runs, commonSourceSets, "Server");
        if(multiLoaderForge.useDataGen.get()){
            RunConfig dataConfig = createOrConfigureRunConfig(project, runs, commonSourceSets, "Data");
            dataConfig.args("--mod", multiLoaderRoot.modID.get(), "--all", "--output",
                    project.file("src/generated/resources"), "--existing", project.file("src/main/resources"));
        }
    }

    private static RunConfig createOrConfigureRunConfig(Project project, NamedDomainObjectContainer<RunConfig> runs,
                                                   SourceSetContainer commonSourceSets, String name){
        RunConfig runConfig = runs.findByName(name.toLowerCase());
        if(runConfig == null){
            runConfig = runs.create(name.toLowerCase());
        }
        runConfig.workingDirectory(project.file("run"));
        runConfig.ideaModule(project.getRootProject().getName() + "." + project.getName() + ".main");
        runConfig.taskName(name);
        runConfig.property("mixin.env.remapRefMap", "true");
        runConfig.property("mixin.env.refMapRemappingFile", project.getProjectDir() + "/build/createSrgToMcp/output.srg");
        NamedDomainObjectContainer<ModConfig> mods = runConfig.getMods();
        ModConfig modRun = mods.findByName("mod" + name + "Run");
        if(modRun == null){
            modRun = mods.create("mod" + name + "Run");
        }
        modRun.source(commonSourceSets.getByName("main"));
        modRun.source(project.getExtensions().getByType(SourceSetContainer.class).getByName("main"));
        return runConfig;
    }

}
