package in.ShopSphere.ecommerce.mapper;

import in.ShopSphere.ecommerce.dto.category.CategoryRequest;
import in.ShopSphere.ecommerce.dto.category.CategoryResponse;
import in.ShopSphere.ecommerce.model.entity.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toCategory(CategoryRequest request);
    
    @Mapping(target = "parent", source = "parent")
    @Mapping(target = "children", source = "children")
    @Mapping(target = "productCount", ignore = true)
    CategoryResponse toCategoryResponse(Category category);
    
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCategoryFromRequest(CategoryRequest request, @MappingTarget Category category);
    
    @Mapping(target = "productCount", ignore = true)
    CategoryResponse.CategorySummary toCategorySummary(Category category);
    
    List<CategoryResponse.CategorySummary> toCategorySummaryList(List<Category> categories);
}
