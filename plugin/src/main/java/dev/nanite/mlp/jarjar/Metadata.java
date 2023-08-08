package dev.nanite.mlp.jarjar;

import java.util.List;

public record Metadata(List<MetadataJar> jars) {

    public record MetadataJar(MetadataJarIdentifier identifier, MetadataJarVersion version, String path, boolean isObfuscated){}

    public record MetadataJarIdentifier(String group, String artifact){}

    public record MetadataJarVersion(String artifactVersion, String range){}

}

