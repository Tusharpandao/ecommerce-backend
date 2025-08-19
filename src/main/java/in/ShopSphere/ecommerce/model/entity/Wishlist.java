package in.ShopSphere.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists", indexes = {
    @Index(name = "idx_wishlist_user_product", columnList = "user_id,product_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Business logic methods
    public String getProductName() {
        return product != null ? product.getName() : null;
    }
    
    public String getProductImage() {
        return product != null ? product.getPrimaryImageUrl() : null;
    }
    
    public boolean isProductInStock() {
        return product != null && product.isInStock();
    }
    
    public boolean isProductOnSale() {
        return product != null && product.isOnSale();
    }
}
