package dev.imabad.mlp.loaders;

import dev.imabad.mlp.MultiLoaderExtension;
import dev.imabad.mlp.ext.MultiLoaderRoot;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;

public class GenericLoader {
    public static void genericGradleSetup(Project project){
        //TODO: Do this better + split source sets!
        TaskContainer tasks = project.getTasks();
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();
        Project commonProject = MultiLoaderExtension.getCommonProject(project, multiLoaderRoot);
        SourceSetContainer commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer.class);
        SourceSet mainSourceSet = commonSourceSets.getByName("main");
        tasks.withType(ProcessResources.class).forEach((task) -> task.from(mainSourceSet.getResources()));
        tasks.withType(JavaCompile.class).forEach((task) -> task.source(mainSourceSet.getAllSource()));
        tasks.withType(Javadoc.class).forEach((task) -> task.source(mainSourceSet.getAllJava()));
        commonSourceSets.forEach(sourceSet -> {
            sourceSet.getAllSource().getSourceDirectories().filter((file) -> !file.exists()).forEach(File::mkdirs);
        });
        project.getExtensions().getByType(SourceSetContainer.class).forEach(sourceSet -> {
            sourceSet.getAllSource().getSourceDirectories().filter((file) -> !file.exists()).forEach(File::mkdirs);
        });
    }
}
