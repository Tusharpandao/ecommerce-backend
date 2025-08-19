package in.ShopSphere.ecommerce.dto.cart;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CartRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999, message = "Quantity cannot exceed 999")
    private Integer quantity;
    
    private Long variantId;
}
