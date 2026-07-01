package com.pragyan.verifyhub.audit;

import com.pragyan.verifyhub.document.Document;
import com.pragyan.verifyhub.document.DocumentStatus;
import com.pragyan.verifyhub.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, updatable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false, updatable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false,
            columnDefinition = "ENUM('UPLOADED','STATE_CHANGED','VIEWED','DOWNLOADED')")
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", updatable = false,
            columnDefinition = "ENUM('PENDING','UNDER_REVIEW','VERIFIED','REJECTED')")
    private DocumentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", updatable = false,
            columnDefinition = "ENUM('PENDING','UNDER_REVIEW','VERIFIED','REJECTED')")
    private DocumentStatus newStatus;

    @Column(name = "reason", length = 500, updatable = false)
    private String reason;

    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 500, updatable = false)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}