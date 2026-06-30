package com.pragyan.verifyhub.document;

import com.pragyan.verifyhub.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageInitializer {

    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        Path basePath = Paths.get(storageProperties.getBasePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
            log.info("Storage initialized at: {}", basePath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to initialize storage directory at: " + basePath, e);
        }
    }
}