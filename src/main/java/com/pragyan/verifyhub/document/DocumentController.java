package com.pragyan.verifyhub.document;

import com.pragyan.verifyhub.document.dto.DocumentListResponse;
import com.pragyan.verifyhub.document.dto.DocumentSummary;
import com.pragyan.verifyhub.document.dto.UploadResponse;
import com.pragyan.verifyhub.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType
    ) {
        log.info("Upload requested by user {} for type {}", user.getEmail(), documentType);
        Document saved = documentService.upload(user, file, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(UploadResponse.success(saved));
    }

    @GetMapping
    public ResponseEntity<DocumentListResponse> list(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        DocumentListResponse response = documentService.listForUser(user, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentSummary> getById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        DocumentSummary doc = documentService.getForUser(user, id);
        return ResponseEntity.ok(doc);
    }
}