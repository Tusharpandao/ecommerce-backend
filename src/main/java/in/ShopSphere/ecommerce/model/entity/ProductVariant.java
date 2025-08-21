package in.ShopSphere.ecommerce.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotBlank(message = "Variant name is required")
    @Size(max = 100, message = "Variant name must not exceed 100 characters")
    @Column(name = "variant_name", nullable = false)
    private String variantName; // e.g., "Size", "Color", "Material"
    
    @NotBlank(message = "Variant value is required")
    @Size(max = 100, message = "Variant value must not exceed 100 characters")
    @Column(name = "variant_value", nullable = false)
    private String variantValue; // e.g., "Large", "Red", "Cotton"
    
    @Column(name = "price_adjustment", precision = 10, scale = 2)
    private BigDecimal priceAdjustment = BigDecimal.ZERO;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;
    
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    @Column
    private String sku;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Business logic methods
    public BigDecimal getFinalPrice() {
        if (product != null) {
            return product.getCurrentPrice().add(priceAdjustment);
        }
        return priceAdjustment;
    }
    
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    public boolean hasStock(int quantity) {
        return stockQuantity >= quantity;
    }
    
    public void decreaseStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new IllegalStateException("Insufficient stock for variant");
        }
        this.stockQuantity -= quantity;
    }
    
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
    
    public String getDisplayName() {
        return variantName + ": " + variantValue;
    }
    
    public boolean isPriceAdjustmentPositive() {
        return priceAdjustment.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isPriceAdjustmentNegative() {
        return priceAdjustment.compareTo(BigDecimal.ZERO) < 0;
    }
}
