package dev.imabad.mlp.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class MultiLoaderRoot  {

    public Property<String> minecraftVersion;
    public Property<String> parchmentVersion;
    public Property<String> mixinString;
    public Property<String> commonProjectName;
    public Property<String> modID;
    public Property<String> accessWidenerFile;
    @Inject
    public MultiLoaderRoot(Project project) {
        minecraftVersion = project.getObjects().property(String.class);
        parchmentVersion = project.getObjects().property(String.class);
        mixinString = project.getObjects().property(String.class).convention("org.spongepowered:mixin:0.8.5");
        commonProjectName = project.getObjects().property(String.class).convention("common");
        modID = project.getObjects().property(String.class);
        accessWidenerFile = project.getObjects().property(String.class).convention("src/main/resources/%s.accesswidener");
    }

}
