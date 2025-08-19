package in.ShopSphere.ecommerce.controller;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cache Management", description = "Cache monitoring and management APIs")
public class CacheController {

    private final CacheService cacheService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get cache statistics", description = "Get statistics for all caches")
    public ResponseEntity<ApiResponse> getCacheStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get cache sizes
            stats.put("products", cacheService.getCacheSize("products"));
            stats.put("categories", cacheService.getCacheSize("categories"));
            stats.put("users", cacheService.getCacheSize("users"));
            stats.put("carts", cacheService.getCacheSize("carts"));
            stats.put("orders", cacheService.getCacheSize("orders"));
            stats.put("search", cacheService.getCacheSize("search"));
            
            log.info("Cache statistics retrieved successfully");
            return ResponseEntity.ok(ApiResponse.success(stats, "Cache statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to get cache statistics", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get cache statistics", e.getMessage()));
        }
    }

    @DeleteMapping("/clear/{cacheName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear specific cache", description = "Clear a specific cache by name")
    public ResponseEntity<ApiResponse> clearCache(@PathVariable String cacheName) {
        try {
            cacheService.clearCache(cacheName);
            log.info("Cache cleared successfully: {}", cacheName);
            return ResponseEntity.ok(ApiResponse.success(null, "Cache cleared successfully: " + cacheName));
        } catch (Exception e) {
            log.error("Failed to clear cache: {}", cacheName, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to clear cache", e.getMessage()));
        }
    }

    @DeleteMapping("/clear/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear all caches", description = "Clear all caches")
    public ResponseEntity<ApiResponse> clearAllCaches() {
        try {
            cacheService.clearAllCaches();
            log.info("All caches cleared successfully");
            return ResponseEntity.ok(ApiResponse.success(null, "All caches cleared successfully"));
        } catch (Exception e) {
            log.error("Failed to clear all caches", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to clear all caches", e.getMessage()));
        }
    }

    @DeleteMapping("/invalidate/product/{productId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Invalidate product cache", description = "Invalidate cache for a specific product")
    public ResponseEntity<ApiResponse> invalidateProductCache(@PathVariable Long productId) {
        try {
            cacheService.invalidateProductCache(productId);
            log.info("Product cache invalidated successfully: {}", productId);
            return ResponseEntity.ok(ApiResponse.success(null, "Product cache invalidated successfully"));
        } catch (Exception e) {
            log.error("Failed to invalidate product cache: {}", productId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to invalidate product cache", e.getMessage()));
        }
    }

    @DeleteMapping("/invalidate/category/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Invalidate category cache", description = "Invalidate cache for a specific category")
    public ResponseEntity<ApiResponse> invalidateCategoryCache(@PathVariable Long categoryId) {
        try {
            cacheService.invalidateCategoryCache(categoryId);
            log.info("Category cache invalidated successfully: {}", categoryId);
            return ResponseEntity.ok(ApiResponse.success(null, "Category cache invalidated successfully"));
        } catch (Exception e) {
            log.error("Failed to invalidate category cache: {}", categoryId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to invalidate category cache", e.getMessage()));
        }
    }

    @DeleteMapping("/invalidate/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Invalidate user cache", description = "Invalidate cache for a specific user")
    public ResponseEntity<ApiResponse> invalidateUserCache(@PathVariable Long userId) {
        try {
            cacheService.invalidate("user:" + userId);
            log.info("User cache invalidated successfully: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(null, "User cache invalidated successfully"));
        } catch (Exception e) {
            log.error("Failed to invalidate user cache: {}", userId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to invalidate user cache", e.getMessage()));
        }
    }

    @DeleteMapping("/invalidate/cart/{userId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    @Operation(summary = "Invalidate cart cache", description = "Invalidate cache for a specific user's cart")
    public ResponseEntity<ApiResponse> invalidateCartCache(@PathVariable Long userId) {
        try {
            cacheService.invalidate("cart:" + userId);
            log.info("Cart cache invalidated successfully for user: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(null, "Cart cache invalidated successfully"));
        } catch (Exception e) {
            log.error("Failed to invalidate cart cache for user: {}", userId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to invalidate cart cache", e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Cache health check", description = "Check if cache is working properly")
    public ResponseEntity<ApiResponse> cacheHealthCheck() {
        try {
            // Try to set and get a test value
            String testKey = "health:check";
            String testValue = "OK";
            
            cacheService.cache(testKey, testValue, 60); // Cache for 1 minute
            var result = cacheService.get(testKey, String.class);
            
            if (result.isPresent() && testValue.equals(result.get())) {
                // Clean up test value
                cacheService.invalidate(testKey);
                return ResponseEntity.ok(ApiResponse.success("OK", "Cache is working properly"));
            } else {
                return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Cache health check failed", "Cache read/write test failed"));
            }
        } catch (Exception e) {
            log.error("Cache health check failed", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Cache health check failed", e.getMessage()));
        }
    }
}
