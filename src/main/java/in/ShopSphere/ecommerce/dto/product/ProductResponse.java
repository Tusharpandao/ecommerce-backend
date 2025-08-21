package in.ShopSphere.ecommerce.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductResponse {
    
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private String sku;
    private String barcode;
    private BigDecimal weight;
    private String dimensions;
    private String brand;
    private List<String> tags;
    private BigDecimal discountPercentage;
    private String warrantyInformation;
    private String shippingInformation;
    private String returnPolicy;
    private Integer minimumOrderQuantity;
    private String availabilityStatus;
    private String thumbnail;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Double rating;
    private Integer reviewCount;
    private Boolean isActive;
    private Boolean isFeatured;
    
    // Removed LocalDateTime fields to avoid serialization issues
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
    
    private CategorySummary category;
    private UserSummary seller;
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
    
    @Data
    public static class CategorySummary {
        private String id;
        private String name;
        private String description;
        private String image;
    }
    
    @Data
    public static class UserSummary {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String profileImage;
    }
    
    @Data
    public static class ProductImageResponse {
        private String id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
        private Integer sortOrder;
    }
    
    @Data
    public static class ProductVariantResponse {
        private String id;
        private String variantName;
        private String variantValue;
        private BigDecimal priceAdjustment;
        private Integer stockQuantity;
        private String sku;
        private BigDecimal finalPrice;
    }
}
