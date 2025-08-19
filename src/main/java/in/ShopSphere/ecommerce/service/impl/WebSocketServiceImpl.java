package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    private static final String USER_DESTINATION_PREFIX = "/user";
    private static final String ROLE_DESTINATION_PREFIX = "/topic/role";
    private static final String BROADCAST_DESTINATION_PREFIX = "/topic/broadcast";
    private static final String ORDER_DESTINATION_PREFIX = "/topic/orders";
    private static final String SALES_DESTINATION_PREFIX = "/topic/sales";
    private static final String PRODUCT_DESTINATION_PREFIX = "/topic/products";
    private static final String ALERT_DESTINATION_PREFIX = "/topic/alerts";
    private static final String SYSTEM_DESTINATION_PREFIX = "/topic/system";

    @Override
    public void sendNotificationToUser(Long userId, String type, String message, Object data) {
        if (userId == null || type == null || message == null) {
            log.warn("Cannot send notification: userId={}, type={}, message={}", userId, type, message);
            return;
        }

        String destination = USER_DESTINATION_PREFIX + "/" + userId + "/notifications";
        Map<String, Object> payload = createNotificationPayload(type, message, data);
        
        try {
            messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
            log.debug("Notification sent to user {}: type={}, message={}", userId, type, message);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendNotificationToRole(String role, String type, String message, Object data) {
        if (role == null || type == null || message == null) {
            log.warn("Cannot send notification: role={}, type={}, message={}", role, type, message);
            return;
        }

        String destination = ROLE_DESTINATION_PREFIX + "/" + role;
        Map<String, Object> payload = createNotificationPayload(type, message, data);
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Notification sent to role {}: type={}, message={}", role, type, message);
        } catch (Exception e) {
            log.error("Failed to send notification to role {}: {}", role, e.getMessage());
        }
    }

    @Override
    public void sendNotificationToAll(String type, String message, Object data) {
        if (type == null || message == null) {
            log.warn("Cannot send notification: type={}, message={}", type, message);
            return;
        }

        String destination = BROADCAST_DESTINATION_PREFIX;
        Map<String, Object> payload = createNotificationPayload(type, message, data);
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Broadcast notification sent: type={}, message={}", type, message);
        } catch (Exception e) {
            log.error("Failed to send broadcast notification: {}", e.getMessage());
        }
    }

    @Override
    public void notifyOrderPlaced(String orderNumber, Long userId, Object orderData) {
        if (orderNumber == null || userId == null) {
            log.warn("Cannot notify order placed: orderNumber={}, userId={}", orderNumber, userId);
            return;
        }

        // Notify the user who placed the order
        sendNotificationToUser(userId, "ORDER_PLACED", 
            "Your order " + orderNumber + " has been placed successfully", orderData);

        // Notify admins and sellers about new order
        Map<String, Object> adminPayload = createNotificationPayload("NEW_ORDER", 
            "New order placed: " + orderNumber, orderData);
        
        try {
            messagingTemplate.convertAndSend(ORDER_DESTINATION_PREFIX + "/new", adminPayload);
            log.debug("Order placed notification sent: orderNumber={}, userId={}", orderNumber, userId);
        } catch (Exception e) {
            log.error("Failed to send order placed notification: {}", e.getMessage());
        }
    }

    @Override
    public void notifyOrderStatusUpdated(String orderNumber, Long userId, String oldStatus, String newStatus, Object orderData) {
        if (orderNumber == null || userId == null || oldStatus == null || newStatus == null) {
            log.warn("Cannot notify order status update: orderNumber={}, userId={}, oldStatus={}, newStatus={}", 
                orderNumber, userId, oldStatus, newStatus);
            return;
        }

        // Notify the user about status change
        String message = String.format("Your order %s status has been updated from %s to %s", 
            orderNumber, oldStatus, newStatus);
        sendNotificationToUser(userId, "ORDER_STATUS_UPDATED", message, orderData);

        // Notify admins and sellers about status change
        Map<String, Object> adminPayload = createNotificationPayload("ORDER_STATUS_CHANGED", 
            String.format("Order %s status changed: %s → %s", orderNumber, oldStatus, newStatus), orderData);
        
        try {
            messagingTemplate.convertAndSend(ORDER_DESTINATION_PREFIX + "/status-update", adminPayload);
            log.debug("Order status update notification sent: orderNumber={}, oldStatus={}, newStatus={}", 
                orderNumber, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Failed to send order status update notification: {}", e.getMessage());
        }
    }

    @Override
    public void notifySalesUpdated(Long sellerId, Object salesData) {
        if (sellerId == null) {
            log.warn("Cannot notify sales update: sellerId={}", sellerId);
            return;
        }

        String destination = SALES_DESTINATION_PREFIX + "/" + sellerId;
        Map<String, Object> payload = createNotificationPayload("SALES_UPDATED", 
            "Your sales have been updated", salesData);
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Sales update notification sent to seller: {}", sellerId);
        } catch (Exception e) {
            log.error("Failed to send sales update notification to seller {}: {}", sellerId, e.getMessage());
        }
    }

    @Override
    public void notifyProductUpdated(Long productId, String updateType, Object productData) {
        if (productId == null || updateType == null) {
            log.warn("Cannot notify product update: productId={}, updateType={}", productId, updateType);
            return;
        }

        String destination = PRODUCT_DESTINATION_PREFIX + "/" + productId;
        Map<String, Object> payload = createNotificationPayload("PRODUCT_UPDATED", 
            "Product has been " + updateType.toLowerCase(), productData);
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Product update notification sent: productId={}, updateType={}", productId, updateType);
        } catch (Exception e) {
            log.error("Failed to send product update notification: {}", e.getMessage());
        }
    }

    @Override
    public void notifyLowStockAlert(Long productId, Integer currentStock, Integer threshold) {
        if (productId == null || currentStock == null || threshold == null) {
            log.warn("Cannot notify low stock alert: productId={}, currentStock={}, threshold={}", 
                productId, currentStock, threshold);
            return;
        }

        String destination = ALERT_DESTINATION_PREFIX + "/low-stock";
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("productId", productId);
        alertData.put("currentStock", currentStock);
        alertData.put("threshold", threshold);
        alertData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Map<String, Object> payload = createNotificationPayload("LOW_STOCK_ALERT", 
            String.format("Product %d is running low on stock (Current: %d, Threshold: %d)", 
                productId, currentStock, threshold), alertData);
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Low stock alert sent: productId={}, currentStock={}, threshold={}", 
                productId, currentStock, threshold);
        } catch (Exception e) {
            log.error("Failed to send low stock alert: {}", e.getMessage());
        }
    }

    @Override
    public void notifySystemMaintenance(String message, String scheduledTime) {
        if (message == null) {
            log.warn("Cannot notify system maintenance: message={}", message);
            return;
        }

        String destination = SYSTEM_DESTINATION_PREFIX + "/maintenance";
        Map<String, Object> maintenanceData = new HashMap<>();
        maintenanceData.put("message", message);
        maintenanceData.put("scheduledTime", scheduledTime);
        maintenanceData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Map<String, Object> payload = createNotificationPayload("SYSTEM_MAINTENANCE", 
            "System maintenance scheduled", maintenanceData);
        
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("System maintenance notification sent: message={}, scheduledTime={}", message, scheduledTime);
        } catch (Exception e) {
            log.error("Failed to send system maintenance notification: {}", e.getMessage());
        }
    }

    @Override
    public boolean isUserConnected(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            return userRegistry.getUser(userId.toString()) != null;
        } catch (Exception e) {
            log.error("Failed to check if user {} is connected: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public long getConnectedUserCount() {
        try {
            return userRegistry.getUserCount();
        } catch (Exception e) {
            log.error("Failed to get connected user count: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long getConnectedUserCountByRole(String role) {
        if (role == null) {
            return 0;
        }
        
        try {
            return userRegistry.getUsers().stream()
                .filter(user -> user.getPrincipal() != null && 
                    user.getPrincipal().getName() != null && 
                    user.getPrincipal().getName().contains("ROLE_" + role))
                .count();
        } catch (Exception e) {
            log.error("Failed to get connected user count by role {}: {}", role, e.getMessage());
            return 0;
        }
    }

    private Map<String, Object> createNotificationPayload(String type, String message, Object data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("message", message);
        payload.put("data", data);
        payload.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return payload;
    }
}
