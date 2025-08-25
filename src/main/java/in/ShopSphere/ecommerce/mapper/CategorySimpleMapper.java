package in.ShopSphere.ecommerce.mapper;

import in.ShopSphere.ecommerce.dto.category.CategorySimpleResponse;
import in.ShopSphere.ecommerce.model.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategorySimpleMapper {
    
    public CategorySimpleResponse toCategorySimpleResponse(Category category) {
        if (category == null) {
            return null;
        }
        
        CategorySimpleResponse response = new CategorySimpleResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setImage(category.getImage());
        response.setIsActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        return response;
    }
    
    public List<CategorySimpleResponse> toCategorySimpleResponseList(List<Category> categories) {
        if (categories == null) {
            return List.of();
        }
        
        return categories.stream()
            .map(this::toCategorySimpleResponse)
            .collect(Collectors.toList());
    }
}
