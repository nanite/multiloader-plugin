package dev.nanite.mlp.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.nanite.mlp.MultiLoaderExtension;
import dev.nanite.mlp.ext.MultiLoaderRoot;
import dev.nanite.mlp.jarjar.FabricMod;
import dev.nanite.mlp.jarjar.Metadata;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.jvm.tasks.Jar;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public abstract class SingleOutputJar extends DefaultTask {

    private static final Gson GSON = new GsonBuilder().create();

    @Internal
    final Manifest manifest = new Manifest();

    @Inject
    public SingleOutputJar() {
        getArchiveGroup().convention(getProject().getGroup().toString());
        getArchiveVersion().convention(getProject().getVersion().toString());
        getArchiveIdentifier().convention(getProject().getName());
        this.getOutput().convention(getProject().getLayout()
                .file(getProject().provider(() ->
                        new File(getProject().getBuildDir(), "libs/" + getArchiveIdentifier().get() + "-" + getArchiveVersion().get() + "-ml.jar"))));
        Jar forgeJar = (Jar) getProject().project("forge").getTasks().getByName("jar");
        getForgeJar().convention(forgeJar.getArchiveFile());
        Jar fabricJar = (Jar) getProject().project("fabric").getTasks().getByName("remapJar");
        getFabricJar().convention(fabricJar.getArchiveFile());
    }

    public Manifest getManifest() {
        return manifest;
    }

    @InputFile
    public abstract RegularFileProperty getForgeJar();
    @InputFile
    public abstract RegularFileProperty getFabricJar();
    @Input
    public abstract Property<String> getArchiveGroup();
    @Input
    public abstract Property<String> getArchiveIdentifier();
    @Input
    public abstract Property<String> getArchiveVersion();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @TaskAction
    public void run(){
        MultiLoaderRoot multiLoaderRoot = MultiLoaderExtension.getRootExtension(getProject()).getRootOptions().get();
        Path outputPath = getOutput().getAsFile().get().toPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.deleteIfExists(outputPath);
        } catch (Exception e){}
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try(final JarOutputStream jarout = new JarOutputStream(Files.newOutputStream(outputPath), manifest)){
            File forgeJarFile = getForgeJar().getAsFile().get();
            putJarIntoJar(jarout, forgeJarFile, "META-INF/jarjar/");
            jarout.putNextEntry(new ZipEntry("META-INF/jarjar/metadata.json"));
            jarout.write(GSON.toJson(new Metadata(Collections.singletonList(new Metadata.MetadataJar(
                    new Metadata.MetadataJarIdentifier(getArchiveGroup().get(), getArchiveIdentifier().get()),
                    new Metadata.MetadataJarVersion(getArchiveVersion().get(),
                            "[" + getArchiveVersion().get() + "]"),
                    "META-INF/jarjar/" + forgeJarFile.toPath().getFileName().toString(), true))))
                    .getBytes(StandardCharsets.UTF_8));
            jarout.closeEntry();

            File fabricJarFile = getFabricJar().getAsFile().get();
            putJarIntoJar(jarout, fabricJarFile, "META-INF/jars/");

            jarout.putNextEntry(new ZipEntry("fabric.mod.json"));
            jarout.write(GSON.toJson(new FabricMod(1, multiLoaderRoot.modID.get() + "_ml",
                    getArchiveVersion().get(), getArchiveIdentifier().get(),
                    Collections.singletonList(
                            new FabricMod.Jar("META-INF/jars/" + fabricJarFile.toPath().getFileName().toString())
                    ))).getBytes(StandardCharsets.UTF_8));
            jarout.closeEntry();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void putJarIntoJar(JarOutputStream jarOut, File jarFile, String destPath) throws IOException {
        Path forgeJarPath = jarFile.toPath();
        String finalForgeJarPath = destPath + forgeJarPath.getFileName().toString();
        jarOut.putNextEntry(new ZipEntry(finalForgeJarPath));
        try(final InputStream inputStream = Files.newInputStream(forgeJarPath)){
            inputStream.transferTo(jarOut);
        }
        jarOut.closeEntry();
    }
}
