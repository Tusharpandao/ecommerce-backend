package in.ShopSphere.ecommerce.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import in.ShopSphere.ecommerce.model.entity.OrderStatus;
import in.ShopSphere.ecommerce.model.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    
    private String id;
    private String orderNumber;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate estimatedDeliveryDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualDeliveryDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private UserSummary user;
    private AddressResponse shippingAddress;
    private AddressResponse billingAddress;
    private List<OrderItemResponse> items;
    
    @Data
    public static class UserSummary {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
    }
    
    @Data
    public static class AddressResponse {
        private String id;
        private String addressType;
        private String streetAddress;
        private String streetAddress2;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private Boolean isDefault;
    }
    
    @Data
    public static class OrderItemResponse {
        private String id;
        private String productId;
        private String productName;
        private String productSku;
        private String productImage;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        private ProductVariantInfo variant;
        
        @Data
        public static class ProductVariantInfo {
            private String id;
            private String variantName;
            private String variantValue;
            private BigDecimal priceAdjustment;
        }
    }
}
