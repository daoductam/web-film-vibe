package com.tamdao.web_film_backend.aspect;

import com.tamdao.web_film_backend.entity.AuditLog;
import com.tamdao.web_film_backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning("execution(* com.tamdao.web_film_backend.controller.AdminController.*(..))")
    public void logAdminAction(JoinPoint joinPoint) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.getName() != null) ? auth.getName() : "system";

            String methodName = joinPoint.getSignature().getName();
            
            AuditLog auditLog = AuditLog.builder()
                    .action("ADMIN_ACTION: " + methodName)
                    .entityType("N/A")
                    .entityId("N/A")
                    .performedBy(username)
                    .timestamp(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created for action {} by user {}", methodName, username);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }
    }
}
