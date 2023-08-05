package dev.imabad.mlp.ext;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public abstract class MultiLoaderRoot  {

    public Property<String> minecraftVersion;
    public Property<String> parchmentVersion;
    public Property<String> mixinString;
    public Property<String> commonProjectName;
    public Property<String> modID;
    public Property<String> accessWidenerFile;
    public Property<Boolean> splitSources;
    public Property<Boolean> singleOutputJar;
    public ListProperty<String> filesToExpand;
    public abstract Property<DataGenOptions> getDataGenOptions();

    private Project project;
    @Inject
    public MultiLoaderRoot(Project project) {
        this.project = project;
        minecraftVersion = project.getObjects().property(String.class);
        parchmentVersion = project.getObjects().property(String.class);
        mixinString = project.getObjects().property(String.class).convention("org.spongepowered:mixin:0.8.5");
        commonProjectName = project.getObjects().property(String.class).convention("common");
        modID = project.getObjects().property(String.class);
        accessWidenerFile = project.getObjects().property(String.class).convention("src/main/resources/%s.accesswidener");
        splitSources = project.getObjects().property(Boolean.class).convention(false);
        singleOutputJar = project.getObjects().property(Boolean.class).convention(false);
        filesToExpand = project.getObjects().listProperty(String.class)
                .convention(Arrays.asList("pack.mcmeta", "fabric.mod.json",
                        "META-INF/mods.toml", "mods.toml", "*.mixins.json"));
    }

    public void dataGen(Action<DataGenOptions> action) {
        DataGenOptions dataGenOptions = project.getObjects().newInstance(DataGenOptions.class, project);
        action.execute(dataGenOptions);
        getDataGenOptions().set(dataGenOptions);
    }

}
