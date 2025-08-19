package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.AuditLog;
import in.ShopSphere.ecommerce.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    
    List<AuditLog> findByUser(User user);
    
    List<AuditLog> findByUserId(Long userId);
    
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
    
    List<AuditLog> findByAction(String action);
    
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    List<AuditLog> findByEntityType(String entityType);
    
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    
    List<AuditLog> findByEntityId(Long entityId);
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = :action")
    List<AuditLog> findByUserIdAndAction(@Param("userId") Long userId, @Param("action") String action);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.entityType = :entityType")
    List<AuditLog> findByUserIdAndEntityType(@Param("userId") Long userId, @Param("entityType") String entityType);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.entityId = :entityId")
    List<AuditLog> findByUserIdAndEntityId(@Param("userId") Long userId, @Param("entityId") Long entityId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt >= :startDate")
    List<AuditLog> findByCreatedAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt <= :endDate")
    List<AuditLog> findByCreatedBefore(@Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.createdAt >= :startDate")
    List<AuditLog> findByActionAndCreatedAfter(@Param("action") String action, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.createdAt >= :startDate")
    List<AuditLog> findByEntityTypeAndCreatedAfter(@Param("entityType") String entityType, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.createdAt >= :startDate")
    List<AuditLog> findByUserIdAndCreatedAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.createdAt BETWEEN :startDate AND :endDate")
    List<AuditLog> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress")
    List<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress);
    
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.createdAt >= :startDate")
    List<AuditLog> findByIpAddressAndCreatedAfter(@Param("ipAddress") String ipAddress, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.userAgent LIKE %:userAgent%")
    List<AuditLog> findByUserAgentContaining(@Param("userAgent") String userAgent);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    Page<AuditLog> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    List<AuditLog> findEntityHistory(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action IN :actions")
    Page<AuditLog> findByActions(@Param("actions") List<String> actions, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityType IN :entityTypes")
    Page<AuditLog> findByEntityTypes(@Param("entityTypes") List<String> entityTypes, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action IN :actions")
    List<AuditLog> findByUserIdAndActions(@Param("userId") Long userId, @Param("actions") List<String> actions);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.entityType IN :entityTypes")
    List<AuditLog> findByUserIdAndEntityTypes(@Param("userId") Long userId, @Param("entityTypes") List<String> entityTypes);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId AND a.action = :action AND a.createdAt >= :startDate")
    long countByUserIdAndActionAndDateRange(@Param("userId") Long userId, @Param("action") String action, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId")
    long countByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.createdAt >= :startDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findRecentActivityByUser(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, Pageable pageable);
}
