package dev.imabad.mlp.test;

import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerRemapper;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.util.TinyRemapperHelper;
import net.fabricmc.loom.util.service.ScopedSharedServiceManager;
import net.fabricmc.mappingio.format.SrgReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.gradle.api.Project;
import org.objectweb.asm.commons.Remapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReappearTest {

    public static byte[] remapAccessWidener(Project project, byte[] input) {

        LoomGradleExtension extension = LoomGradleExtension.get(project);
        try(ScopedSharedServiceManager serviceManager = new ScopedSharedServiceManager()) {
            MemoryMappingTree mappingTree = extension.getMappingConfiguration().getMappingsService(serviceManager).getMappingTree();

//            MemoryMappingTree newTree = new MemoryMappingTree(mappingTree);
//            SrgReader.read(Files.newBufferedReader(Path.of("joined.tsrg")), newTree);

            return remap(mappingTree, MappingsNamespace.NAMED.toString(), MappingsNamespace.NAMED.toString(), input);

        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static byte[] remap(MemoryMappingTree mappingTree, String fromM, String toM, byte[] input) {
        TinyRemapper tinyRemapper = TinyRemapper.newRemapper()
                .withMappings(TinyRemapperHelper.create(mappingTree, fromM, toM, true))
                .ignoreFieldDesc(true)
                .build();

        final Remapper sigAsmRemapper = tinyRemapper.getEnvironment().getRemapper();

        ATWriter writer = new ATWriter();
        AccessWidenerRemapper awRemapper = new AccessWidenerRemapper(writer, sigAsmRemapper, fromM, toM);
        AccessWidenerReader reader = new AccessWidenerReader(awRemapper);
        reader.read(input);
        tinyRemapper.finish();
        return writer.write();
    }

}
