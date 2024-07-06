package dev.nanite.mlp.loaders;

import dev.nanite.mlp.MultiLoaderExtension;
import dev.nanite.mlp.ext.MultiLoaderNeo;
import dev.nanite.mlp.ext.MultiLoaderRoot;
import net.neoforged.moddevgradle.dsl.ModModel;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import net.neoforged.moddevgradle.dsl.RunModel;
import net.neoforged.moddevgradle.internal.ModDevPlugin;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class NeoLoader {

    public static final String ACCESS_TRANSFORMER_PATH = "src/main/resources/META-INF/accesstransformer.cfg";

    private static final Logger LOGGER = LoggerFactory.getLogger(NeoLoader.class);
    public static void setupNeo(Project project, MultiLoaderNeo multiLoaderForge){
        applyNeoPlugins(project);
        configureNeoDependencies(project, multiLoaderForge);
        setupNeoGradle(project, multiLoaderForge);
        GenericLoader.genericGradleSetup(project);
    }

    public static void applyNeoPlugins(Project project){
        project.getPlugins().apply(ModDevPlugin.class);
    }

    public static void configureNeoDependencies(Project project, MultiLoaderNeo neoLoader) {
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        DependencyHandler deps = project.getDependencies();
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, commonProject);
        if(multiLoaderRoot.splitSources.get()) {
            SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
            SourceSet clientSourceSet = commonSourceSets.getByName("client");
            deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, clientSourceSet.getOutput());
        }
    }

    public static void setupNeoGradle(Project project, MultiLoaderNeo multiLoaderForge) {
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        NeoForgeExtension neoForgeExt = project.getExtensions().getByType(NeoForgeExtension.class);
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        //Todo there a better way to do this?
        neoForgeExt.getVersion().set(multiLoaderForge.neoVersion.get());
        if (multiLoaderRoot.isNeoATEnabled()) {
            neoForgeExt.getAccessTransformers().add(ACCESS_TRANSFORMER_PATH);
        }
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
                createRun(runModels, "data", run -> {
                    run.data();
                    run.getIdeName().set("Neo DataGen");
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
        });


    }

    private static void createRun(NamedDomainObjectContainer<RunModel> runsExtension, String name, Consumer<RunModel> consumer) {
        RunModel run = runsExtension.maybeCreate(name);
        consumer.accept(run);
        runsExtension.add(run);
    }

}
