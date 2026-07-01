package com.pragyan.verifyhub.audit;

import com.pragyan.verifyhub.document.Document;
import com.pragyan.verifyhub.document.DocumentStatus;
import com.pragyan.verifyhub.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    private static final int USER_AGENT_MAX_LENGTH = 500;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUpload(Document document, User actor) {
        AuditLog entry = baseBuilder(document, actor, AuditAction.UPLOADED)
                .newStatus(document.getStatus())
                .build();
        auditLogRepository.save(entry);
        log.debug("Audit: UPLOADED document {} by {}", document.getId(), actor.getEmail());
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordStateChange(Document document, User actor,
                                  DocumentStatus oldStatus, DocumentStatus newStatus,
                                  String reason) {
        AuditLog entry = baseBuilder(document, actor, AuditAction.STATE_CHANGED)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();
        auditLogRepository.save(entry);
        log.info("Audit: STATE_CHANGED document {} {} -> {} by {}",
                document.getId(), oldStatus, newStatus, actor.getEmail());
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordView(Document document, User actor) {
        AuditLog entry = baseBuilder(document, actor, AuditAction.VIEWED).build();
        auditLogRepository.save(entry);
        log.debug("Audit: VIEWED document {} by {}", document.getId(), actor.getEmail());
    }


    private AuditLog.AuditLogBuilder baseBuilder(Document document, User actor, AuditAction action) {
        RequestContext ctx = currentRequestContext();
        return AuditLog.builder()
                .document(document)
                .actor(actor)
                .action(action)
                .ipAddress(ctx.ipAddress())
                .userAgent(ctx.userAgent());
    }

    private RequestContext currentRequestContext() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(this::extractContext)
                .orElse(new RequestContext(null, null));
    }

    private RequestContext extractContext(HttpServletRequest request) {
        String ip = resolveClientIp(request);
        String ua = truncate(request.getHeader("User-Agent"), USER_AGENT_MAX_LENGTH);
        return new RequestContext(ip, ua);
    }


    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private record RequestContext(String ipAddress, String userAgent) {}
}