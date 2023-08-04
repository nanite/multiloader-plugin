package dev.imabad.mlp.ext;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;

public abstract class MultiLoaderRoot  {

    public Property<String> minecraftVersion;
    public Property<String> parchmentVersion;
    public Property<String> mixinString;
    public Property<String> commonProjectName;
    public Property<String> modID;
    public Property<File> accessWidenerFile;
    public Property<Boolean> splitSources;
    public Property<Boolean> convertAccessWidener;
    @Inject
    public MultiLoaderRoot(Project project) {
        minecraftVersion = project.getObjects().property(String.class);
        parchmentVersion = project.getObjects().property(String.class);
        mixinString = project.getObjects().property(String.class).convention("org.spongepowered:mixin:0.8.5");
        commonProjectName = project.getObjects().property(String.class).convention("common");
        modID = project.getObjects().property(String.class);
        accessWidenerFile = project.getObjects().property(File.class);
        splitSources = project.getObjects().property(Boolean.class).convention(true);
        convertAccessWidener = project.getObjects().property(Boolean.class).convention(true);
    }

    //Todo better name?
    public boolean isForgeATEnabled() {
        return accessWidenerFile.isPresent() && convertAccessWidener.get();
    }

}
