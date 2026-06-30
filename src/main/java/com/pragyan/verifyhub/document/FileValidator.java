package com.pragyan.verifyhub.document;

import com.pragyan.verifyhub.config.StorageProperties;
import com.pragyan.verifyhub.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileValidator {

    private final StorageProperties storageProperties;
    private final Tika tika = new Tika();


    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("No file was provided");
        }

        long maxSize = storageProperties.getMaxFileSizeBytes();
        if (file.getSize() > maxSize) {
            throw new InvalidFileException(
                    "File size " + file.getSize() + " bytes exceeds maximum allowed " + maxSize + " bytes"
            );
        }

        String detectedMime = detectMimeType(file);

        Set<String> allowed = Set.copyOf(storageProperties.getAllowedMimeTypes());
        if (!allowed.contains(detectedMime)) {
            log.warn("Rejected file upload: detected MIME '{}' not in allowed set {}", detectedMime, allowed);
            throw new InvalidFileException(
                    "File type '" + detectedMime + "' is not allowed. Permitted types: " + allowed
            );
        }

        String claimedMime = file.getContentType();
        if (claimedMime != null && !claimedMime.equalsIgnoreCase(detectedMime)) {
            log.warn("MIME mismatch — client claimed '{}', actual content is '{}'", claimedMime, detectedMime);
        }

        return detectedMime;
    }

    private String detectMimeType(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return tika.detect(in);
        } catch (IOException e) {
            throw new InvalidFileException("Could not read file content for type detection");
        }
    }


    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "unnamed";
        }
        String base = Paths.get(filename).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}