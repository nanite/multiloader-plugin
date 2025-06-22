package dev.nanite.mlp.loaders;

import dev.nanite.mlp.MultiLoaderExtension;
import dev.nanite.mlp.ext.MultiLoaderNeo;
import dev.nanite.mlp.ext.MultiLoaderRoot;
import net.neoforged.moddevgradle.dsl.ModModel;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import net.neoforged.moddevgradle.dsl.RunModel;
import net.neoforged.moddevgradle.internal.ModDevArtifactsWorkflow;
import net.neoforged.moddevgradle.internal.ModDevPlugin;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public class NeoLoader {

    public static final String ACCESS_TRANSFORMER_PATH = "src/main/resources/META-INF/accesstransformer.cfg";

    private static final Logger LOGGER = LoggerFactory.getLogger(NeoLoader.class);

    public static void setupNeo(Project project, MultiLoaderNeo multiLoaderForge){
        applyNeoPlugins(project);
        configureNeoDependencies(project, multiLoaderForge);
        setupNeoGradle(project, multiLoaderForge);

//        GenericLoader.genericGradleSetup(project);
    }

    public static void applyNeoPlugins(Project project){
        project.getPlugins().apply(ModDevPlugin.class);
    }

    public static void configureNeoDependencies(Project project, MultiLoaderNeo neoLoader) {
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        DependencyHandler deps = project.getDependencies();
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);

        Configuration commonJava = project.getConfigurations().maybeCreate("commonJava");
        commonJava.setCanBeResolved(true);
        project.getConfigurations().add(commonJava);
        Configuration commonResources = project.getConfigurations().maybeCreate("commonResources");
        commonResources.setCanBeResolved(true);
        project.getConfigurations().add(commonResources);

        project.getTasks().named("compileJava", JavaCompile.class).configure(task -> {
            task.dependsOn(commonJava);
            task.source(commonJava);
        });

        project.getTasks().named("processResources", ProcessResources.class).configure(task -> {
            task.dependsOn(commonResources);
            task.from(commonResources);
        });

        ModuleDependency commonDep = (ModuleDependency) deps.add("compileOnly", commonProject);
        commonDep.capabilities(capabilities -> capabilities.requireCapability(multiLoaderRoot.group.get() + ":common"));

        deps.add("commonJava", project.getDependencies().project(Map.of("path", ":common", "configuration", "commonJava")));
        deps.add("commonResources", project.getDependencies().project(Map.of("path", ":common", "configuration", "commonResources")));


        deps.add("commonJava", project.getDependencies().project(Map.of("path", ":common", "configuration", "commonClientJava")));
        deps.add("commonResources", project.getDependencies().project(Map.of("path", ":common", "configuration", "commonClientResources")));


        project.getTasks().withType(ProcessResources.class).forEach((task) -> {
            task.filesMatching(multiLoaderRoot.filesToExpand.get(),
                    (s) -> {
                        s.expand(project.getProperties());
                    });
        });

//        }
//        deps.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, commonProject);

//        if(multiLoaderRoot.splitSources.get()) {
//            SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
//            SourceSet clientSourceSet = commonSourceSets.getByName("client");
//            deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, clientSourceSet.getOutput());
//        }
    }

    public static void setupNeoGradle(Project project, MultiLoaderNeo neoLoader) {
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        NeoForgeExtension neoForgeExt = project.getExtensions().getByType(NeoForgeExtension.class);
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        //Todo there a better way to do this?
        neoForgeExt.setVersion(neoLoader.neoVersion.get());
        neoForgeExt.runs(runModels -> {
            createRun(runModels, "client", run -> {
                run.client();
                run.getIdeName().set("Neo Client");
                run.getGameDirectory().set(project.file("run/client"));
            });

            createRun(runModels, "server", run -> {
                run.server();
                run.getIdeName().set("Neo Server");
                run.programArgument("--nogui");
                run.getGameDirectory().set(project.file("run/server"));
            });

            if(multiLoaderRoot.getDataGenOptions().isPresent() && multiLoaderRoot.getDataGenOptions().get().useNeo.isPresent()) {
                createRun(runModels, "clientData", run -> {
                    run.clientData();
                    run.getIdeName().set("Neo DataGen Client");
                    run.getProgramArguments().addAll(
                            "--mod", multiLoaderRoot.modID.get(),
                            "--all",
                            "--output", multiLoaderRoot.getDataGenOptions().get().useNeo.get().getAbsolutePath(),
                            "--existing", commonProject.file("src/main/resources").getAbsolutePath(),
                            "--existing", project.file("src/main/resources").getAbsolutePath());
                });

                createRun(runModels, "serverData", run -> {
                    run.serverData();
                    run.getIdeName().set("Neo DataGen Server");
                    run.getProgramArguments().addAll(
                            "--mod", multiLoaderRoot.modID.get(),
                            "--all",
                            "--output", multiLoaderRoot.getDataGenOptions().get().useNeo.get().getAbsolutePath(),
                            "--existing", commonProject.file("src/main/resources").getAbsolutePath(),
                            "--existing", project.file("src/main/resources").getAbsolutePath());
                });
            }
        });

        neoForgeExt.mods(modModels -> {
            ModModel modModel = modModels.maybeCreate(multiLoaderRoot.modID.get());
            modModel.sourceSet(project.getExtensions().getByType(SourceSetContainer.class).getByName("main"));
            modModels.add(modModel);
        });

        if (multiLoaderRoot.parchmentVersion.isPresent()) {
            neoForgeExt.parchment(parchment -> {
                parchment.getMappingsVersion().set(multiLoaderRoot.parchmentVersion.get());
                parchment.getMinecraftVersion().set(multiLoaderRoot.getParchmentMcVersion());
            });
        }


    }

    private static void createRun(NamedDomainObjectContainer<RunModel> runsExtension, String name, Consumer<RunModel> consumer) {
        RunModel run = runsExtension.maybeCreate(name);
        consumer.accept(run);
        runsExtension.add(run);
    }

}
