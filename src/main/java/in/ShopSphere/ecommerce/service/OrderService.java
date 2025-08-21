package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.order.OrderRequest;
import in.ShopSphere.ecommerce.dto.order.OrderResponse;
import in.ShopSphere.ecommerce.model.entity.OrderStatus;
import in.ShopSphere.ecommerce.model.entity.PaymentStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    
    OrderResponse createOrder(OrderRequest request);
    
    OrderResponse getOrderById(String id);
    
    OrderResponse getOrderByNumber(String orderNumber);
    
    PaginationResponse<OrderResponse> getUserOrders(Pageable pageable);
    
    PaginationResponse<OrderResponse> getAllOrders(Pageable pageable);
    
    PaginationResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);
    
    PaginationResponse<OrderResponse> getOrdersByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);
    
    List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    List<OrderResponse> getOrdersByEstimatedDelivery(LocalDate date);
    
    List<OrderResponse> getOrdersByActualDelivery(LocalDate date);
    
    ApiResponse updateOrderStatus(String id, OrderStatus status);
    
    ApiResponse updatePaymentStatus(String id, PaymentStatus paymentStatus);
    
    ApiResponse updateDeliveryDate(String id, LocalDate deliveryDate);
    
    ApiResponse cancelOrder(String id);
    
    ApiResponse refundOrder(String id);
    
    ApiResponse addOrderNotes(String id, String notes);
     ApiResponse addOrderNote(String id, String notes);
    
    OrderResponse getOrderDetails(String id);
    
    PaginationResponse<OrderResponse> searchOrders(String searchTerm, Pageable pageable);
    
    PaginationResponse<OrderResponse> getOrdersByAmountRange(Double minAmount, Double maxAmount, Pageable pageable);
}
