package dev.imabad.mlp;

import dev.imabad.mlp.test.HiMikeTask;
import org.gradle.api.Project;
import org.gradle.api.Plugin;

public class MultiLoaderPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getExtensions().create("multiLoader", MultiLoaderExtension.class);
        project.getTasks().register("himikey", HiMikeTask.class);
    }
}
