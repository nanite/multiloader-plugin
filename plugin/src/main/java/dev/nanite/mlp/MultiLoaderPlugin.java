package dev.nanite.mlp;

import org.gradle.api.Project;
import org.gradle.api.Plugin;

public class MultiLoaderPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getExtensions().create("multiLoader", MultiLoaderExtension.class);
    }
}
