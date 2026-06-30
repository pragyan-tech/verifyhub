package com.pragyan.verifyhub.document.dto;

import com.pragyan.verifyhub.document.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private String message;
    private DocumentSummary document;

    public static UploadResponse success(Document doc) {
        return UploadResponse.builder()
                .message("Document uploaded successfully and is pending verification")
                .document(DocumentSummary.from(doc))
                .build();
    }
}