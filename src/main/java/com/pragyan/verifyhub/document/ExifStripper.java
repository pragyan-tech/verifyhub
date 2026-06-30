package com.pragyan.verifyhub.document;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.pragyan.verifyhub.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ExifStripper {


    public byte[] stripIfImage(byte[] originalBytes, String mimeType) {
        if (!isStrippable(mimeType)) {
            log.debug("Skipping EXIF strip for non-image MIME type: {}", mimeType);
            return originalBytes;
        }

        logMetadata(originalBytes, mimeType);

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (image == null) {
                log.warn("ImageIO could not decode image with MIME {} — returning original bytes", mimeType);
                return originalBytes;
            }

            String format = formatForMime(mimeType);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean written = ImageIO.write(image, format, out);
            if (!written) {
                log.warn("ImageIO had no writer for format {} — returning original bytes", format);
                return originalBytes;
            }

            byte[] cleaned = out.toByteArray();
            log.debug("Stripped EXIF: original {} bytes -> cleaned {} bytes", originalBytes.length, cleaned.length);
            return cleaned;

        } catch (IOException e) {
            log.error("EXIF stripping failed for MIME {}; returning original bytes", mimeType, e);
            return originalBytes;
        }
    }

    private boolean isStrippable(String mimeType) {
        return "image/jpeg".equalsIgnoreCase(mimeType) || "image/png".equalsIgnoreCase(mimeType);
    }

    private String formatForMime(String mimeType) {
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new InvalidFileException("No format mapping for MIME " + mimeType);
        };
    }

    private void logMetadata(byte[] bytes, String mimeType) {
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(in);
            List<String> tags = collectInterestingTags(metadata);
            if (!tags.isEmpty()) {
                log.info("EXIF metadata present (will be stripped): {}", tags);
            } else {
                log.debug("No interesting EXIF tags found in upload");
            }
        } catch (ImageProcessingException | IOException e) {
            log.debug("Could not read metadata from image (MIME {}): {}", mimeType, e.getMessage());
        }
    }

    private List<String> collectInterestingTags(Metadata metadata) {
        List<String> tags = new ArrayList<>();
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                String name = tag.getTagName();
                if (isInteresting(name)) {
                    tags.add(directory.getName() + " > " + name + ": " + tag.getDescription());
                }
            }
        }
        return tags;
    }

    private boolean isInteresting(String tagName) {
        String n = tagName.toLowerCase();
        return n.contains("gps")
                || n.contains("date")
                || n.contains("time")
                || n.contains("make")
                || n.contains("model")
                || n.contains("software")
                || n.contains("artist")
                || n.contains("owner");
    }
}