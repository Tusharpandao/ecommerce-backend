package in.ShopSphere.ecommerce.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SocketIOServer socketIOServer;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        log.info("Client connected: {}", sessionId);
        
        // Send welcome message
        Map<String, Object> welcomeMessage = new HashMap<>();
        welcomeMessage.put("type", "CONNECTION_ESTABLISHED");
        welcomeMessage.put("sessionId", sessionId);
        welcomeMessage.put("timestamp", LocalDateTime.now().toString());
        welcomeMessage.put("message", "Connected to E-Commerce WebSocket server");
        
        client.sendEvent("welcome", welcomeMessage);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        log.info("Client disconnected: {}", sessionId);
    }

    @OnEvent("join_room")
    public void onJoinRoom(SocketIOClient client, Map<String, String> data) {
        String room = data.get("room");
        String userId = data.get("userId");
        
        if (room != null && userId != null) {
            client.joinRoom(room);
            log.info("User {} joined room: {}", userId, room);
            
            // Notify room about new user
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "USER_JOINED_ROOM");
            notification.put("userId", userId);
            notification.put("room", room);
            notification.put("timestamp", LocalDateTime.now().toString());
            
            socketIOServer.getRoomOperations(room).sendEvent("room_notification", notification);
        }
    }

    @OnEvent("leave_room")
    public void onLeaveRoom(SocketIOClient client, Map<String, String> data) {
        String room = data.get("room");
        String userId = data.get("userId");
        
        if (room != null && userId != null) {
            client.leaveRoom(room);
            log.info("User {} left room: {}", userId, room);
            
            // Notify room about user leaving
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "USER_LEFT_ROOM");
            notification.put("userId", userId);
            notification.put("room", room);
            notification.put("timestamp", LocalDateTime.now().toString());
            
            socketIOServer.getRoomOperations(room).sendEvent("room_notification", notification);
        }
    }

    @OnEvent("order_update")
    public void onOrderUpdate(SocketIOClient client, Map<String, Object> orderData) {
        String orderId = (String) orderData.get("orderId");
        String status = (String) orderData.get("status");
        String userId = (String) orderData.get("userId");
        
        log.info("Order update: {} - Status: {} by User: {}", orderId, status, userId);
        
        // Broadcast to all clients interested in orders
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ORDER_UPDATE");
        notification.put("orderId", orderId);
        notification.put("status", status);
        notification.put("updatedBy", userId);
        notification.put("timestamp", LocalDateTime.now().toString());
        
        socketIOServer.getBroadcastOperations().sendEvent("order_notification", notification);
        
        // Send to specific user room if available
        if (userId != null) {
            socketIOServer.getRoomOperations("user_" + userId).sendEvent("order_update", notification);
        }
    }

    @OnEvent("product_update")
    public void onProductUpdate(SocketIOClient client, Map<String, Object> productData) {
        String productId = (String) productData.get("productId");
        String action = (String) productData.get("action");
        String userId = (String) productData.get("userId");
        
        log.info("Product update: {} - Action: {} by User: {}", productId, action, userId);
        
        // Broadcast to all clients interested in products
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "PRODUCT_UPDATE");
        notification.put("productId", productId);
        notification.put("action", action);
        notification.put("updatedBy", userId);
        notification.put("timestamp", LocalDateTime.now().toString());
        
        socketIOServer.getBroadcastOperations().sendEvent("product_notification", notification);
    }

    @OnEvent("notification")
    public void onNotification(SocketIOClient client, Map<String, String> notificationData) {
        String userId = notificationData.get("userId");
        String title = notificationData.get("title");
        String message = notificationData.get("message");
        
        log.info("Notification for user {}: {} - {}", userId, title, message);
        
        // Send notification to specific user
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NOTIFICATION");
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now().toString());
        notification.put("id", UUID.randomUUID().toString());
        
        if (userId != null) {
            socketIOServer.getRoomOperations("user_" + userId).sendEvent("notification", notification);
        }
    }

    @OnEvent("chat_message")
    public void onChatMessage(SocketIOClient client, Map<String, String> messageData) {
        String room = messageData.get("room");
        String userId = messageData.get("userId");
        String content = messageData.get("content");
        
        log.info("Chat message in room {} from user {}: {}", room, userId, content);
        
        // Broadcast message to room
        Map<String, Object> message = new HashMap<>();
        message.put("type", "CHAT_MESSAGE");
        message.put("userId", userId);
        message.put("content", content);
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("messageId", UUID.randomUUID().toString());
        
        if (room != null) {
            socketIOServer.getRoomOperations(room).sendEvent("chat_message", message);
        }
    }

    // Method to send order status updates (called from services)
    public void sendOrderStatusUpdate(String orderId, String status, String userId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ORDER_STATUS_UPDATE");
        notification.put("orderId", orderId);
        notification.put("status", status);
        notification.put("updatedBy", userId);
        notification.put("timestamp", LocalDateTime.now().toString());
        
        socketIOServer.getBroadcastOperations().sendEvent("order_status_update", notification);
        
        // Send to specific user room
        if (userId != null) {
            socketIOServer.getRoomOperations("user_" + userId).sendEvent("order_status_update", notification);
        }
    }

    // Method to send product updates (called from services)
    public void sendProductUpdate(String productId, String action, String userId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "PRODUCT_UPDATE");
        notification.put("productId", productId);
        notification.put("action", action);
        notification.put("updatedBy", userId);
        notification.put("timestamp", LocalDateTime.now().toString());
        
        socketIOServer.getBroadcastOperations().sendEvent("product_update", notification);
    }

    // Method to send system notifications
    public void sendSystemNotification(String userId, String title, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "SYSTEM_NOTIFICATION");
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now().toString());
        notification.put("id", UUID.randomUUID().toString());
        
        if (userId != null) {
            socketIOServer.getRoomOperations("user_" + userId).sendEvent("system_notification", notification);
        }
    }
}
