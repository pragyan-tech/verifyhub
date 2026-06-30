package com.pragyan.verifyhub.document;

import com.pragyan.verifyhub.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false,
            columnDefinition = "ENUM('AADHAAR','PAN','PASSPORT','DRIVING_LICENSE','OTHER')")
    private DocumentType documentType;

    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 500)
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "content_hash", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false,
            columnDefinition = "ENUM('PENDING','UNDER_REVIEW','VERIFIED','REJECTED')")
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}