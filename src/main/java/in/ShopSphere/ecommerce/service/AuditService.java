package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.model.entity.AuditLog;
import in.ShopSphere.ecommerce.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AuditService {
    
    /**
     * Log user action
     * @param user user performing the action
     * @param action action performed
     * @param resourceType type of resource affected
     * @param resourceId ID of resource affected
     * @param details additional details
     * @param ipAddress IP address of the user
     * @param userAgent user agent string
     */
    void logUserAction(User user, String action, String resourceType, String resourceId, 
                      String details, String ipAddress, String userAgent);
    
    /**
     * Log system event
     * @param event event type
     * @param description event description
     * @param severity event severity
     * @param details additional details
     */
    void logSystemEvent(String event, String description, String severity, String details);
    
    /**
     * Log security event
     * @param event security event type
     * @param description event description
     * @param severity security level
     * @param ipAddress IP address involved
     * @param userAgent user agent involved
     * @param details additional details
     */
    void logSecurityEvent(String event, String description, String severity, 
                         String ipAddress, String userAgent, String details);
    
    /**
     * Log data access
     * @param user user accessing data
     * @param dataType type of data accessed
     * @param dataId ID of data accessed
     * @param accessType type of access (READ, WRITE, DELETE)
     * @param ipAddress IP address of the user
     */
    void logDataAccess(User user, String dataType, String dataId, String accessType, String ipAddress);
    
    /**
     * Log authentication event
     * @param user user involved (can be null for failed attempts)
     * @param event authentication event type
     * @param success whether authentication was successful
     * @param ipAddress IP address involved
     * @param userAgent user agent involved
     * @param details additional details
     */
    void logAuthenticationEvent(User user, String event, boolean success, 
                              String ipAddress, String userAgent, String details);
    
    /**
     * Get audit logs for a specific user
     * @param userId user ID
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @param page page number
     * @param size page size
     * @return paginated audit logs
     */
    List<AuditLog> getUserAuditLogs(String userId, LocalDateTime startDate, LocalDateTime endDate, 
                                   int page, int size);
    
    /**
     * Get audit logs for a specific resource
     * @param resourceType resource type
     * @param resourceId resource ID
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @param page page number
     * @param size page size
     * @return paginated audit logs
     */
    List<AuditLog> getResourceAuditLogs(String resourceType, String resourceId, 
                                       LocalDateTime startDate, LocalDateTime endDate, 
                                       int page, int size);
    
    /**
     * Get system audit logs
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @param severity severity level filter
     * @param page page number
     * @param size page size
     * @return paginated audit logs
     */
    List<AuditLog> getSystemAuditLogs(LocalDateTime startDate, LocalDateTime endDate, 
                                     String severity, int page, int size);
    
    /**
     * Get security audit logs
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @param severity security level filter
     * @param page page number
     * @param size page size
     * @return paginated audit logs
     */
    List<AuditLog> getSecurityAuditLogs(LocalDateTime startDate, LocalDateTime endDate, 
                                       String severity, int page, int size);
    
    /**
     * Export audit logs to CSV
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @param logType type of logs to export
     * @return CSV content as string
     */
    String exportAuditLogsToCsv(LocalDateTime startDate, LocalDateTime endDate, String logType);
    
    /**
     * Clean up old audit logs
     * @param retentionDays number of days to retain logs
     * @return number of logs cleaned up
     */
    CompletableFuture<Long> cleanupOldAuditLogs(int retentionDays);
    
    /**
     * Get audit statistics
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @return audit statistics
     */
    AuditStatistics getAuditStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Audit statistics data class
     */
    record AuditStatistics(
        long totalLogs,
        long userActions,
        long systemEvents,
        long securityEvents,
        long authenticationEvents,
        long dataAccessLogs,
        long criticalEvents,
        long warningEvents,
        long infoEvents
    ) {}
}
