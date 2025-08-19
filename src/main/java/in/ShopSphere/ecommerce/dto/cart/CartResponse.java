package in.ShopSphere.ecommerce.dto.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartResponse {
    
    private Long id;
    private Long userId;
    private Integer itemCount;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<CartItemResponse> items;
    
    @Data
    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private String productImage;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        private ProductVariantInfo variant;
        
        @Data
        public static class ProductVariantInfo {
            private Long id;
            private String variantName;
            private String variantValue;
            private BigDecimal priceAdjustment;
        }
    }
}
