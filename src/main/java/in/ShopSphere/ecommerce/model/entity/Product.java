package in.ShopSphere.ecommerce.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "products", indexes = {
    @Index(name = "idx_products_category_id", columnList = "category_id"),
    @Index(name = "idx_products_seller_id", columnList = "seller_id"),
    @Index(name = "idx_products_is_active", columnList = "is_active"),
    @Index(name = "idx_products_price", columnList = "price"),
    @Index(name = "idx_products_rating", columnList = "rating"),
    @Index(name = "idx_products_stock_quantity", columnList = "stock_quantity"),
    @Index(name = "idx_products_sku", columnList = "sku")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @DecimalMin(value = "0.00", message = "Sale price must be greater than or equal to 0")
    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;
    
    @DecimalMin(value = "0.00", message = "Cost price must be greater than or equal to 0")
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;
    
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    @Column(unique = true)
    private String sku;
    
    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    @Column
    private String barcode;
    
    @DecimalMin(value = "0.000", message = "Weight must be greater than or equal to 0")
    @Column(precision = 8, scale = 3)
    private BigDecimal weight;
    
    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    @Column
    private String dimensions;
    
    @NotNull(message = "Stock quantity is required")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
    
    @Column(name = "min_stock_level")
    private Integer minStockLevel = 5;
    
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "review_count")
    private Integer reviewCount = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductVariant> variants;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wishlist> wishlistItems;
    
    // Business logic methods
    public BigDecimal getCurrentPrice() {
        return salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0 ? salePrice : price;
    }
    
    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0 && salePrice.compareTo(price) < 0;
    }
    
    public BigDecimal getDiscountPercentage() {
        if (!isOnSale()) {
            return BigDecimal.ZERO;
        }
        return price.subtract(salePrice).divide(price, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    public boolean isLowStock() {
        return stockQuantity <= minStockLevel;
    }
    
    public boolean hasStock(int quantity) {
        return stockQuantity >= quantity;
    }
    
    public void decreaseStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
    }
    
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
    
    public void updateRating(BigDecimal newRating) {
        if (this.rating.equals(BigDecimal.ZERO)) {
            this.rating = newRating;
        } else {
            // Calculate weighted average
            BigDecimal totalRating = this.rating.multiply(new BigDecimal(reviewCount));
            totalRating = totalRating.add(newRating);
            this.rating = totalRating.divide(new BigDecimal(reviewCount + 1), 2, BigDecimal.ROUND_HALF_UP);
        }
        this.reviewCount++;
    }
    
    public String getPrimaryImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.stream()
                    .filter(ProductImage::getIsPrimary)
                    .findFirst()
                    .map(ProductImage::getImageUrl)
                    .orElse(images.get(0).getImageUrl());
        }
        return null;
    }
    
    public List<String> getAllImageUrls() {
        if (images != null && !images.isEmpty()) {
            return images.stream()
                    .sorted((i1, i2) -> Integer.compare(i1.getSortOrder(), i2.getSortOrder()))
                    .map(ProductImage::getImageUrl)
                    .toList();
        }
        return List.of();
    }
}
