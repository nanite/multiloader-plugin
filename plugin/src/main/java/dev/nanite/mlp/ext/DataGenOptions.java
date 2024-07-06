package dev.nanite.mlp.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;

public abstract class DataGenOptions {

    public Property<File> useNeo;
    public Property<File> useFabric;


    @Inject
    public DataGenOptions(Project project) {
        useFabric = project.getObjects().property(File.class);
        useNeo = project.getObjects().property(File.class);
    }

}
