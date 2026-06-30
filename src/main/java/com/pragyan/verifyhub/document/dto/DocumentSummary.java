package com.pragyan.verifyhub.document.dto;

import com.pragyan.verifyhub.document.Document;
import com.pragyan.verifyhub.document.DocumentStatus;
import com.pragyan.verifyhub.document.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummary {

    private Long id;
    private DocumentType documentType;
    private String originalFilename;
    private Long fileSizeBytes;
    private String mimeType;
    private DocumentStatus status;
    private String rejectionReason;
    private Instant createdAt;
    private Instant reviewedAt;

    public static DocumentSummary from(Document doc) {
        return DocumentSummary.builder()
                .id(doc.getId())
                .documentType(doc.getDocumentType())
                .originalFilename(doc.getOriginalFilename())
                .fileSizeBytes(doc.getFileSizeBytes())
                .mimeType(doc.getMimeType())
                .status(doc.getStatus())
                .rejectionReason(doc.getRejectionReason())
                .createdAt(doc.getCreatedAt())
                .reviewedAt(doc.getReviewedAt())
                .build();
    }
}