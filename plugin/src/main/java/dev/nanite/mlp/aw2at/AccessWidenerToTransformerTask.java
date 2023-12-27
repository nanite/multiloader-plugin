package dev.nanite.mlp.aw2at;

import dev.nanite.mlp.MultiLoaderExtension;
import dev.nanite.mlp.ext.MultiLoaderRoot;
import dev.nanite.mlp.lib.DownloaderUtils;
import dev.nanite.mlp.lib.minecraft.PistonMeta;
import dev.nanite.mlp.lib.minecraft.VersionMeta;
import dev.nanite.mlp.test.ATWriter;
import dev.nanite.mlp.test.AccessRemappper;
import dev.nanite.mlp.test.NeoCustomAccessWidenerRemapper;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.util.ZipUtils;
import net.fabricmc.loom.util.download.Download;
import net.fabricmc.mappingio.format.ProGuardReader;
import net.fabricmc.mappingio.format.TsrgReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraftforge.gradle.common.util.Utils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

//This should only run from the root project
public class AccessWidenerToTransformerTask extends DefaultTask {

    public static final String ACCESS_TRANSFORMER_PATH = "src/main/resources/META-INF/accesstransformer.cfg";

    private static final String MCP_CONFIG = "https://maven.minecraftforge.net/releases/de/oceanlabs/mcp/mcp_config/%s/mcp_config-%s.zip";
    private static final String PISTON_META = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    @TaskAction
    public void run() throws IOException, URISyntaxException {
        runTransformer(getProject());
    }

    public static void runConverter(Path accessWidener, Path output) throws IOException {
        ATWriter writer = new ATWriter();
        NeoCustomAccessWidenerRemapper awRemapper = new NeoCustomAccessWidenerRemapper(writer);
        AccessWidenerReader reader = new AccessWidenerReader(awRemapper);
        reader.read(Files.readAllBytes(accessWidener));
        if(!Files.exists(output.getParent())) {
            Files.createDirectories(output);
        }
        Files.write(output, writer.write());
    }

    public static void runTransformer(Project rootProject) throws IOException, URISyntaxException {
        rootProject.getLogger().info("Converting access widener to access transformer");
        Project forgeProject = rootProject.project(":forge");
        Project commonProject = rootProject.project(":common");

        MultiLoaderRoot root = MultiLoaderExtension.getRootExtension(rootProject).getRootOptions().get();

        LoomGradleExtension extension = LoomGradleExtension.get(commonProject);
        MemoryMappingTree tree = new MemoryMappingTree();
        Path cacheDir = DownloaderUtils.getCacheDir(rootProject);
        InputStream mojangMappings = new ByteArrayInputStream(getMojangMappings(cacheDir, root.minecraftVersion.get()));
        ProGuardReader.read(new InputStreamReader(mojangMappings), "named", "obf", tree);

        InputStream inputStream = new ByteArrayInputStream(getSrgMappings(cacheDir, root.minecraftVersion.get()));


        TsrgReader.read(new InputStreamReader(inputStream), tree);
        AccessRemappper remappper = new AccessRemappper(extension.getMinecraftJars(MappingsNamespace.NAMED), tree, "named", "srg");
        File file = root.accessWidenerFile.get();
        byte[] remap = remappper.remap(Files.readAllBytes(file.toPath()));
        Files.write(forgeProject.file(ACCESS_TRANSFORMER_PATH).toPath(), remap, StandardOpenOption.CREATE);
    }

    public static byte[] getMojangMappings(Path folder, String minecraftVersion) throws IOException, URISyntaxException {
        Path mojangMappings = folder.resolve("mojang_mappings.txt");
        error:
        if(!Files.exists(mojangMappings)) {
            String pistonMetaData = Download.create(PISTON_META).downloadString();
            PistonMeta pistonMeta = Utils.GSON.fromJson(pistonMetaData, PistonMeta.class);
            for (PistonMeta.Version version : pistonMeta.versions) {
                if(version.id.equals(minecraftVersion)) {
                    String versionMetaString = Download.create(version.url).downloadString();
                    VersionMeta versionMeta = Utils.GSON.fromJson(versionMetaString, VersionMeta.class);
                    VersionMeta.Downloads downloads = versionMeta.downloads;
                    Download.create(downloads.client_mappings.url)
                            .sha1(downloads.client_mappings.sha1)
                            .defaultCache()
                            .downloadPath(mojangMappings);
                    break error;
                }
            }
            throw new RuntimeException("Could not find mojang mappings for " + minecraftVersion);
        }
        return Files.readAllBytes(mojangMappings);

    }

    public static byte[] getSrgMappings(Path folder, String minecraftVersion) throws IOException, URISyntaxException {
        Path mcpConfigPath = folder.resolve("mcp_config.zip");
        if(!Files.exists(mcpConfigPath)) {
            Download.create(String.format(MCP_CONFIG, minecraftVersion, minecraftVersion))
                    .downloadPath(mcpConfigPath);
        }
        return ZipUtils.unpack(mcpConfigPath, "config/joined.tsrg");
    }

}
