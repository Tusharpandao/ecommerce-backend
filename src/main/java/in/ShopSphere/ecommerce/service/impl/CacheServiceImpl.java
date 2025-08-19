package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.model.entity.Category;
import in.ShopSphere.ecommerce.model.entity.Product;
import in.ShopSphere.ecommerce.model.entity.User;
import in.ShopSphere.ecommerce.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;
    
    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final String CATEGORY_CACHE_PREFIX = "category:";
    private static final String USER_SESSION_PREFIX = "session:";
    private static final String PRODUCTS_LIST_PREFIX = "products:";
    private static final String CATEGORIES_LIST_PREFIX = "categories:";
    private static final String CART_PREFIX = "cart:";
    private static final String ORDER_PREFIX = "order:";
    private static final String SEARCH_PREFIX = "search:";
    
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final Duration SESSION_TTL = Duration.ofHours(24);
    private static final Duration CART_TTL = Duration.ofMinutes(15);
    private static final Duration SEARCH_TTL = Duration.ofMinutes(30);

    // Enhanced product caching with Spring Cache integration
    @Override
    public void cacheProduct(Product product) {
        if (product == null || product.getId() == null) {
            log.warn("Cannot cache null or invalid product");
            return;
        }
        
        String key = PRODUCT_CACHE_PREFIX + product.getId();
        try {
            // Cache in Redis for manual operations
            redisTemplate.opsForValue().set(key, product, DEFAULT_TTL);
            
            // Also cache in Spring Cache
            Cache cache = cacheManager.getCache("products");
            if (cache != null) {
                cache.put(product.getId(), product);
            }
            
            log.debug("Product cached with key: {} in both Redis and Spring Cache", key);
        } catch (Exception e) {
            log.error("Failed to cache product: {}", key, e);
        }
    }

    @Override
    public Optional<Product> getCachedProduct(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        
        try {
            // First try Spring Cache
            Cache cache = cacheManager.getCache("products");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(productId);
                if (wrapper != null) {
                    log.debug("Product found in Spring Cache: {}", productId);
                    return Optional.of((Product) wrapper.get());
                }
            }
            
            // Fallback to Redis
            String key = PRODUCT_CACHE_PREFIX + productId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Product) {
                log.debug("Product found in Redis: {}", key);
                return Optional.of((Product) cached);
            }
        } catch (Exception e) {
            log.error("Failed to get cached product: {}", productId, e);
        }
        return Optional.empty();
    }

    // Enhanced cart caching
    public void cacheCart(Long userId, Object cartData) {
        if (userId == null || cartData == null) {
            return;
        }
        
        String key = CART_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, cartData, CART_TTL);
            
            // Also cache in Spring Cache
            Cache cache = cacheManager.getCache("carts");
            if (cache != null) {
                cache.put(userId, cartData);
            }
            
            log.debug("Cart cached for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to cache cart for user: {}", userId, e);
        }
    }
    
    public Optional<Object> getCachedCart(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        
        try {
            // First try Spring Cache
            Cache cache = cacheManager.getCache("carts");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(userId);
                if (wrapper != null) {
                    log.debug("Cart found in Spring Cache for user: {}", userId);
                    return Optional.of(wrapper.get());
                }
            }
            
            // Fallback to Redis
            String key = CART_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("Cart found in Redis for user: {}", userId);
                return Optional.of(cached);
            }
        } catch (Exception e) {
            log.error("Failed to get cached cart for user: {}", userId, e);
        }
        return Optional.empty();
    }
    
    public void invalidateCartCache(Long userId) {
        if (userId == null) {
            return;
        }
        
        try {
            // Invalidate in Spring Cache
            Cache cache = cacheManager.getCache("carts");
            if (cache != null) {
                cache.evict(userId);
            }
            
            // Invalidate in Redis
            String key = CART_PREFIX + userId;
            redisTemplate.delete(key);
            
            log.debug("Cart cache invalidated for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to invalidate cart cache for user: {}", userId, e);
        }
    }

    // Enhanced search caching
    public void cacheSearchResults(String searchKey, Object results) {
        if (searchKey == null || results == null) {
            return;
        }
        
        String key = SEARCH_PREFIX + searchKey.hashCode();
        try {
            redisTemplate.opsForValue().set(key, results, SEARCH_TTL);
            
            // Also cache in Spring Cache
            Cache cache = cacheManager.getCache("search");
            if (cache != null) {
                cache.put(searchKey.hashCode(), results);
            }
            
            log.debug("Search results cached for key: {}", searchKey);
        } catch (Exception e) {
            log.error("Failed to cache search results for key: {}", searchKey, e);
        }
    }
    
    public Optional<Object> getCachedSearchResults(String searchKey) {
        if (searchKey == null) {
            return Optional.empty();
        }
        
        try {
            // First try Spring Cache
            Cache cache = cacheManager.getCache("search");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(searchKey.hashCode());
                if (wrapper != null) {
                    log.debug("Search results found in Spring Cache for key: {}", searchKey);
                    return Optional.of(wrapper.get());
                }
            }
            
            // Fallback to Redis
            String key = SEARCH_PREFIX + searchKey.hashCode();
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("Search results found in Redis for key: {}", searchKey);
                return Optional.of(cached);
            }
        } catch (Exception e) {
            log.error("Failed to get cached search results for key: {}", searchKey, e);
        }
        return Optional.empty();
    }

    // Enhanced cache invalidation with Spring Cache integration
    @Override
    public void invalidateProductCache(Long productId) {
        if (productId == null) {
            return;
        }
        
        try {
            // Invalidate in Spring Cache
            Cache cache = cacheManager.getCache("products");
            if (cache != null) {
                cache.evict(productId);
            }
            
            // Invalidate in Redis
            String key = PRODUCT_CACHE_PREFIX + productId;
            redisTemplate.delete(key);
            
            // Also invalidate related caches
            invalidateSearchCache();
            invalidateProductsListCache();
            
            log.debug("Product cache invalidated: {}", productId);
        } catch (Exception e) {
            log.error("Failed to invalidate product cache: {}", productId, e);
        }
    }

    @Override
    public void invalidateAllProductCache() {
        try {
            // Clear Spring Cache
            Cache cache = cacheManager.getCache("products");
            if (cache != null) {
                cache.clear();
            }
            
            // Clear Redis
            Set<String> keys = redisTemplate.keys(PRODUCT_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            // Clear related caches
            invalidateSearchCache();
            invalidateProductsListCache();
            
            log.debug("All product caches invalidated");
        } catch (Exception e) {
            log.error("Failed to invalidate all product caches", e);
        }
    }

    private void invalidateSearchCache() {
        try {
            Cache cache = cacheManager.getCache("search");
            if (cache != null) {
                cache.clear();
            }
            
            Set<String> keys = redisTemplate.keys(SEARCH_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Failed to invalidate search cache", e);
        }
    }
    
    private void invalidateProductsListCache() {
        try {
            Set<String> keys = redisTemplate.keys(PRODUCTS_LIST_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Failed to invalidate products list cache", e);
        }
    }

    // Enhanced cache statistics
    @Override
    public long getCacheSize(String cacheName) {
        if (cacheName == null) {
            return 0;
        }
        
        try {
            // Get cache size using Redis pattern matching
            Set<String> keys = redisTemplate.keys(cacheName + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("Failed to get cache size for: {}", cacheName, e);
            return 0;
        }
    }

    @Override
    public void clearCache(String cacheName) {
        if (cacheName == null) {
            return;
        }
        
        try {
            // Clear Spring Cache
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
            
            // Clear Redis
            Set<String> keys = redisTemplate.keys(cacheName + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            log.debug("Cache cleared: {}, count: {}", cacheName, keys != null ? keys.size() : 0);
        } catch (Exception e) {
            log.error("Failed to clear cache: {}", cacheName, e);
        }
    }

    // Keep existing methods for backward compatibility
    @Override
    public void cacheProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        
        String key = PRODUCTS_LIST_PREFIX + "all";
        try {
            redisTemplate.opsForValue().set(key, products, DEFAULT_TTL);
            log.debug("Products list cached with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to cache products list: {}", key, e);
        }
    }

    @Override
    public List<Product> getCachedProducts(String key) {
        if (key == null) {
            return null;
        }
        
        String cacheKey = PRODUCTS_LIST_PREFIX + key;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List) {
                log.debug("Products list found in cache: {}", cacheKey);
                return (List<Product>) cached;
            }
        } catch (Exception e) {
            log.error("Failed to get cached products list: {}", cacheKey, e);
        }
        return null;
    }

    @Override
    public void cacheCategory(Category category) {
        if (category == null || category.getId() == null) {
            log.warn("Cannot cache null or invalid category");
            return;
        }
        
        String key = CATEGORY_CACHE_PREFIX + category.getId();
        try {
            redisTemplate.opsForValue().set(key, category, DEFAULT_TTL);
            
            // Also cache in Spring Cache
            Cache cache = cacheManager.getCache("categories");
            if (cache != null) {
                cache.put(category.getId(), category);
            }
            
            log.debug("Category cached with key: {} in both Redis and Spring Cache", key);
        } catch (Exception e) {
            log.error("Failed to cache category: {}", key, e);
        }
    }

    @Override
    public Optional<Category> getCachedCategory(Long categoryId) {
        if (categoryId == null) {
            return Optional.empty();
        }
        
        try {
            // First try Spring Cache
            Cache cache = cacheManager.getCache("categories");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(categoryId);
                if (wrapper != null) {
                    log.debug("Category found in Spring Cache: {}", categoryId);
                    return Optional.of((Category) wrapper.get());
                }
            }
            
            // Fallback to Redis
            String key = CATEGORY_CACHE_PREFIX + categoryId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Category) {
                log.debug("Category found in Redis: {}", key);
                return Optional.of((Category) cached);
            }
        } catch (Exception e) {
            log.error("Failed to get cached category: {}", categoryId, e);
        }
        return Optional.empty();
    }

    @Override
    public void cacheCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        
        String key = CATEGORIES_LIST_PREFIX + "all";
        try {
            redisTemplate.opsForValue().set(key, categories, DEFAULT_TTL);
            log.debug("Categories list cached with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to cache categories list: {}", key, e);
        }
    }

    @Override
    public List<Category> getCachedCategories(String key) {
        if (key == null) {
            return null;
        }
        
        String cacheKey = CATEGORIES_LIST_PREFIX + key;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List) {
                log.debug("Categories list found in cache: {}", cacheKey);
                return (List<Category>) cached;
            }
        } catch (Exception e) {
            log.error("Failed to get cached categories list: {}", cacheKey, e);
        }
        return null;
    }

    @Override
    public void invalidateCategoryCache(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        
        try {
            // Invalidate in Spring Cache
            Cache cache = cacheManager.getCache("categories");
            if (cache != null) {
                cache.evict(categoryId);
            }
            
            // Invalidate in Redis
            String key = CATEGORY_CACHE_PREFIX + categoryId;
            redisTemplate.delete(key);
            
            // Also invalidate related caches
            invalidateProductsListCache();
            
            log.debug("Category cache invalidated: {}", categoryId);
        } catch (Exception e) {
            log.error("Failed to invalidate category cache: {}", categoryId, e);
        }
    }

    @Override
    public void invalidateAllCategoryCache() {
        try {
            // Clear Spring Cache
            Cache cache = cacheManager.getCache("categories");
            if (cache != null) {
                cache.clear();
            }
            
            // Clear Redis
            Set<String> keys = redisTemplate.keys(CATEGORY_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            // Clear related caches
            invalidateProductsListCache();
            
            log.debug("All category caches invalidated");
        } catch (Exception e) {
            log.error("Failed to invalidate all category caches", e);
        }
    }

    @Override
    public void cacheUserSession(String sessionId, User user) {
        if (sessionId == null || user == null) {
            log.warn("Cannot cache null session ID or user");
            return;
        }
        
        String key = USER_SESSION_PREFIX + sessionId;
        try {
            redisTemplate.opsForValue().set(key, user, SESSION_TTL);
            log.debug("User session cached with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to cache user session: {}", key, e);
        }
    }

    @Override
    public Optional<User> getCachedUserSession(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }
        
        String key = USER_SESSION_PREFIX + sessionId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof User) {
                log.debug("User session found in cache: {}", key);
                return Optional.of((User) cached);
            }
        } catch (Exception e) {
            log.error("Failed to get cached user session: {}", key, e);
        }
        return Optional.empty();
    }

    @Override
    public void invalidateUserSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        
        String key = USER_SESSION_PREFIX + sessionId;
        try {
            redisTemplate.delete(key);
            log.debug("User session cache invalidated: {}", key);
        } catch (Exception e) {
            log.error("Failed to invalidate user session cache: {}", key, e);
        }
    }

    @Override
    public void invalidateAllUserSessions() {
        try {
            Set<String> keys = redisTemplate.keys(USER_SESSION_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("All user session caches invalidated, count: {}", keys.size());
            }
        } catch (Exception e) {
            log.error("Failed to invalidate all user session caches", e);
        }
    }

    @Override
    public <T> void cache(String key, T value, long ttlSeconds) {
        if (key == null || value == null) {
            log.warn("Cannot cache null key or value");
            return;
        }
        
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            log.debug("Object cached with key: {} and TTL: {}s", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Failed to cache object with key: {}", key, e);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        if (key == null || clazz == null) {
            return Optional.empty();
        }
        
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (clazz.isInstance(cached)) {
                log.debug("Object found in cache: {}", key);
                return Optional.of(clazz.cast(cached));
            }
        } catch (Exception e) {
            log.error("Failed to get cached object: {}", key, e);
        }
        return Optional.empty();
    }

    @Override
    public void invalidate(String key) {
        if (key == null) {
            return;
        }
        
        try {
            redisTemplate.delete(key);
            log.debug("Cache invalidated: {}", key);
        } catch (Exception e) {
            log.error("Failed to invalidate cache: {}", key, e);
        }
    }

    @Override
    public void invalidateByPattern(String pattern) {
        if (pattern == null) {
            return;
        }
        
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Caches invalidated by pattern: {}, count: {}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("Failed to invalidate caches by pattern: {}", pattern, e);
        }
    }

    @Override
    public void clearAllCaches() {
        try {
            // Clear all Spring Caches
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
            
            // Clear all Redis
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.debug("All caches cleared");
        } catch (Exception e) {
            log.error("Failed to clear all caches", e);
        }
    }
}
