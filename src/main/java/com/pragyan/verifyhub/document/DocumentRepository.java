package com.pragyan.verifyhub.document;

import com.pragyan.verifyhub.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByUser(User user, Pageable pageable);

    Optional<Document> findByIdAndUser(Long id, User user);

    boolean existsByUserAndContentHash(User user, String contentHash);

    Page<Document> findByStatus(DocumentStatus status, Pageable pageable);
}