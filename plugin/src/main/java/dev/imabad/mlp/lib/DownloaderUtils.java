package dev.imabad.mlp.lib;

import dev.imabad.mlp.MultiLoaderExtension;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloaderUtils {

    public static Path getCacheDir(Project project) {
        File gradleUserHomeDir = project.getGradle().getGradleUserHomeDir();
        String minecraftVersion = MultiLoaderExtension.getRootExtension(project).getRootOptions().get().minecraftVersion.get();
        return Paths.get(gradleUserHomeDir.getPath(), "caches", "multiloader", minecraftVersion);

    }

}
