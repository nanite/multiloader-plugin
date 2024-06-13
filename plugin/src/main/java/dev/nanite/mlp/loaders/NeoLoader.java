package dev.nanite.mlp.loaders;

import dev.nanite.mlp.MultiLoaderExtension;
import dev.nanite.mlp.aw2at.AccessWidenerToTransformerTask;
import dev.nanite.mlp.ext.MultiLoaderNeo;
import dev.nanite.mlp.ext.MultiLoaderRoot;
import net.neoforged.gradle.dsl.common.extensions.Minecraft;
import net.neoforged.gradle.dsl.common.runs.run.Run;
import net.neoforged.gradle.userdev.UserDevPlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.FactoryNamedDomainObjectContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class NeoLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeoLoader.class);
    public static void setupNeo(Project project, MultiLoaderNeo multiLoaderForge){
        applyNeoPlugins(project);
        configureNeoDependencies(project, multiLoaderForge);
        setupNeoGradle(project, multiLoaderForge);
        GenericLoader.genericGradleSetup(project);
    }

    public static void applyNeoPlugins(Project project){
        project.getPlugins().apply(UserDevPlugin.class);
    }

    public static void configureNeoDependencies(Project project, MultiLoaderNeo neoLoader) {
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        DependencyHandler deps = project.getDependencies();
        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, "net.neoforged:neoforge:" + neoLoader.neoVersion.get());
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
        Minecraft minecraft = project.getExtensions().getByType(Minecraft.class);
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        //Todo there a better way to do this?
        FactoryNamedDomainObjectContainer<Run> runsExtension = ((FactoryNamedDomainObjectContainer<Run>) project.getExtensions().getByName("runs"));

        if (multiLoaderRoot.isForgeATEnabled()) {
            minecraft.getAccessTransformers().file(AccessWidenerToTransformerTask.ACCESS_TRANSFORMER_PATH);
        }
        createRun(runsExtension, "client", run -> {


        });
        createRun(runsExtension, "server", run -> {
            run.programArgument("--nogui");
        });
        if (multiLoaderRoot.getDataGenOptions().isPresent() && (multiLoaderRoot.getDataGenOptions().get().useNeo.isPresent() || multiLoaderRoot.getDataGenOptions().get().mixBoth.get())) {
            createRun(runsExtension, "data", run -> {
                run.getProgramArguments().addAll(
                        "--mod", multiLoaderRoot.modID.get(),
                        "--all",
                        "--output", multiLoaderRoot.getDataGenOptions().get().useNeo.get().getName(),
                        "--existing", commonProject.file("src/main/resources").getName(),
                        "--existing", project.file("src/main/resources").getName());
            });
        }
        runsExtension.configureEach(run -> run.modSource(project.getExtensions().getByType(SourceSetContainer.class).getByName("main")));
    }

    private static void createRun(FactoryNamedDomainObjectContainer<Run> runsExtension, String name, Consumer<Run> consumer) {
        Run run = runsExtension.maybeCreate(name);
        consumer.accept(run);
        runsExtension.add(run);
    }

}
