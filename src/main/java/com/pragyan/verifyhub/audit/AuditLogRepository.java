package com.pragyan.verifyhub.audit;

import com.pragyan.verifyhub.document.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByDocumentOrderByCreatedAtAsc(Document document, Pageable pageable);
}