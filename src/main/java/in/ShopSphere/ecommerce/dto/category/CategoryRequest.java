package in.ShopSphere.ecommerce.dto.category;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Category description is required")
    @Size(min = 10, max = 500, message = "Category description must be between 10 and 500 characters")
    private String description;
    
    @Size(max = 500, message = "Category image URL must not exceed 500 characters")
    private String image;
    
    private String parentId;
    
    @Min(value = 1, message = "Sort order must be at least 1")
    @Max(value = 1000, message = "Sort order cannot exceed 1000")
    private Integer sortOrder = 1;
    
    private Boolean isActive = true;
}
