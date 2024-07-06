package dev.nanite.mlp.aw2at;

import net.fabricmc.accesswidener.AccessWidenerReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AW2AT
{
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
}
