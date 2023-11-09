package dev.nanite.mlp.loaders;

import dev.nanite.mlp.MultiLoaderExtension;
import dev.nanite.mlp.aw2at.AccessWidenerToTransformerTask;
import dev.nanite.mlp.ext.MultiLoaderForge;
import dev.nanite.mlp.ext.MultiLoaderRoot;
import net.minecraftforge.gradle.common.util.ModConfig;
import net.minecraftforge.gradle.common.util.RunConfig;
import net.minecraftforge.gradle.userdev.UserDevExtension;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.spongepowered.asm.gradle.plugins.MixinExtension;

import java.util.Map;

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
        project.getPlugins().apply("org.spongepowered.mixin");
    }

    public static void configureForgeDependencies(Project project, MultiLoaderForge multiLoaderForge){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        if(multiLoaderForge.isNeo.get() && !multiLoaderRoot.minecraftVersion.get().equals("1.20.1")) {
            throw new IllegalStateException("Neo in forge is only supported on 1.20.1");
        }
        var forgePath = multiLoaderForge.isNeo.get() ? "net.neoforged" : "net.minecraftforge";
        if(multiLoaderRoot.overrideSpongeMixin.get()){
            project.getConfigurations().getByName("annotationProcessor")
                    .exclude(Map.of("group", "org.spongepowered", "module", "mixin"));
        }
        DependencyHandler deps = project.getDependencies();
        deps.add("minecraft",
                forgePath + ":forge:" + multiLoaderRoot.minecraftVersion.get() + "-" +
                        multiLoaderForge.forgeVersion.get());
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        deps.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
                commonProject);
        deps.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME,
                multiLoaderRoot.mixinString.get());
        if(multiLoaderRoot.splitSources.get()) {
            SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
            SourceSet clientSourceSet = commonSourceSets.getByName("client");
            deps.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, clientSourceSet.getOutput());
        }
    }

    public static void setupForgeGradle(Project project, MultiLoaderForge multiLoaderForge){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        UserDevExtension forgeUserDev = project.getExtensions().getByType(UserDevExtension.class);
        forgeUserDev.mappings("official", multiLoaderRoot.minecraftVersion.get());
        NamedDomainObjectContainer<RunConfig> runs = forgeUserDev.getRuns();
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
        SourceSetContainer projectSourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        createOrConfigureRunConfig(project, runs, commonSourceSets, commonProject, "Client", multiLoaderRoot.splitSources.get());
        createOrConfigureRunConfig(project, runs, commonSourceSets, commonProject, "Server", multiLoaderRoot.splitSources.get());
        if(multiLoaderRoot.getDataGenOptions().isPresent() &&
                (multiLoaderRoot.getDataGenOptions().get().useForge.isPresent() || multiLoaderRoot.getDataGenOptions().get().mixBoth.get())){
            RunConfig dataConfig = createOrConfigureRunConfig(project, runs, commonSourceSets,
                    commonProject, "Data", multiLoaderRoot.splitSources.get());
            dataConfig.args("--mod", multiLoaderRoot.modID.get(), "--all", "--output",
                    multiLoaderRoot.getDataGenOptions().get().useForge.get(), "--existing",
                    commonProject.file("src/main/resources"), "--existing",
                    project.file("src/main/resources"));
        }
        if(multiLoaderRoot.isForgeATEnabled()) {
            forgeUserDev.accessTransformer(project.file(AccessWidenerToTransformerTask.ACCESS_TRANSFORMER_PATH));
        }
        MixinExtension mixinExtension = project.getExtensions().getByType(MixinExtension.class);
        if(commonProject.file("src/main/resources/" + multiLoaderRoot.commonMixin.get()).exists()) {
            mixinExtension.config(multiLoaderRoot.commonMixin.get());
        }
        if(project.file("src/main/resources/" + multiLoaderForge.forgeMixins.get()).exists()) {
            mixinExtension.config(multiLoaderForge.forgeMixins.get());
        }
        mixinExtension.add(projectSourceSets.getByName("main"), multiLoaderRoot.modID.get() + ".refmap.json");
    }

    private static RunConfig createOrConfigureRunConfig(Project project, NamedDomainObjectContainer<RunConfig> runs,
                                                   SourceSetContainer commonSourceSets, Project commonProject, String name, boolean isSplitSources){
        RunConfig runConfig = runs.maybeCreate(name.toLowerCase());
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
        SourceSet forgeMain = project.getExtensions().getByType(SourceSetContainer.class).getByName("main");
        modRun.sources(commonSourceSets.getByName("main"), forgeMain);
        if(isSplitSources){
            SourceSet client = commonSourceSets.getByName("client");
            modRun.source(client);
            project.getTasks().maybeCreate("clientClasses")
                    .dependsOn(commonProject.getTasks().getByName("clientClasses"));
        }
        return runConfig;
    }

}
