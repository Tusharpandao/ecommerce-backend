package in.ShopSphere.ecommerce.dto.order;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;
    
    @NotNull(message = "Billing address ID is required")
    private Long billingAddressId;
    
    @Size(max = 1000, message = "Order notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 50, message = "Coupon code must not exceed 50 characters")
    private String couponCode;
    
    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "Order must contain at least one item")
    @Size(max = 100, message = "Order cannot contain more than 100 items")
    private List<OrderItemRequest> items;
    
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity cannot exceed 999")
        private Integer quantity;
        
        private Long variantId;
    }
}
