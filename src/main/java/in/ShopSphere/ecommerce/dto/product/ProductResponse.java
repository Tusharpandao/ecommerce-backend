package in.ShopSphere.ecommerce.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private String sku;
    private String barcode;
    private BigDecimal weight;
    private String dimensions;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Double rating;
    private Integer reviewCount;
    private Boolean isActive;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private CategorySummary category;
    private UserSummary seller;
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
    
    @Data
    public static class CategorySummary {
        private Long id;
        private String name;
        private String description;
        private String image;
    }
    
    @Data
    public static class UserSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String profileImage;
    }
    
    @Data
    public static class ProductImageResponse {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
        private Integer sortOrder;
    }
    
    @Data
    public static class ProductVariantResponse {
        private Long id;
        private String variantName;
        private String variantValue;
        private BigDecimal priceAdjustment;
        private Integer stockQuantity;
        private String sku;
        private BigDecimal finalPrice;
    }
}
