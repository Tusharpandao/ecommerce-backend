package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.dto.category.CategoryRequest;
import in.ShopSphere.ecommerce.dto.category.CategoryResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.exception.BusinessException;
import in.ShopSphere.ecommerce.exception.ResourceNotFoundException;
import in.ShopSphere.ecommerce.mapper.CategoryMapper;
import in.ShopSphere.ecommerce.model.entity.Category;
import in.ShopSphere.ecommerce.repository.CategoryRepository;
import in.ShopSphere.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating category: {}", request.getName());
        
        // Check if category name already exists (within the same parent)
        if (categoryRepository.existsByNameAndParentId(request.getName(), request.getParentId())) {
            throw new BusinessException("Category with name " + request.getName() + " already exists in this parent category");
        }
        
        // Validate parent category if provided
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));
        }
        
        // Create category
        Category category = categoryMapper.toCategory(request);
        category.setParent(parent);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        log.info("Updating category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Check if name already exists (if changed)
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByNameAndParentId(request.getName(), request.getParentId())) {
            throw new BusinessException("Category with name " + request.getName() + " already exists in this parent category");
        }
        
        // Validate parent category if provided
        Category parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("Category cannot be its own parent");
            }
            parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));
        }
        
        // Update category
        categoryMapper.updateCategoryFromRequest(request, category);
        category.setParent(parent);
        category.setUpdatedAt(LocalDateTime.now());
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @Override
    @Cacheable(value = "category", key = "#id")
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        if (!category.getIsActive()) {
            throw new BusinessException("Category is not active");
        }
        
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Cacheable(value = "category", key = "#name")
    public CategoryResponse getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        
        if (!category.getIsActive()) {
            throw new BusinessException("Category is not active");
        }
        
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Cacheable(value = "categories")
    public PaginationResponse<CategoryResponse> getAllCategories(Pageable pageable) {
        var categories = categoryRepository.findAllActive(pageable);
        List<CategoryResponse> categoryResponses = categoryMapper.toCategoryResponseList(categories.getContent());
        
        return PaginationResponse.<CategoryResponse>builder()
            .data(categoryResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(categories.getNumber())
                .limit(categories.getSize())
                .total(categories.getTotalElements())
                .totalPages(categories.getTotalPages())
                .build())
            .build();
    }

    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getRootCategories() {
        List<Category> categories = categoryRepository.findRootCategories();
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getSubCategories(String parentId) {
        List<Category> categories = categoryRepository.findSubCategories(parentId);
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getCategoriesWithProducts() {
        List<Category> categories = categoryRepository.findCategoriesWithActiveProducts();
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getActiveCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Cacheable(value = "categories")
    public PaginationResponse<CategoryResponse> searchCategories(String searchTerm, Pageable pageable) {
        var categories = categoryRepository.searchCategories(searchTerm, pageable);
        List<CategoryResponse> categoryResponses = categoryMapper.toCategoryResponseList(categories.getContent());
        
        return PaginationResponse.<CategoryResponse>builder()
            .data(categoryResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(categories.getNumber())
                .limit(categories.getSize())
                .total(categories.getTotalElements())
                .totalPages(categories.getTotalPages())
                .build())
            .build();
    }

    @Override
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public ApiResponse deleteCategory(String id) {
        log.info("Deleting category with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Check if category has children
        if (categoryRepository.countSubCategories(id) > 0) {
            throw new BusinessException("Cannot delete category with subcategories");
        }
        
        // Check if category has products
        if (categoryRepository.findCategoriesWithActiveProducts().stream()
                .anyMatch(c -> c.getId().equals(id))) {
            throw new BusinessException("Cannot delete category with active products");
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
        
        return ApiResponse.success(null, "Category deleted successfully");
    }

    @Override
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public ApiResponse toggleCategoryStatus(String id) {
        log.info("Toggling category status with ID: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        category.setIsActive(!category.getIsActive());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
        
        String status = category.getIsActive() ? "activated" : "deactivated";
        log.info("Category {} successfully with ID: {}", status, id);
        
        return ApiResponse.success(null, "Category " + status + " successfully");
    }

    @Override
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public ApiResponse updateCategoryOrder(String id, Integer sortOrder) {
        log.info("Updating category order for ID: {} to: {}", id, sortOrder);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        category.setSortOrder(sortOrder);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
        
        log.info("Category order updated successfully for ID: {}", id);
        
        return ApiResponse.success(null, "Category order updated successfully");
    }

    @Override
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public ApiResponse moveCategory(String id, String newParentId) {
        log.info("Moving category with ID: {} to parent ID: {}", id, newParentId);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        if (newParentId != null) {
            if (newParentId.equals(id)) {
                throw new BusinessException("Category cannot be its own parent");
            }
            
            Category newParent = categoryRepository.findById(newParentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + newParentId));
            category.setParent(newParent);
        } else {
            category.setParent(null);
        }
        
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
        
        log.info("Category moved successfully with ID: {}", id);
        
        return ApiResponse.success(null, "Category moved successfully");
    }






}