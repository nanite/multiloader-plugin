package dev.nanite.mlp.ext;

import dev.nanite.mlp.MultiLoaderExtension;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class MultiLoaderForge {

    public Property<Boolean> isNeo;
    public Property<String> forgeVersion;
    public Property<Boolean> useDataGen;
    public Property<String> forgeMixins;
    public Property<Boolean> useMixins;
    @Inject
    public MultiLoaderForge(Project project) {
        isNeo = project.getObjects().property(Boolean.class).convention(false);
        forgeVersion = project.getObjects().property(String.class);
        useDataGen = project.getObjects().property(Boolean.class).convention(false);
        useMixins = project.getObjects().property(Boolean.class).convention(true);
        forgeMixins = project.getObjects().property(String.class)
                .convention(MultiLoaderExtension.getRootExtension(project).getRootOptions().get().modID.get()
                        + ".forge.mixins.json");
    }
}
