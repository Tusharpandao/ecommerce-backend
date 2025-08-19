package in.ShopSphere.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> items;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Business logic methods
    public BigDecimal getTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(item -> item.getPriceAtTime().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getItemCount() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    
    public void addItem(CartItem item) {
        if (items == null) {
            items = new java.util.ArrayList<>();
        }
        
        // Check if product already exists in cart
        CartItem existingItem = items.stream()
                .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            // Add new item
            item.setCart(this);
            items.add(item);
        }
    }
    
    public void removeItem(Long productId) {
        if (items != null) {
            items.removeIf(item -> item.getProduct().getId().equals(productId));
        }
    }
    
    public void updateItemQuantity(Long productId, int quantity) {
        if (items != null) {
            items.stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .ifPresent(item -> {
                        if (quantity <= 0) {
                            items.remove(item);
                        } else {
                            item.setQuantity(quantity);
                        }
                    });
        }
    }
    
    public void clear() {
        if (items != null) {
            items.clear();
        }
    }
    
    public CartItem getItemByProductId(Long productId) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }
    
    public boolean containsProduct(Long productId) {
        return getItemByProductId(productId) != null;
    }
}
