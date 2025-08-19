package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.model.entity.Category;
import in.ShopSphere.ecommerce.model.entity.Product;
import in.ShopSphere.ecommerce.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface CacheService {
    
    // Product caching
    void cacheProduct(Product product);
    Optional<Product> getCachedProduct(Long productId);
    void cacheProducts(List<Product> products);
    List<Product> getCachedProducts(String key);
    void invalidateProductCache(Long productId);
    void invalidateAllProductCache();
    
    // Category caching
    void cacheCategory(Category category);
    Optional<Category> getCachedCategory(Long categoryId);
    void cacheCategories(List<Category> categories);
    List<Category> getCachedCategories(String key);
    void invalidateCategoryCache(Long categoryId);
    void invalidateAllCategoryCache();
    
    // User session caching
    void cacheUserSession(String sessionId, User user);
    Optional<User> getCachedUserSession(String sessionId);
    void invalidateUserSession(String sessionId);
    void invalidateAllUserSessions();
    
    // Generic caching
    <T> void cache(String key, T value, long ttlSeconds);
    <T> Optional<T> get(String key, Class<T> clazz);
    void invalidate(String key);
    void invalidateByPattern(String pattern);
    
    // Cache statistics
    long getCacheSize(String cacheName);
    void clearCache(String cacheName);
    void clearAllCaches();
}
