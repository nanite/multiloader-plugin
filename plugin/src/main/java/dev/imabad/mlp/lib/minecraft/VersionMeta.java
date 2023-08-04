package dev.imabad.mlp.lib.minecraft;

public class VersionMeta {
    public Downloads downloads;

    public static class Downloads {
        public DownloadMeta client_mappings;

        public static class DownloadMeta {
            public String url;
            public String sha1;
        }
    }
}
