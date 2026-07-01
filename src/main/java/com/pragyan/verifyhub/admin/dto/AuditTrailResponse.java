package com.pragyan.verifyhub.admin.dto;

import com.pragyan.verifyhub.audit.AuditLog;
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
public class AuditTrailResponse {

    private Long documentId;
    private List<AuditLogSummary> entries;
    private int page;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static AuditTrailResponse from(Long documentId, Page<AuditLog> page) {
        return AuditTrailResponse.builder()
                .documentId(documentId)
                .entries(page.getContent().stream()
                        .map(AuditLogSummary::from)
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
