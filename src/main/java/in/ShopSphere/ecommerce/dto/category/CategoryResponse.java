package in.ShopSphere.ecommerce.dto.category;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryResponse {
    
    private Long id;
    private String name;
    private String description;
    private String image;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private CategorySummary parent;
    private List<CategorySummary> children;
    private Integer productCount;
    
    @Data
    public static class CategorySummary {
        private Long id;
        private String name;
        private String description;
        private String image;
        private Boolean isActive;
        private Integer sortOrder;
        private Integer productCount;
    }
}
