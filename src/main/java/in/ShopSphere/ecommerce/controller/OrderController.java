package in.ShopSphere.ecommerce.controller;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.order.OrderRequest;
import in.ShopSphere.ecommerce.dto.order.OrderResponse;
import in.ShopSphere.ecommerce.model.entity.OrderStatus;
import in.ShopSphere.ecommerce.model.entity.PaymentStatus;
import in.ShopSphere.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create order", description = "Creates a new order from the current user's cart.")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request) {
        log.info("Creating order for current user");
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID.")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order by number", description = "Retrieves an order by its order number.")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @Parameter(description = "Order number") @PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user orders", description = "Retrieves orders for the current user.")
    public ResponseEntity<PaginationResponse<OrderResponse>> getUserOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse<OrderResponse> response = orderService.getUserOrders(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders", description = "Retrieves all orders. Admin only.")
    public ResponseEntity<PaginationResponse<OrderResponse>> getAllOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse<OrderResponse> response = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by status", description = "Retrieves orders by status. Admin only.")
    public ResponseEntity<PaginationResponse<OrderResponse>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse<OrderResponse> response = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment-status/{paymentStatus}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by payment status", description = "Retrieves orders by payment status. Admin only.")
    public ResponseEntity<PaginationResponse<OrderResponse>> getOrdersByPaymentStatus(
            @Parameter(description = "Payment status") @PathVariable PaymentStatus paymentStatus,
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse<OrderResponse> response = orderService.getOrdersByPaymentStatus(paymentStatus, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by date range", description = "Retrieves orders within a date range. Admin only.")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<OrderResponse> response = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estimated-delivery/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by estimated delivery date", description = "Retrieves orders by estimated delivery date. Admin only.")
    public ResponseEntity<List<OrderResponse>> getOrdersByEstimatedDelivery(
            @Parameter(description = "Estimated delivery date") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<OrderResponse> response = orderService.getOrdersByEstimatedDelivery(date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/actual-delivery/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by actual delivery date", description = "Retrieves orders by actual delivery date. Admin only.")
    public ResponseEntity<List<OrderResponse>> getOrdersByActualDelivery(
            @Parameter(description = "Actual delivery date") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<OrderResponse> response = orderService.getOrdersByActualDelivery(date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search orders", description = "Searches orders by various criteria. Admin only.")
    public ResponseEntity<PaginationResponse<OrderResponse>> searchOrders(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse<OrderResponse> response = orderService.searchOrders(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Update order status", description = "Updates the status of an order. Admin/Seller only.")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam OrderStatus status) {
        log.info("Updating order status: orderId={}, status={}", id, status);
        ApiResponse response = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/payment-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Update payment status", description = "Updates the payment status of an order. Admin/Seller only.")
    public ResponseEntity<ApiResponse> updatePaymentStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "New payment status") @RequestParam PaymentStatus paymentStatus) {
        log.info("Updating payment status: orderId={}, paymentStatus={}", id, paymentStatus);
        ApiResponse response = orderService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/delivery-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Update delivery date", description = "Updates the estimated delivery date of an order. Admin/Seller only.")
    public ResponseEntity<ApiResponse> updateDeliveryDate(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "New delivery date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate) {
        log.info("Updating delivery date: orderId={}, date={}", id, deliveryDate);
        ApiResponse response = orderService.updateDeliveryDate(id, deliveryDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel order", description = "Cancels an order.")
    public ResponseEntity<ApiResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        log.info("Cancelling order: orderId={}", id);
        ApiResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund order", description = "Refunds an order. Admin only.")
    public ResponseEntity<ApiResponse> refundOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        log.info("Refunding order: orderId={}", id);
        ApiResponse response = orderService.refundOrder(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add order notes", description = "Adds notes to an order.")
    public ResponseEntity<ApiResponse> addOrderNotes(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Notes") @RequestParam String notes) {
        log.info("Adding notes to order: orderId={}, notes={}", id, notes);
        ApiResponse response = orderService.addOrderNotes(id, notes);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order details", description = "Retrieves detailed information about an order.")
    public ResponseEntity<OrderResponse> getOrderDetails(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        OrderResponse response = orderService.getOrderDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/amount-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by amount range", description = "Retrieves orders within an amount range. Admin only.")
    public ResponseEntity<PaginationResponse<OrderResponse>> getOrdersByAmountRange(
            @Parameter(description = "Minimum amount") @RequestParam Double minAmount,
            @Parameter(description = "Maximum amount") @RequestParam Double maxAmount,
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse<OrderResponse> response = orderService.getOrdersByAmountRange(minAmount, maxAmount, pageable);
        return ResponseEntity.ok(response);
    }
}
