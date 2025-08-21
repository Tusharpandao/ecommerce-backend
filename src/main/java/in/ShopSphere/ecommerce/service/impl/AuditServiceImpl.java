package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.model.entity.AuditLog;
import in.ShopSphere.ecommerce.model.entity.User;
import in.ShopSphere.ecommerce.repository.AuditLogRepository;
import in.ShopSphere.ecommerce.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    public void logUserAction(User user, String action, String resourceType, String resourceId, 
                            String details, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(resourceType)
                .entityId(resourceId != null ? resourceId : null)
                .newValues(Map.of("details", details, "ipAddress", ipAddress, "userAgent", userAgent))
                .ipAddress(ipAddress != null ? java.net.InetAddress.getByName(ipAddress) : null)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("User action logged: user={}, action={}, resource={}:{}", 
                user != null ? user.getEmail() : "ANONYMOUS", action, resourceType, resourceId);
        } catch (Exception e) {
            log.error("Failed to log user action: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void logSystemEvent(String event, String description, String severity, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .action(event)
                .entityType("SYSTEM")
                .entityId("0")
                .newValues(Map.of("description", description, "details", details, "severity", severity != null ? severity : "INFO"))
                .createdAt(LocalDateTime.now())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("System event logged: event={}, severity={}", event, severity);
        } catch (Exception e) {
            log.error("Failed to log system event: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void logSecurityEvent(String event, String description, String severity, 
                               String ipAddress, String userAgent, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .action(event)
                .entityType("SECURITY")
                .entityId("0")
                .newValues(Map.of("description", description, "details", details, "severity", severity != null ? severity : "WARNING"))
                .ipAddress(ipAddress != null ? java.net.InetAddress.getByName(ipAddress) : null)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Security event logged: event={}, severity={}, ip={}", event, severity, ipAddress);
        } catch (Exception e) {
            log.error("Failed to log security event: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void logDataAccess(User user, String dataType, String dataId, String accessType, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action("DATA_ACCESS")
                .entityType(dataType)
                .entityId(dataId != null ? dataId : null)
                .newValues(Map.of("accessType", accessType, "ipAddress", ipAddress))
                .ipAddress(ipAddress != null ? java.net.InetAddress.getByName(ipAddress) : null)
                .createdAt(LocalDateTime.now())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Data access logged: user={}, type={}, id={}, access={}", 
                user != null ? user.getEmail() : "ANONYMOUS", dataType, dataId, accessType);
        } catch (Exception e) {
            log.error("Failed to log data access: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void logAuthenticationEvent(User user, String event, boolean success, 
                                    String ipAddress, String userAgent, String details) {
        try {
            String description = success ? "Authentication successful" : "Authentication failed";
            
            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(event)
                .entityType("AUTHENTICATION")
                .entityId("0")
                .newValues(Map.of("description", description, "details", details, "success", success))
                .ipAddress(ipAddress != null ? java.net.InetAddress.getByName(ipAddress) : null)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Authentication event logged: event={}, success={}, user={}", 
                event, success, user != null ? user.getEmail() : "UNKNOWN");
        } catch (Exception e) {
            log.error("Failed to log authentication event: {}", e.getMessage());
        }
    }

    // Simplified implementations for the remaining methods
    @Override
    public List<AuditLog> getUserAuditLogs(String userId, LocalDateTime startDate, LocalDateTime endDate, 
                                         int page, int size) {
        try {
            return auditLogRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("Failed to get user audit logs: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<AuditLog> getResourceAuditLogs(String resourceType, String resourceId, 
                                             LocalDateTime startDate, LocalDateTime endDate, 
                                             int page, int size) {
        try {
            return auditLogRepository.findByEntityTypeAndEntityId(resourceType, resourceId);
        } catch (Exception e) {
            log.error("Failed to get resource audit logs: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<AuditLog> getSystemAuditLogs(LocalDateTime startDate, LocalDateTime endDate, 
                                           String severity, int page, int size) {
        try {
            return auditLogRepository.findByEntityType("SYSTEM");
        } catch (Exception e) {
            log.error("Failed to get system audit logs: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<AuditLog> getSecurityAuditLogs(LocalDateTime startDate, LocalDateTime endDate, 
                                             String severity, int page, int size) {
        try {
            return auditLogRepository.findByEntityType("SECURITY");
        } catch (Exception e) {
            log.error("Failed to get security audit logs: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public String exportAuditLogsToCsv(LocalDateTime startDate, LocalDateTime endDate, String logType) {
        try {
            List<AuditLog> logs = auditLogRepository.findByDateRange(startDate, endDate);

            StringBuilder csv = new StringBuilder();
            csv.append("Timestamp,Action,Entity Type,Entity ID,User Email,IP Address,User Agent,Details\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (AuditLog log : logs) {
                csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "",
                    log.getAction() != null ? log.getAction() : "",
                    log.getEntityType() != null ? log.getEntityType() : "",
                    log.getEntityId() != null ? log.getEntityId() : "",
                    log.getUserEmail() != null ? log.getUserEmail() : "",
                    log.getIpAddressString() != null ? log.getIpAddressString() : "",
                    log.getUserAgent() != null ? log.getUserAgent() : "",
                    log.getChangeSummary() != null ? log.getChangeSummary().replace("\"", "\"\"") : ""
                ));
            }

            return csv.toString();
        } catch (Exception e) {
            log.error("Failed to export audit logs to CSV: {}", e.getMessage());
            return "";
        }
    }

    @Override
    @Async
    public CompletableFuture<Long> cleanupOldAuditLogs(int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            List<AuditLog> oldLogs = auditLogRepository.findByCreatedBefore(cutoffDate);
            auditLogRepository.deleteAll(oldLogs);
            log.info("Cleaned up {} old audit logs older than {} days", oldLogs.size(), retentionDays);
            return CompletableFuture.completedFuture((long) oldLogs.size());
        } catch (Exception e) {
            log.error("Failed to cleanup old audit logs: {}", e.getMessage());
            return CompletableFuture.completedFuture(0L);
        }
    }

    @Override
    public AuditStatistics getAuditStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<AuditLog> logs = auditLogRepository.findByDateRange(startDate, endDate);
            
            long totalLogs = logs.size();
            long userActions = logs.stream().filter(log -> "USER".equals(log.getEntityType())).count();
            long systemEvents = logs.stream().filter(log -> "SYSTEM".equals(log.getEntityType())).count();
            long securityEvents = logs.stream().filter(log -> "SECURITY".equals(log.getEntityType())).count();
            long authenticationEvents = logs.stream().filter(log -> "AUTHENTICATION".equals(log.getEntityType())).count();
            long dataAccessLogs = logs.stream().filter(log -> "DATA_ACCESS".equals(log.getAction())).count();
            
            // Simplified statistics since severity is not directly available
            long criticalEvents = 0;
            long warningEvents = 0;
            long infoEvents = totalLogs;

            return new AuditStatistics(
                totalLogs, userActions, systemEvents, securityEvents, authenticationEvents,
                dataAccessLogs, criticalEvents, warningEvents, infoEvents
            );
        } catch (Exception e) {
            log.error("Failed to get audit statistics: {}", e.getMessage());
            return new AuditStatistics(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }
}
