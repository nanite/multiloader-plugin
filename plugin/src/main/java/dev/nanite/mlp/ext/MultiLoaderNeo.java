package dev.nanite.mlp.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class MultiLoaderNeo {

    public Property<String> neoVersion;
    @Inject
    public MultiLoaderNeo(Project project) {
        neoVersion = project.getObjects().property(String.class);
    }
}
