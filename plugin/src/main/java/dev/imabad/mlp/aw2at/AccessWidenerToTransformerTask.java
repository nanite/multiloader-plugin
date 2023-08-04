package dev.imabad.mlp.aw2at;

import dev.imabad.mlp.MultiLoaderExtension;
import dev.imabad.mlp.ext.MultiLoaderRoot;
import dev.imabad.mlp.test.AccessRemappper;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.mappingio.format.ProGuardReader;
import net.fabricmc.mappingio.format.TsrgReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraftforge.gradle.common.tasks.DownloadMavenArtifact;
import net.minecraftforge.gradle.common.util.Utils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//This should only run from the root project
public class AccessWidenerToTransformerTask extends DefaultTask {

    @TaskAction
    public void run() throws IOException {
        Project project = getProject();
        Project forgeProject = project.project(":forge");
        Project commonProject = project.project(":common");

        MultiLoaderRoot root = MultiLoaderExtension.getRootExtension(project).getRootOptions().get();

        LoomGradleExtension extension = LoomGradleExtension.get(commonProject);
        MemoryMappingTree tree = new MemoryMappingTree();
        InputStream mojangMappings = new ByteArrayInputStream(getMojangMappings(forgeProject, root.minecraftVersion.get()));
        ProGuardReader.read(new InputStreamReader(mojangMappings), "named", "obf", tree);

        InputStream inputStream = new ByteArrayInputStream(getSrg(forgeProject));


        TsrgReader.read(new InputStreamReader(inputStream), tree);
        AccessRemappper remappper = new AccessRemappper(extension.getMinecraftJars(MappingsNamespace.NAMED), tree, "named", "srg");
        //Todo: make this configurable
        File file = commonProject.file("src/main/resources/test.accesswidener");
        byte[] remap = remappper.remap(Files.readAllBytes(file.toPath()));
        //Todo make this configurable
        Files.write(forgeProject.file("src/main/resources/META-INF/at.cfg").toPath(), remap, StandardOpenOption.CREATE);
    }

    private byte[] getMojangMappings(Project forgeProject, String minecraftVersion) throws IOException{
        Path resolve = Utils.getCacheBase(forgeProject).resolve("minecraft_repo").resolve("versions").resolve(minecraftVersion).resolve("client_mappings.txt");
        if(!Files.exists(resolve)){
            throw new IOException("Could not find mappings file for " + minecraftVersion + " forge needs to download files first");
        }else {
            return Files.readAllBytes(resolve);
        }
    }


    //This is a bit of a hack, but it works, could just directly reference the file?
    private byte[] getSrg(Project forgeProject) throws IOException {
        Task mcpConfig = forgeProject.getTasks().getByName("downloadMcpConfig");
        if (mcpConfig instanceof DownloadMavenArtifact data) {
            RegularFile regularFile = data.getOutput().get();
            try (ZipFile zipFile = new ZipFile(regularFile.getAsFile())) {
                ZipEntry entry = zipFile.getEntry("config/joined.tsrg");
                if (entry != null) {
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        return stream.readAllBytes();
                    }
                }
            }
        }
        throw new IOException("Could not find joined.tsrg in MCP Config");
    }
}
