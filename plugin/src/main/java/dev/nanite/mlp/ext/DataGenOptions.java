package dev.nanite.mlp.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;

public abstract class DataGenOptions {

    public Property<File> useForge;
    public Property<File> useFabric;
    public Property<Boolean> mixBoth;

    @Inject
    public DataGenOptions(Project project) {
        useForge = project.getObjects().property(File.class);
        useFabric = project.getObjects().property(File.class);
        mixBoth = project.getObjects().property(Boolean.class).convention(false);
    }

}
