package com.pragyan.verifyhub.document;

import com.pragyan.verifyhub.config.StorageProperties;
import com.pragyan.verifyhub.document.dto.DocumentListResponse;
import com.pragyan.verifyhub.document.dto.DocumentSummary;
import com.pragyan.verifyhub.exception.DocumentNotFoundException;
import com.pragyan.verifyhub.exception.DuplicateDocumentException;
import com.pragyan.verifyhub.exception.InvalidFileException;
import com.pragyan.verifyhub.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageProperties storageProperties;
    private final FileValidator fileValidator;
    private final FileHasher fileHasher;
    private final ExifStripper exifStripper;

    private static final DateTimeFormatter DATE_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    @Transactional
    public Document upload(User user, MultipartFile file, DocumentType documentType) {
        String detectedMime = fileValidator.validate(file);
        log.debug("Upload validation passed for user {}, MIME {}", user.getEmail(), detectedMime);

        byte[] originalBytes = readBytes(file);

        String contentHash = fileHasher.hash(file);

        if (documentRepository.existsByUserAndContentHash(user, contentHash)) {
            log.warn("Duplicate upload attempt by user {} (hash {})", user.getEmail(), contentHash);
            throw new DuplicateDocumentException(
                    "This document has already been uploaded by you. " +
                            "If you believe this is an error, please contact support."
            );
        }

        byte[] cleanedBytes = exifStripper.stripIfImage(originalBytes, detectedMime);

        String sanitizedOriginal = fileValidator.sanitizeFilename(file.getOriginalFilename());
        String extension = extensionForMime(detectedMime);
        String storedFilename = UUID.randomUUID() + extension;
        Path relativePath = Paths.get(LocalDate.now().format(DATE_DIR_FORMAT), storedFilename);
        Path absolutePath = resolveStoragePath(relativePath);

        writeToDisk(absolutePath, cleanedBytes);

        Document doc = Document.builder()
                .user(user)
                .documentType(documentType)
                .originalFilename(sanitizedOriginal)
                .storedFilename(storedFilename)
                .filePath(relativePath.toString().replace('\\', '/'))  // store with forward slashes
                .fileSizeBytes((long) cleanedBytes.length)
                .mimeType(detectedMime)
                .contentHash(contentHash)
                .status(DocumentStatus.PENDING)
                .build();

        Document saved = documentRepository.save(doc);
        log.info("Document {} uploaded by user {} (type {}, {} bytes)",
                saved.getId(), user.getEmail(), documentType, saved.getFileSizeBytes());

        return saved;
    }

    @Transactional(readOnly = true)
    public DocumentListResponse listForUser(User user, Pageable pageable) {
        Page<Document> page = documentRepository.findByUser(user, pageable);
        return DocumentListResponse.from(page);
    }

    @Transactional(readOnly = true)
    public DocumentSummary getForUser(User user, Long documentId) {
        Document doc = documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> new DocumentNotFoundException(
                        "Document not found, or you do not have access to it"));
        return DocumentSummary.from(doc);
    }


    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read upload bytes", e);
            throw new InvalidFileException("Could not read uploaded file");
        }
    }

    private Path resolveStoragePath(Path relativePath) {
        Path basePath = Paths.get(storageProperties.getBasePath()).toAbsolutePath().normalize();
        Path resolved = basePath.resolve(relativePath).normalize();

        if (!resolved.startsWith(basePath)) {
            throw new IllegalStateException(
                    "Resolved path escaped storage base: " + resolved);
        }
        return resolved;
    }

    private void writeToDisk(Path absolutePath, byte[] bytes) {
        try {
            Files.createDirectories(absolutePath.getParent());
            Files.write(absolutePath, bytes);
            log.debug("Wrote {} bytes to {}", bytes.length, absolutePath);
        } catch (IOException e) {
            log.error("Failed to write file to disk at {}", absolutePath, e);
            throw new IllegalStateException("Could not persist uploaded file", e);
        }
    }

    private String extensionForMime(String mime) {
        return switch (mime.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }
}