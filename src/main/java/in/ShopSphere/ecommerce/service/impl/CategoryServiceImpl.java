package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.dto.category.CategoryRequest;
import in.ShopSphere.ecommerce.dto.category.CategoryResponse;
import in.ShopSphere.ecommerce.dto.category.CategorySimpleResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.exception.BusinessException;
import in.ShopSphere.ecommerce.exception.ResourceNotFoundException;
import in.ShopSphere.ecommerce.mapper.CategoryMapper;
import in.ShopSphere.ecommerce.mapper.CategorySimpleMapper;
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
    private final CategorySimpleMapper categorySimpleMapper;

    @Override
    @CacheEvict(value = {"categories", "categories-simple", "category"}, allEntries = true)
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
    @CacheEvict(value = {"categories", "categories-simple", "category"}, allEntries = true)
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
    @Cacheable(value = "categories") // Re-enabled after fixing Redis serialization
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
    @Cacheable(value = "categories") // Re-enabled after fixing Redis serialization
    public List<CategoryResponse> getRootCategories() {
        List<Category> categories = categoryRepository.findRootCategories();
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Cacheable(value = "categories") // Re-enabled after fixing Redis serialization
    public List<CategoryResponse> getSubCategories(String parentId) {
        List<Category> categories = categoryRepository.findSubCategories(parentId);
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Cacheable(value = "categories") // Re-enabled after fixing Redis serialization
    public List<CategoryResponse> getCategoriesWithProducts() {
        List<Category> categories = categoryRepository.findCategoriesWithActiveProducts();
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Cacheable(value = "categories") // Re-enabled after fixing Redis serialization
    public List<CategoryResponse> getActiveCategories() {
        log.info("Fetching active categories from database");
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        log.info("Found {} active categories in database", categories.size());
        List<CategoryResponse> responses = categoryMapper.toCategoryResponseList(categories);
        log.info("Mapped {} categories to response DTOs", responses.size());
        return responses;
    }

    @Override
    @Cacheable(value = "categories-simple") // Separate cache for simple categories
    public List<CategorySimpleResponse> getActiveCategoriesSimple() {
        log.info("Fetching active categories (simple) from database");
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        log.info("Found {} active categories in database", categories.size());
        List<CategorySimpleResponse> responses = categorySimpleMapper.toCategorySimpleResponseList(categories);
        log.info("Mapped {} categories to simple response DTOs", responses.size());
        return responses;
    }

    @Override
    @Cacheable(value = "categories") // Re-enabled after fixing Redis serialization
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
    @CacheEvict(value = {"categories", "categories-simple", "category"}, allEntries = true)
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
    @CacheEvict(value = {"categories", "categories-simple", "category"}, allEntries = true)
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
    @CacheEvict(value = {"categories", "categories-simple", "category"}, allEntries = true)
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
    @CacheEvict(value = {"categories", "categories-simple", "category"}, allEntries = true)
    public ApiResponse moveCategory(String id, String newParentId) {
        log.info("Moving category with ID: {} to parent ID: {}", id, newParentId);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        
        // Validate new parent category
        Category newParent = null;
        if (newParentId != null) {
            if (newParentId.equals(id)) {
                throw new BusinessException("Category cannot be its own parent");
            }
            newParent = categoryRepository.findById(newParentId)
                .orElseThrow(() -> new ResourceNotFoundException("New parent category not found with ID: " + newParentId));
        }
        
        category.setParent(newParent);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
        
        log.info("Category moved successfully with ID: {} to parent ID: {}", id, newParentId);
        return ApiResponse.success(null, "Category moved successfully");
    }
    
    @Override
    public long getCategoryCount() {
        return categoryRepository.count();
    }
    
    @Override
    public int initializeBasicCategories() {
        log.info("Initializing basic categories");
        
        // Check if categories already exist
        if (categoryRepository.count() > 0) {
            log.info("Categories already exist, skipping initialization");
            return 0;
        }
        
        // Create basic categories
        Category electronics = Category.builder()
            .name("Electronics")
            .description("Electronic devices and gadgets")
            .isActive(true)
            .sortOrder(1)
            .build();
        
        Category clothing = Category.builder()
            .name("Clothing")
            .description("Apparel and fashion items")
            .isActive(true)
            .sortOrder(2)
            .build();
        
        Category books = Category.builder()
            .name("Books")
            .description("Books and publications")
            .isActive(true)
            .sortOrder(3)
            .build();
        
        Category home = Category.builder()
            .name("Home & Garden")
            .description("Home improvement and garden items")
            .isActive(true)
            .sortOrder(4)
            .build();
        
        Category sports = Category.builder()
            .name("Sports & Outdoors")
            .description("Sports equipment and outdoor gear")
            .isActive(true)
            .sortOrder(5)
            .build();
        
        // Save categories
        categoryRepository.save(electronics);
        categoryRepository.save(clothing);
        categoryRepository.save(books);
        categoryRepository.save(home);
        categoryRepository.save(sports);
        
        log.info("Successfully initialized {} basic categories", 5);
        return 5;
    }
    
    @Override
    @CacheEvict(value = {"categories", "categories-simple", "category"}, allEntries = true)
    public void clearCategoryCache() {
        log.info("Category cache cleared");
        // The @CacheEvict annotation will handle clearing the cache
    }






}