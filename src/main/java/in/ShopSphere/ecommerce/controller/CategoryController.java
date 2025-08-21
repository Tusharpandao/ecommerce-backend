package in.ShopSphere.ecommerce.controller;

import in.ShopSphere.ecommerce.dto.category.CategoryRequest;
import in.ShopSphere.ecommerce.dto.category.CategoryResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing product categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category", description = "Creates a new product category. Admin only.")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        log.info("Creating category: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a category", description = "Updates an existing product category. Admin only.")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID") @PathVariable String id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("Updating category with ID: {}", id);
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID.")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable String id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get category by name", description = "Retrieves a category by its name.")
    public ResponseEntity<CategoryResponse> getCategoryByName(
            @Parameter(description = "Category name") @PathVariable String name) {
        CategoryResponse response = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all categories with pagination.")
    public ResponseEntity<PaginationResponse<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            log.info("Fetching all categories with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
            PaginationResponse<CategoryResponse> response = categoryService.getAllCategories(pageable);
            log.info("Successfully fetched {} categories", response.getData().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching categories: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    @GetMapping("/simple")
    @Operation(summary = "Get all categories (simple)", description = "Retrieves all categories as a simple list.")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesSimple() {
        try {
            log.info("Fetching all categories (simple)");
            List<CategoryResponse> response = categoryService.getActiveCategories();
            log.info("Successfully fetched {} categories", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching categories (simple): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Retrieves all root (parent) categories.")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{parentId}/subcategories")
    @Operation(summary = "Get subcategories", description = "Retrieves all subcategories of a parent category.")
    public ResponseEntity<List<CategoryResponse>> getSubCategories(
            @Parameter(description = "Parent category ID") @PathVariable String parentId) {
        List<CategoryResponse> response = categoryService.getSubCategories(parentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active categories", description = "Retrieves all active categories.")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        List<CategoryResponse> response = categoryService.getActiveCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/with-products")
    @Operation(summary = "Get categories with products", description = "Retrieves categories that have active products.")
    public ResponseEntity<List<CategoryResponse>> getCategoriesWithProducts() {
        List<CategoryResponse> response = categoryService.getCategoriesWithProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Searches categories by name or description.")
    public ResponseEntity<PaginationResponse<CategoryResponse>> searchCategories(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse<CategoryResponse> response = categoryService.searchCategories(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category", description = "Deletes a category. Admin only.")
    public ResponseEntity<ApiResponse> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable String id) {
        log.info("Deleting category with ID: {}", id);
        ApiResponse response = categoryService.deleteCategory(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle category status", description = "Toggles the active status of a category. Admin only.")
    public ResponseEntity<ApiResponse> toggleCategoryStatus(
            @Parameter(description = "Category ID") @PathVariable String id) {
        log.info("Toggling category status with ID: {}", id);
        ApiResponse response = categoryService.toggleCategoryStatus(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/order")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category order", description = "Updates the sort order of a category. Admin only.")
    public ResponseEntity<ApiResponse> updateCategoryOrder(
            @Parameter(description = "Category ID") @PathVariable String id,
            @Parameter(description = "New sort order") @RequestParam Integer sortOrder) {
        log.info("Updating category order for ID: {} to: {}", id, sortOrder);
        ApiResponse response = categoryService.updateCategoryOrder(id, sortOrder);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/parent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move category", description = "Moves a category to a different parent. Admin only.")
    public ResponseEntity<ApiResponse> moveCategory(
            @Parameter(description = "Category ID") @PathVariable String id,
            @Parameter(description = "New parent category ID") @RequestParam String newParentId) {
        log.info("Moving category with ID: {} to parent ID: {}", id, newParentId);
        ApiResponse response = categoryService.moveCategory(id, newParentId);
        return ResponseEntity.ok(response);
    }
}
