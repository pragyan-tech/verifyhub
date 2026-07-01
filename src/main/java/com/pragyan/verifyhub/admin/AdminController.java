package com.pragyan.verifyhub.admin;

import com.pragyan.verifyhub.admin.dto.AuditTrailResponse;
import com.pragyan.verifyhub.admin.dto.ReviewRequest;
import com.pragyan.verifyhub.document.Document;
import com.pragyan.verifyhub.document.dto.DocumentListResponse;
import com.pragyan.verifyhub.document.dto.DocumentSummary;
import com.pragyan.verifyhub.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminDocumentService adminDocumentService;

    @GetMapping("/pending")
    public ResponseEntity<DocumentListResponse> listPending(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminDocumentService.listPendingDocuments(pageable));
    }

    @PostMapping("/{id}/start-review")
    public ResponseEntity<DocumentSummary> startReview(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody(required = false) ReviewRequest request
    ) {
        String reason = request != null ? request.getReason() : null;
        Document updated = adminDocumentService.startReview(id, admin, reason);
        return ResponseEntity.ok(DocumentSummary.from(updated));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<DocumentSummary> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody(required = false) ReviewRequest request
    ) {
        String reason = request != null ? request.getReason() : null;
        Document updated = adminDocumentService.approve(id, admin, reason);
        return ResponseEntity.ok(DocumentSummary.from(updated));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<DocumentSummary> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody ReviewRequest request
    ) {
        Document updated = adminDocumentService.reject(id, admin, request.getReason());
        return ResponseEntity.ok(DocumentSummary.from(updated));
    }

    @GetMapping("/{id}/audit-trail")
    public ResponseEntity<AuditTrailResponse> getAuditTrail(
            @PathVariable Long id,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminDocumentService.getAuditTrail(id, pageable));
    }
}