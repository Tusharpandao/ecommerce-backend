package in.ShopSphere.ecommerce.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_logs_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_logs_action", columnList = "action")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @NotBlank(message = "Action is required")
    @Size(max = 100, message = "Action must not exceed 100 characters")
    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.
    
    @NotBlank(message = "Entity type is required")
    @Size(max = 100, message = "Entity type must not exceed 100 characters")
    @Column(name = "entity_type", nullable = false)
    private String entityType; // User, Product, Order, etc.
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private Map<String, Object> oldValues;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private Map<String, Object> newValues;
    
    @Column(name = "ip_address")
    private InetAddress ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Business logic methods
    public String getUserEmail() {
        return user != null ? user.getEmail() : "System";
    }
    
    public String getUserFullName() {
        return user != null ? user.getFullName() : "System";
    }
    
    public String getActionDescription() {
        return switch (action.toUpperCase()) {
            case "CREATE" -> "Created";
            case "UPDATE" -> "Updated";
            case "DELETE" -> "Deleted";
            case "LOGIN" -> "Logged in";
            case "LOGOUT" -> "Logged out";
            case "BLOCK" -> "Blocked";
            case "UNBLOCK" -> "Unblocked";
            case "APPROVE" -> "Approved";
            case "REJECT" -> "Rejected";
            case "SHIP" -> "Shipped";
            case "DELIVER" -> "Delivered";
            case "CANCEL" -> "Cancelled";
            default -> action;
        };
    }
    
    public boolean isCreateAction() {
        return "CREATE".equalsIgnoreCase(action);
    }
    
    public boolean isUpdateAction() {
        return "UPDATE".equalsIgnoreCase(action);
    }
    
    public boolean isDeleteAction() {
        return "DELETE".equalsIgnoreCase(action);
    }
    
    public boolean hasChanges() {
        return (oldValues != null && !oldValues.isEmpty()) || 
               (newValues != null && !newValues.isEmpty());
    }
    
    public String getChangeSummary() {
        if (!hasChanges()) {
            return "No changes recorded";
        }
        
        StringBuilder summary = new StringBuilder();
        if (oldValues != null && newValues != null) {
            summary.append("Modified fields: ");
            newValues.keySet().stream()
                    .filter(key -> !key.equals("updatedAt") && !key.equals("lastModifiedDate"))
                    .forEach(key -> {
                        Object oldValue = oldValues.get(key);
                        Object newValue = newValues.get(key);
                        if (oldValue != null && !oldValue.equals(newValue)) {
                            summary.append(key).append(", ");
                        }
                    });
            if (summary.toString().endsWith(", ")) {
                summary.setLength(summary.length() - 2);
            }
        } else if (isCreateAction()) {
            summary.append("Created new ").append(entityType.toLowerCase());
        } else if (isDeleteAction()) {
            summary.append("Deleted ").append(entityType.toLowerCase());
        }
        
        return summary.toString();
    }
    
    public String getIpAddressString() {
        return ipAddress != null ? ipAddress.getHostAddress() : "Unknown";
    }
}
