package in.ShopSphere.ecommerce.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;
    
    @NotBlank(message = "Product description is required")
    @Size(min = 10, max = 2000, message = "Product description must be between 10 and 2000 characters")
    private String description;
    
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Product price must have up to 10 digits and 2 decimal places")
    private BigDecimal price;
    
    @DecimalMin(value = "0.01", message = "Sale price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Sale price must have up to 10 digits and 2 decimal places")
    private BigDecimal salePrice;
    
    @DecimalMin(value = "0.01", message = "Cost price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Cost price must have up to 10 digits and 2 decimal places")
    private BigDecimal costPrice;
    
    @NotBlank(message = "Product SKU is required")
    @Size(min = 3, max = 50, message = "Product SKU must be between 3 and 50 characters")
    private String sku;
    
    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Max(value = 999999, message = "Stock quantity cannot exceed 999,999")
    private Integer stockQuantity;
    
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    @Max(value = 999999, message = "Minimum stock level cannot exceed 999,999")
    private Integer minStockLevel;
    
    @Min(value = 0, message = "Maximum stock level cannot be negative")
    @Max(value = 999999, message = "Maximum stock level cannot exceed 999,999")
    private Integer maxStockLevel;
    
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @Digits(integer = 5, fraction = 2, message = "Weight must have up to 5 digits and 2 decimal places")
    private BigDecimal weight;
    
    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    private String dimensions;
    
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;
    
    private List<String> tags;
    
    @DecimalMin(value = "0.00", message = "Discount percentage must be greater than or equal to 0")
    @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Discount percentage must have up to 3 digits and 2 decimal places")
    private BigDecimal discountPercentage;
    
    @Size(max = 1000, message = "Warranty information must not exceed 1000 characters")
    private String warrantyInformation;
    
    @Size(max = 1000, message = "Shipping information must not exceed 1000 characters")
    private String shippingInformation;
    
    @Size(max = 1000, message = "Return policy must not exceed 1000 characters")
    private String returnPolicy;
    
    @Min(value = 1, message = "Minimum order quantity must be at least 1")
    @Max(value = 999999, message = "Minimum order quantity cannot exceed 999,999")
    private Integer minimumOrderQuantity = 1;
    
    @Size(max = 50, message = "Availability status must not exceed 50 characters")
    private String availabilityStatus = "In Stock";
    
    @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
    private String thumbnail;
    
    private Boolean isActive = true;
    
    private Boolean isFeatured = false;
    
    @Size(max = 10, message = "Cannot have more than 10 product images")
    private List<ProductImageRequest> images;
    
    @Size(max = 20, message = "Cannot have more than 20 product variants")
    private List<ProductVariantRequest> variants;
    
    @Data
    public static class ProductImageRequest {
        @NotBlank(message = "Image URL is required")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String imageUrl;
        
        @Size(max = 100, message = "Alt text must not exceed 100 characters")
        private String altText;
        
        private Boolean isPrimary = false;
        
        @Min(value = 1, message = "Sort order must be at least 1")
        @Max(value = 100, message = "Sort order cannot exceed 100")
        private Integer sortOrder = 1;
    }
    
    @Data
    public static class ProductVariantRequest {
        @NotBlank(message = "Variant name is required")
        @Size(max = 100, message = "Variant name must not exceed 100 characters")
        private String variantName;
        
        @NotBlank(message = "Variant value is required")
        @Size(max = 100, message = "Variant value must not exceed 100 characters")
        private String variantValue;
        
        @DecimalMin(value = "-999.99", message = "Price adjustment must be at least -999.99")
        @DecimalMax(value = "999.99", message = "Price adjustment cannot exceed 999.99")
        @Digits(integer = 3, fraction = 2, message = "Price adjustment must have up to 3 digits and 2 decimal places")
        private BigDecimal priceAdjustment;
        
        @Min(value = 0, message = "Stock quantity cannot be negative")
        @Max(value = 999999, message = "Stock quantity cannot exceed 999,999")
        private Integer stockQuantity;
        
        @Size(max = 50, message = "Variant SKU must not exceed 50 characters")
        private String sku;
    }
}
