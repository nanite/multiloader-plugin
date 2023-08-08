package dev.nanite.mlp.test;

import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.loom.util.TinyRemapperHelper;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.objectweb.asm.commons.Remapper;

import java.nio.file.Path;
import java.util.List;

public class AccessRemappper {

    private final Remapper sigAsmRemapper;
    private final TinyRemapper tinyRemapper;
    public AccessRemappper(List<Path> minecraftJars, MemoryMappingTree mappingTree, String fromM, String toM) {
        TinyRemapper tinyRemapper = TinyRemapper.newRemapper()
                .withMappings(TinyRemapperHelper.create(mappingTree, fromM, toM, true))
                .ignoreFieldDesc(true)
                .build();

        for (Path minecraftJar : minecraftJars) {
            //Todo async might cause issues? if it finish before or something
            tinyRemapper.readClassPath(minecraftJar);
        }
        this.sigAsmRemapper = tinyRemapper.getEnvironment().getRemapper();
        this.tinyRemapper = tinyRemapper;
    }

    public byte[] remap(byte[] input) {
        ATWriter writer = new ATWriter();
        CustomAccessWidenerRemapper awRemapper = new CustomAccessWidenerRemapper(writer, sigAsmRemapper);
        AccessWidenerReader reader = new AccessWidenerReader(awRemapper);
        reader.read(input);
        return writer.write();
    }

    public String remap(String input) {
        ATWriter writer = new ATWriter();
        CustomAccessWidenerRemapper awRemapper = new CustomAccessWidenerRemapper(writer, sigAsmRemapper);
        AccessWidenerReader reader = new AccessWidenerReader(awRemapper);
        reader.read(input.getBytes());
        return writer.writeString();
    }

    public void finish() {
        tinyRemapper.finish();
    }



}
