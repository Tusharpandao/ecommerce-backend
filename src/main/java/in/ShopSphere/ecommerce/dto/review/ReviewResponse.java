package in.ShopSphere.ecommerce.dto.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    
    private Long id;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private ProductSummary product;
    private UserSummary user;
    
    @Data
    public static class ProductSummary {
        private Long id;
        private String name;
        private String sku;
        private String image;
    }
    
    @Data
    public static class UserSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String profileImage;
    }
}
