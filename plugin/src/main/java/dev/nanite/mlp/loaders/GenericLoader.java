package dev.nanite.mlp.loaders;

import dev.nanite.mlp.MultiLoaderExtension;
import dev.nanite.mlp.ext.MultiLoaderRoot;
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
        SourceSetContainer projectSourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet mainSourceSet = commonSourceSets.getByName("main");
        if(!multiLoaderRoot.splitSources.get()){
            tasks.withType(JavaCompile.class).matching(t -> !t.getName().contains("test")).forEach((task) -> task.source(mainSourceSet.getAllSource()));
            tasks.withType(ProcessResources.class).matching(t -> !t.getName().contains("test")).forEach((task) -> task.from(mainSourceSet.getResources()));
        } else {
            try  {
                projectSourceSets.getByName("client");
                tasks.withType(JavaCompile.class).forEach((task) -> {
                    if(!task.getName().toLowerCase().contains("client") && !task.getName().toLowerCase().contains("test")){
                        task.source(mainSourceSet.getAllSource());
                    }
                });
                tasks.withType(ProcessResources.class).forEach((task) -> {
                    if(!task.getName().toLowerCase().contains("client") && !task.getName().toLowerCase().contains("test")){
                        task.from(mainSourceSet.getResources());
                    }
                });
            } catch (Exception e){
                //No Client sourceset, so this is safe!
                tasks.withType(JavaCompile.class).matching(t -> !t.getName().contains("test")).forEach((task) -> task.source(mainSourceSet.getAllSource()));
                tasks.withType(ProcessResources.class).matching(t -> !t.getName().contains("test")).forEach((task) -> task.from(mainSourceSet.getResources()));
            }
        }
        tasks.withType(ProcessResources.class).forEach((task) -> {
            task.filesMatching(multiLoaderRoot.filesToExpand.get(),
                    (s) -> {
                s.expand(project.getProperties());
            });
        });
        tasks.withType(Javadoc.class).forEach((task) -> task.source(mainSourceSet.getAllJava()));
        commonSourceSets.matching(t -> !t.getName().contains("test")).forEach(sourceSet -> {
            sourceSet.getAllSource().getSourceDirectories().filter((file) -> !file.exists()).forEach(File::mkdirs);
        });
        project.getExtensions().getByType(SourceSetContainer.class).matching(t -> !t.getName().contains("test")).forEach(sourceSet -> {
            sourceSet.getAllSource().getSourceDirectories().filter((file) -> !file.exists()).forEach(File::mkdirs);
        });
    }
}
