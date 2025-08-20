package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.order.OrderRequest;
import in.ShopSphere.ecommerce.dto.order.OrderResponse;
import in.ShopSphere.ecommerce.exception.BusinessException;
import in.ShopSphere.ecommerce.exception.ResourceNotFoundException;
import in.ShopSphere.ecommerce.mapper.OrderMapper;
import in.ShopSphere.ecommerce.model.entity.*;
import in.ShopSphere.ecommerce.repository.*;
import in.ShopSphere.ecommerce.controller.WebSocketController;
import in.ShopSphere.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;
    private final WebSocketController webSocketController;

    @Override
    @CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user");
        
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUser(currentUser)
            .orElseThrow(() -> new BusinessException("Cart is empty"));
        
        if (cart.isEmpty()) {
            throw new BusinessException("Cannot create order from empty cart");
        }
        
        // Validate addresses
        Address shippingAddress = addressRepository.findById(request.getShippingAddressId())
            .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));
        
        Address billingAddress = null;
        if (request.getBillingAddressId() != null) {
            billingAddress = addressRepository.findById(request.getBillingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Billing address not found"));
        } else {
            billingAddress = shippingAddress; // Use shipping address as billing address
        }
        
        // Validate cart items availability
        validateCartItems(cart);
        
        // Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(currentUser);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);
        order.setNotes(request.getNotes());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Create order items from cart
        List<OrderItem> orderItems = cart.getItems().stream()
            .map(cartItem -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setProductName(cartItem.getProduct().getName());
                orderItem.setProductSku(cartItem.getProduct().getSku());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(cartItem.getPriceAtTime());
                orderItem.setTotalPrice(cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                orderItem.setCreatedAt(LocalDateTime.now());
                return orderItem;
            })
            .toList();
        
        order.setItems(orderItems);
        
        // Calculate totals
        order.calculateTotal();
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Update product stock
        updateProductStock(cart);
        
        // Clear cart
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        
        // Emit WebSocket event for new order
        webSocketController.sendOrderStatusUpdate(savedOrder.getId().toString(), "CREATED", currentUser.getId().toString());
        
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Check if current user can access this order
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only view your own orders");
        }
        
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Cacheable(value = "orders", key = "#orderNumber")
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        
        // Check if current user can access this order
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only view your own orders");
        }
        
        return orderMapper.toOrderResponse(order);
    }



    @Override
    @Cacheable(value = "orders", key = "#status + #pageable.pageNumber + #pageable.pageSize")
    public PaginationResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        // Only admins can filter by status
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can filter orders by status");
        }
        
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        List<OrderResponse> orderResponses = orderMapper.toOrderResponseList(orders.getContent());
        
        return PaginationResponse.<OrderResponse>builder()
            .data(orderResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(orders.getNumber())
                .limit(orders.getSize())
                .total(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build())
            .build();
    }

    @Override
    @Cacheable(value = "orders", key = "#paymentStatus + #pageable.pageNumber + #pageable.pageSize")
    public PaginationResponse<OrderResponse> getOrdersByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable) {
        // Only admins can filter by payment status
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can filter orders by payment status");
        }
        
        Page<Order> orders = orderRepository.findByPaymentStatus(paymentStatus, pageable);
        List<OrderResponse> orderResponses = orderMapper.toOrderResponseList(orders.getContent());
        
        return PaginationResponse.<OrderResponse>builder()
            .data(orderResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(orders.getNumber())
                .limit(orders.getSize())
                .total(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build())
            .build();
    }

    @Override
    @Cacheable(value = "orders", key = "#startDate.toString() + #endDate.toString()")
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Only admins can filter by date range
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can filter orders by date range");
        }
        
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        return orderMapper.toOrderResponseList(orders);
    }

    @Override
    @Cacheable(value = "orders", key = "#date.toString()")
    public List<OrderResponse> getOrdersByEstimatedDelivery(LocalDate date) {
        // Only admins can filter by estimated delivery date
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can filter orders by estimated delivery date");
        }
        
        List<Order> orders = orderRepository.findByEstimatedDeliveryDate(date);
        return orderMapper.toOrderResponseList(orders);
    }

    @Override
    @Cacheable(value = "orders", key = "#date.toString()")
    public List<OrderResponse> getOrdersByActualDelivery(LocalDate date) {
        // Only admins can filter by actual delivery date
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can filter orders by actual delivery date");
        }
        
        List<Order> orders = orderRepository.findByActualDeliveryDate(date);
        return orderMapper.toOrderResponseList(orders);
    }

    @Override
    @Cacheable(value = "orders", key = "#searchTerm + #pageable.pageNumber + #pageable.pageSize")
    public PaginationResponse<OrderResponse> searchOrders(String searchTerm, Pageable pageable) {
        // Only admins can search orders
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can search orders");
        }
        
        Page<Order> orders = orderRepository.searchOrders(searchTerm, pageable);
        List<OrderResponse> orderResponses = orderMapper.toOrderResponseList(orders.getContent());
        
        return PaginationResponse.<OrderResponse>builder()
            .data(orderResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(orders.getNumber())
                .limit(orders.getSize())
                .total(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build())
            .build();
    }

    @Override
    @CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
    public ApiResponse updateOrderStatus(Long id, OrderStatus status) {
        log.info("Updating order status: orderId={}, status={}", id, status);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Check if current user can update this order
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getRole().name().equals("SELLER")) {
            throw new BusinessException("Only admins and sellers can update order status");
        }
        
        // Validate status transition
        if (!canTransitionToStatus(order.getStatus(), status)) {
            throw new BusinessException("Cannot transition from " + order.getStatus() + " to " + status);
        }
        
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Set delivery dates based on status
        if (status == OrderStatus.SHIPPED) {
            order.setEstimatedDeliveryDate(LocalDate.now().plusDays(3)); // 3 days delivery
        } else if (status == OrderStatus.DELIVERED) {
            order.setActualDeliveryDate(LocalDateTime.now());
        }
        
        orderRepository.save(order);
        
        // Emit WebSocket event for real-time updates
        webSocketController.sendOrderStatusUpdate(id.toString(), status.name(), currentUser.getId().toString());
        
        log.info("Order status updated successfully. Order ID: {}", id);
        
        return ApiResponse.success(null, "Order status updated successfully");
    }

    @Override
    @CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
    public ApiResponse updatePaymentStatus(Long id, PaymentStatus paymentStatus) {
        log.info("Updating payment status: orderId={}, status={}", id, paymentStatus);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Check if current user can update this order
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getRole().name().equals("SELLER")) {
            throw new BusinessException("Only admins and sellers can update payment status");
        }
        
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info("Payment status updated successfully. Order ID: {}", id);
        
        return ApiResponse.success(null, "Payment status updated successfully");
    }

    @Override
    @CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
    public ApiResponse updateDeliveryDate(Long id, LocalDate deliveryDate) {
        log.info("Updating delivery date: orderId={}, date={}", id, deliveryDate);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Check if current user can update this order
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getRole().name().equals("SELLER")) {
            throw new BusinessException("Only admins and sellers can update delivery date");
        }
        
        order.setEstimatedDeliveryDate(deliveryDate);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info("Delivery date updated successfully. Order ID: {}", id);
        
        return ApiResponse.success(null, "Delivery date updated successfully");
    }

    @Override
    @CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
    public ApiResponse cancelOrder(Long id) {
        log.info("Cancelling order: orderId={}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Check if current user can cancel this order
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only cancel your own orders");
        }
        
        if (!order.canBeCancelled()) {
            throw new BusinessException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // Restore product stock
        restoreProductStock(order);
        
        log.info("Order cancelled successfully. Order ID: {}", id);
        
        return ApiResponse.success(null, "Order cancelled successfully");
    }

    @Override
    @CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
    public ApiResponse addOrderNote(Long id, String note) {
        log.info("Adding note to order: orderId={}, note={}", id, note);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Check if current user can add notes to this order
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getRole().name().equals("SELLER") && 
            !order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You cannot add notes to this order");
        }
        
        String currentNotes = order.getNotes() != null ? order.getNotes() : "";
        String newNotes = currentNotes + "\n" + LocalDateTime.now() + " - " + note;
        order.setNotes(newNotes);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info("Note added to order successfully. Order ID: {}", id);
        
        return ApiResponse.success(null, "Note added successfully");
    }

    private void validateCartItems(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.getIsActive()) {
                throw new BusinessException("Product " + product.getName() + " is not active");
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException("Insufficient stock for " + product.getName() + ". Available: " + product.getStockQuantity());
            }
        }
    }

    private void updateProductStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.decreaseStock(item.getQuantity());
            productRepository.save(product);
        }
    }

    private void restoreProductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean canTransitionToStatus(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED:
                return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED:
                return newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED;
            case DELIVERED:
            case CANCELLED:
                return false; // Final states
            default:
                return false;
        }
    }

    @Override
    @Cacheable(value = "userOrders", key = "#pageable.pageNumber + #pageable.pageSize")
    public PaginationResponse<OrderResponse> getUserOrders(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Order> orders = orderRepository.findByUserId(currentUser.getId().toString(), pageable);
        List<OrderResponse> orderResponses = orderMapper.toOrderResponseList(orders.getContent());
        
        return PaginationResponse.<OrderResponse>builder()
            .data(orderResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(orders.getNumber())
                .limit(orders.getSize())
                .total(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build())
            .build();
    }

    @Override
    @Cacheable(value = "allOrders", key = "#pageable.pageNumber + #pageable.pageSize")
    public PaginationResponse<OrderResponse> getAllOrders(Pageable pageable) {
        // Only admins can view all orders
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can view all orders");
        }
        
        Page<Order> orders = orderRepository.findAll(pageable);
        List<OrderResponse> orderResponses = orderMapper.toOrderResponseList(orders.getContent());
        
        return PaginationResponse.<OrderResponse>builder()
            .data(orderResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(orders.getNumber())
                .limit(orders.getSize())
                .total(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build())
            .build();
    }

    @Override
    @CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
    public ApiResponse refundOrder(Long id) {
        log.info("Refunding order: orderId={}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Only admins can refund orders
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can refund orders");
        }
        
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessException("Order must be paid to be refunded");
        }
        
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info("Order refunded successfully. Order ID: {}", id);
        
        return ApiResponse.success(null, "Order refunded successfully");
    }   

    @Override
    @CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
    public ApiResponse addOrderNotes(Long id, String notes) {
        log.info("Adding notes to order: orderId={}, notes={}", id, notes);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Check if current user can add notes to this order
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getRole().name().equals("SELLER") && 
            !order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You cannot add notes to this order");
        }
        
        String currentNotes = order.getNotes() != null ? order.getNotes() : "";
        String newNotes = currentNotes + "\n" + LocalDateTime.now() + " - " + notes;
        order.setNotes(newNotes);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info("Notes added to order successfully. Order ID: {}", id);
        
        return ApiResponse.success(null, "Notes added successfully");
    }

    @Override
    public OrderResponse getOrderDetails(Long id) {
        return getOrderById(id); // Same implementation
    }

    @Override
    @Cacheable(value = "orders", key = "#minAmount + #maxAmount + #pageable.pageNumber + #pageable.pageSize")
    public PaginationResponse<OrderResponse> getOrdersByAmountRange(Double minAmount, Double maxAmount, Pageable pageable) {
        // Only admins can filter by amount range
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can filter orders by amount range");
        }
        
        Page<Order> orders = orderRepository.findByAmountRange(minAmount, maxAmount, pageable);
        List<OrderResponse> orderResponses = orderMapper.toOrderResponseList(orders.getContent());
        
        return PaginationResponse.<OrderResponse>builder()
            .data(orderResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(orders.getNumber())
                .limit(orders.getSize())
                .total(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build())
            .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // This would need to be implemented with proper user retrieval
        // For now, returning a mock user
        throw new BusinessException("Current user retrieval not implemented");
    }
}
