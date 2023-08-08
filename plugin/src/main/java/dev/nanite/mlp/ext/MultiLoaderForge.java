package dev.nanite.mlp.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class MultiLoaderForge {

    public Property<Boolean> isNeo;
    public Property<String> forgeVersion;
    public Property<Boolean> useDataGen;
    @Inject
    public MultiLoaderForge(Project project) {
        isNeo = project.getObjects().property(Boolean.class).convention(false);
        forgeVersion = project.getObjects().property(String.class);
        useDataGen = project.getObjects().property(Boolean.class).convention(false);
    }
}
