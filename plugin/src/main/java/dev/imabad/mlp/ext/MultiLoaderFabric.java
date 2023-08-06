package dev.imabad.mlp.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class MultiLoaderFabric {

    public Property<String> fabricLoaderVersion;
    public Property<String> fabricApiVersion;

    @Inject
    public MultiLoaderFabric(Project project) {
        fabricLoaderVersion = project.getObjects().property(String.class);
        fabricApiVersion = project.getObjects().property(String.class);
    }
}
