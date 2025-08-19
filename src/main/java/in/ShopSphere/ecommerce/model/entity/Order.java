package in.ShopSphere.ecommerce.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user_id", columnList = "user_id"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_created_at", columnList = "created_at"),
    @Index(name = "idx_orders_order_number", columnList = "order_number", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Order status is required")
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Payment status is required")
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.01", message = "Subtotal must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.00", message = "Tax amount must be greater than or equal to 0")
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.00", message = "Shipping amount must be greater than or equal to 0")
    @Column(name = "shipping_amount", precision = 10, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.00", message = "Discount amount must be greater than or equal to 0")
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id")
    private Address billingAddress;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items;
    
    // Business logic methods
    public void calculateTotal() {
        BigDecimal itemsTotal = getItemsTotal();
        this.subtotal = itemsTotal;
        this.totalAmount = itemsTotal
                .add(taxAmount)
                .add(shippingAmount)
                .subtract(discountAmount);
    }
    
    public BigDecimal getItemsTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getTotalItems() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean canBeShipped() {
        return status == OrderStatus.CONFIRMED && paymentStatus == PaymentStatus.PAID;
    }
    
    public boolean canBeDelivered() {
        return status == OrderStatus.SHIPPED;
    }
    
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order can only be confirmed from pending status");
        }
        this.status = OrderStatus.CONFIRMED;
    }
    
    public void ship() {
        if (!canBeShipped()) {
            throw new IllegalStateException("Order cannot be shipped in current status");
        }
        this.status = OrderStatus.SHIPPED;
    }
    
    public void deliver() {
        if (!canBeDelivered()) {
            throw new IllegalStateException("Order cannot be delivered in current status");
        }
        this.status = OrderStatus.DELIVERED;
        this.actualDeliveryDate = LocalDateTime.now();
    }
    
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status");
        }
        this.status = OrderStatus.CANCELLED;
    }
    
    public void markAsPaid() {
        this.paymentStatus = PaymentStatus.PAID;
    }
    
    public void markPaymentFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }
    
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }
    
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }
    
    public boolean isActive() {
        return !isCompleted() && !isCancelled();
    }
    
    public String getStatusDisplayName() {
        return status.name().toLowerCase();
    }
    
    public String getPaymentStatusDisplayName() {
        return paymentStatus.name().toLowerCase();
    }
}
