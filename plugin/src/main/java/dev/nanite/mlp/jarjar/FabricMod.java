package dev.nanite.mlp.jarjar;
import java.util.List;

public record FabricMod(int schemaVersion, String id, String version, String name, List<Jar> jars) {
    public record Jar(String file){}
}
