package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.model.entity.User;

public interface WebSocketService {
    
    /**
     * Send notification to a specific user
     * @param userId user ID to send notification to
     * @param type notification type
     * @param message notification message
     * @param data additional data payload
     */
    void sendNotificationToUser(Long userId, String type, String message, Object data);
    
    /**
     * Send notification to all users with a specific role
     * @param role user role to send notification to
     * @param type notification type
     * @param message notification message
     * @param data additional data payload
     */
    void sendNotificationToRole(String role, String type, String message, Object data);
    
    /**
     * Send notification to all connected users
     * @param type notification type
     * @param message notification message
     * @param data additional data payload
     */
    void sendNotificationToAll(String type, String message, Object data);
    
    /**
     * Send order placed notification
     * @param orderNumber order number
     * @param userId user ID who placed the order
     * @param orderData order details
     */
    void notifyOrderPlaced(String orderNumber, Long userId, Object orderData);
    
    /**
     * Send order status update notification
     * @param orderNumber order number
     * @param userId user ID
     * @param oldStatus previous status
     * @param newStatus new status
     * @param orderData order details
     */
    void notifyOrderStatusUpdated(String orderNumber, Long userId, String oldStatus, String newStatus, Object orderData);
    
    /**
     * Send sales update notification to sellers
     * @param sellerId seller ID
     * @param salesData sales information
     */
    void notifySalesUpdated(Long sellerId, Object salesData);
    
    /**
     * Send product update notification
     * @param productId product ID
     * @param updateType type of update
     * @param productData product details
     */
    void notifyProductUpdated(Long productId, String updateType, Object productData);
    
    /**
     * Send low stock alert to sellers
     * @param productId product ID
     * @param currentStock current stock level
     * @param threshold threshold for low stock
     */
    void notifyLowStockAlert(Long productId, Integer currentStock, Integer threshold);
    
    /**
     * Send system maintenance notification
     * @param message maintenance message
     * @param scheduledTime scheduled maintenance time
     */
    void notifySystemMaintenance(String message, String scheduledTime);
    
    /**
     * Check if a user is currently connected
     * @param userId user ID to check
     * @return true if user is connected, false otherwise
     */
    boolean isUserConnected(Long userId);
    
    /**
     * Get count of connected users
     * @return number of connected users
     */
    long getConnectedUserCount();
    
    /**
     * Get count of connected users by role
     * @param role user role
     * @return number of connected users with the specified role
     */
    long getConnectedUserCountByRole(String role);
}
