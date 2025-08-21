package in.ShopSphere.ecommerce.dto.review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    
    private String productId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;
    
    @NotBlank(message = "Review title is required")
    @Size(min = 5, max = 100, message = "Review title must be between 5 and 100 characters")
    private String title;
    
    @NotBlank(message = "Review comment is required")
    @Size(min = 10, max = 1000, message = "Review comment must be between 10 and 1000 characters")
    private String comment;
}
