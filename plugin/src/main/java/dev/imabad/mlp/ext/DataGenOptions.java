package dev.imabad.mlp.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class DataGenOptions {

    public Property<Boolean> useForge;
    public Property<Boolean> useFabric;
    public Property<Boolean> mixBoth;

    @Inject
    public DataGenOptions(Project project) {
        useForge = project.getObjects().property(Boolean.class).convention(false);
        useFabric = project.getObjects().property(Boolean.class).convention(false);
        mixBoth = project.getObjects().property(Boolean.class).convention(false);
    }

}
