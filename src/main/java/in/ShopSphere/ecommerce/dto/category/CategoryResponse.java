package in.ShopSphere.ecommerce.dto.category;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryResponse {
    
    private String id;
    private String name;
    private String description;
    private String image;
    private Boolean isActive;
    private Integer sortOrder;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private CategorySummary parent;
    private List<CategorySummary> children;
    private Integer productCount;
    
    @Data
    public static class CategorySummary {
        private String id;
        private String name;
        private String description;
        private String image;
        private Boolean isActive;
        private Integer sortOrder;
        private Integer productCount;
    }
}
