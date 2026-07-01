package com.pragyan.verifyhub.admin;

import com.pragyan.verifyhub.admin.dto.AuditTrailResponse;
import com.pragyan.verifyhub.audit.AuditLog;
import com.pragyan.verifyhub.audit.AuditLogRepository;
import com.pragyan.verifyhub.audit.AuditService;
import com.pragyan.verifyhub.document.*;
import com.pragyan.verifyhub.document.dto.DocumentListResponse;
import com.pragyan.verifyhub.exception.DocumentNotFoundException;
import com.pragyan.verifyhub.exception.IllegalDocumentTransitionException;
import com.pragyan.verifyhub.exception.InvalidReviewException;
import com.pragyan.verifyhub.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDocumentService {

    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final StateMachineFactory<DocumentStatus, DocumentEvent> stateMachineFactory;

    @Transactional(readOnly = true)
    public DocumentListResponse listPendingDocuments(Pageable pageable) {
        Page<Document> page = documentRepository.findByStatus(DocumentStatus.PENDING, pageable);
        return DocumentListResponse.from(page);
    }

    @Transactional(readOnly = true)
    public AuditTrailResponse getAuditTrail(Long documentId, Pageable pageable) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        Page<AuditLog> entries = auditLogRepository.findByDocumentOrderByCreatedAtAsc(document, pageable);
        return AuditTrailResponse.from(documentId, entries);
    }


    @Transactional
    public Document startReview(Long documentId, User admin, String reason) {
        return executeTransition(documentId, DocumentEvent.START_REVIEW, admin, reason);
    }


    @Transactional
    public Document approve(Long documentId, User admin, String reason) {
        return executeTransition(documentId, DocumentEvent.APPROVE, admin, reason);
    }


    @Transactional
    public Document reject(Long documentId, User admin, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidReviewException("Rejection reason is required");
        }
        return executeTransition(documentId, DocumentEvent.REJECT, admin, reason);
    }


    private Document executeTransition(Long documentId, DocumentEvent event, User admin, String reason) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        DocumentStatus currentStatus = document.getStatus();

        StateMachine<DocumentStatus, DocumentEvent> stateMachine = createPrimedStateMachine(document);

        boolean accepted = stateMachine.sendEvent(event);
        if (!accepted) {
            log.warn("Illegal transition attempt: document {} in state {} sent event {}",
                    documentId, currentStatus, event);
            throw new IllegalDocumentTransitionException(
                    String.format("Cannot %s a document in state %s", event, currentStatus));
        }

        DocumentStatus newStatus = stateMachine.getState().getId();

        document.setStatus(newStatus);
        if (newStatus == DocumentStatus.REJECTED) {
            document.setRejectionReason(reason);
        }
        if (newStatus == DocumentStatus.VERIFIED || newStatus == DocumentStatus.REJECTED) {
            document.setReviewedBy(admin);
            document.setReviewedAt(Instant.now());
        }

        Document saved = documentRepository.save(document);
        log.info("Document {} transitioned {} -> {} by admin {}",
                documentId, currentStatus, newStatus, admin.getEmail());

        auditService.recordStateChange(saved, admin, currentStatus, newStatus, reason);

        return saved;
    }


    private StateMachine<DocumentStatus, DocumentEvent> createPrimedStateMachine(Document document) {
        StateMachine<DocumentStatus, DocumentEvent> stateMachine =
                stateMachineFactory.getStateMachine(UUID.randomUUID());

        stateMachine.stop();
        stateMachine.getStateMachineAccessor().doWithAllRegions(access ->
                access.resetStateMachine(new DefaultStateMachineContext<>(
                        document.getStatus(), null, null, null))
        );
        stateMachine.start();

        return stateMachine;
    }
}