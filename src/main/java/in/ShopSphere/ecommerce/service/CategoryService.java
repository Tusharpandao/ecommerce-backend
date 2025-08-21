package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.category.CategoryRequest;
import in.ShopSphere.ecommerce.dto.category.CategoryResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    
    CategoryResponse createCategory(CategoryRequest request);
    
    CategoryResponse updateCategory(String id, CategoryRequest request);
    
    CategoryResponse getCategoryById(String id);
    
    CategoryResponse getCategoryByName(String name);
    
    PaginationResponse<CategoryResponse> getAllCategories(Pageable pageable);
    
    List<CategoryResponse> getRootCategories();
    
    List<CategoryResponse> getSubCategories(String parentId);
    
    List<CategoryResponse> getActiveCategories();
    
    PaginationResponse<CategoryResponse> searchCategories(String searchTerm, Pageable pageable);
    
    List<CategoryResponse> getCategoriesWithProducts();
    
    ApiResponse deleteCategory(String id);
    
    ApiResponse toggleCategoryStatus(String id);
    
    ApiResponse updateCategoryOrder(String id, Integer sortOrder);
    
    ApiResponse moveCategory(String id, String newParentId);
}
