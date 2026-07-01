package com.pragyan.verifyhub.admin.dto;

import com.pragyan.verifyhub.audit.AuditAction;
import com.pragyan.verifyhub.audit.AuditLog;
import com.pragyan.verifyhub.document.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSummary {

    private Long id;
    private AuditAction action;
    private DocumentStatus oldStatus;
    private DocumentStatus newStatus;
    private String reason;
    private String actorEmail;
    private String actorFullName;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;

    public static AuditLogSummary from(AuditLog entry) {
        return AuditLogSummary.builder()
                .id(entry.getId())
                .action(entry.getAction())
                .oldStatus(entry.getOldStatus())
                .newStatus(entry.getNewStatus())
                .reason(entry.getReason())
                .actorEmail(entry.getActor().getEmail())
                .actorFullName(entry.getActor().getFullName())
                .ipAddress(entry.getIpAddress())
                .userAgent(entry.getUserAgent())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
