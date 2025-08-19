package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.category.CategoryRequest;
import in.ShopSphere.ecommerce.dto.category.CategoryResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    
    CategoryResponse createCategory(CategoryRequest request);
    
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    
    CategoryResponse getCategoryById(Long id);
    
    CategoryResponse getCategoryByName(String name);
    
    PaginationResponse<CategoryResponse> getAllCategories(Pageable pageable);
    
    List<CategoryResponse> getRootCategories();
    
    List<CategoryResponse> getSubCategories(Long parentId);
    
    List<CategoryResponse> getActiveCategories();
    
    PaginationResponse<CategoryResponse> searchCategories(String searchTerm, Pageable pageable);
    
    List<CategoryResponse> getCategoriesWithProducts();
    
    ApiResponse deleteCategory(Long id);
    
    ApiResponse toggleCategoryStatus(Long id);
    
    ApiResponse updateCategoryOrder(Long id, Integer sortOrder);
    
    ApiResponse moveCategory(Long id, Long newParentId);
}
