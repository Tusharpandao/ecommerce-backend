package in.ShopSphere.ecommerce.dto.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    
    private String id;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isApproved;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private ProductSummary product;
    private UserSummary user;
    
    @Data
    public static class ProductSummary {
        private String id;
        private String name;
        private String sku;
        private String image;
    }
    
    @Data
    public static class UserSummary {
        private String id;
        private String firstName;
        private String lastName;
        private String profileImage;
    }
}
