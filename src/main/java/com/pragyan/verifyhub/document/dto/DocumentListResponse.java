package com.pragyan.verifyhub.document.dto;

import com.pragyan.verifyhub.document.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentListResponse {

    private List<DocumentSummary> documents;
    private int page;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static DocumentListResponse from(Page<Document> page) {
        return DocumentListResponse.builder()
                .documents(page.getContent().stream()
                        .map(DocumentSummary::from)
                        .toList())
                .page(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}