package com.example.docformatting.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-storage")
public class FileStorageProperties {

    /**
     * Base directory used to persist files on local storage.
     */
    private Path baseDir = Paths.get("./storage");

    public Path getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
    }
}
