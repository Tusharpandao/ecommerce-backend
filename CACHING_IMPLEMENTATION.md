# Redis Caching Implementation Guide

## Overview

This document describes the comprehensive Redis caching implementation for the Spring Boot e-commerce backend. The caching system is designed to improve performance by reducing database queries and providing fast access to frequently accessed data.

## Architecture

### Cache Layers
1. **Spring Cache Annotations** - Declarative caching using `@Cacheable`, `@CacheEvict`, `@CachePut`
2. **Redis Template** - Direct Redis operations for complex caching scenarios
3. **Cache Manager** - Centralized cache configuration and management

### Cache Namespaces
- `products` - Product-related data (30 min TTL)
- `categories` - Category data (2 hours TTL)
- `users` - User profile data (1 hour TTL)
- `carts` - Shopping cart data (15 min TTL)
- `orders` - Order data (1 hour TTL)
- `search` - Search results (1 hour TTL)

## Configuration

### Redis Configuration (`RedisConfig.java`)
```java
@Configuration
@EnableCaching
public class RedisConfig {
    // Redis connection factory
    // Redis template with JSON serialization
    // Cache manager with custom TTL configurations
}
```

### Application Properties
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000 # 1 hour
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "ecommerce:"
```

## Caching Annotations Usage

### Product Service
```java
@Cacheable(value = "product", key = "#id")
public ProductResponse getProductById(Long id) {
    // Method will only execute if cache miss
}

@CacheEvict(value = {"products", "product"}, allEntries = true)
public ProductResponse createProduct(ProductRequest request) {
    // Clears all product caches when new product is created
}
```

### Category Service
```java
@Cacheable(value = "category", key = "#id")
public CategoryResponse getCategoryById(Long id) {
    // Cached category lookup
}

@CacheEvict(value = {"categories", "category"}, allEntries = true)
public CategoryResponse updateCategory(Long id, CategoryRequest request) {
    // Invalidates all category caches on update
}
```

### User Service
```java
@Cacheable(value = "users", key = "#id")
public UserResponse getUserById(Long id) {
    // Cached user profile lookup
}
```

### Cart Service
```java
@Cacheable(value = "cart", key = "#root.methodName + #root.args[0]")
public CartResponse getCart() {
    // Cached cart retrieval
}

@CacheEvict(value = "cart", allEntries = true)
public CartResponse addToCart(CartRequest request) {
    // Invalidates cart cache when items are added
}
```

### Order Service
```java
@Cacheable(value = "orders", key = "#id")
public OrderResponse getOrderById(Long id) {
    // Cached order lookup
}

@CacheEvict(value = {"orders", "userOrders", "allOrders"}, allEntries = true)
public OrderResponse createOrder(OrderRequest request) {
    // Invalidates all order caches when new order is created
}
```

## Cache Service Implementation

### Manual Cache Operations
```java
@Service
public class CacheServiceImpl implements CacheService {
    
    // Cache product with dual storage (Spring Cache + Redis)
    public void cacheProduct(Product product) {
        // Store in Redis
        redisTemplate.opsForValue().set(key, product, DEFAULT_TTL);
        
        // Store in Spring Cache
        Cache cache = cacheManager.getCache("products");
        cache.put(product.getId(), product);
    }
    
    // Retrieve with fallback strategy
    public Optional<Product> getCachedProduct(Long productId) {
        // First try Spring Cache
        Cache cache = cacheManager.getCache("products");
        Cache.ValueWrapper wrapper = cache.get(productId);
        if (wrapper != null) {
            return Optional.of((Product) wrapper.get());
        }
        
        // Fallback to Redis
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Product) {
            return Optional.of((Product) cached);
        }
        
        return Optional.empty();
    }
}
```

## Cache Invalidation Strategy

### Automatic Invalidation
- **Create/Update/Delete operations** automatically invalidate related caches
- **Related caches** are invalidated to maintain consistency
- **Pattern-based invalidation** for complex cache relationships

### Manual Invalidation
```java
// Invalidate specific product cache
cacheService.invalidateProductCache(productId);

// Invalidate all product caches
cacheService.invalidateAllProductCache();

// Invalidate by pattern
cacheService.invalidateByPattern("product:*");
```

## Cache Monitoring and Management

### Cache Controller (`/cache/*`)
```java
@RestController
@RequestMapping("/cache")
public class CacheController {
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getCacheStats() {
        // Returns cache sizes and statistics
    }
    
    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<ApiResponse> clearCache(@PathVariable String cacheName) {
        // Clears specific cache
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> cacheHealthCheck() {
        // Tests cache read/write functionality
    }
}
```

### Available Endpoints
- `GET /cache/stats` - Cache statistics (Admin only)
- `DELETE /cache/clear/{cacheName}` - Clear specific cache (Admin only)
- `DELETE /cache/clear/all` - Clear all caches (Admin only)
- `DELETE /cache/invalidate/product/{id}` - Invalidate product cache
- `DELETE /cache/invalidate/category/{id}` - Invalidate category cache
- `DELETE /cache/invalidate/user/{id}` - Invalidate user cache
- `DELETE /cache/invalidate/cart/{userId}` - Invalidate cart cache
- `GET /cache/health` - Cache health check

## Performance Benefits

### Cache Hit Scenarios
1. **Product Lookups** - Frequently accessed products are served from cache
2. **Category Navigation** - Category trees are cached for fast navigation
3. **User Profiles** - User information is cached to reduce database queries
4. **Search Results** - Common search queries are cached
5. **Cart Data** - Shopping cart information is cached for quick access

### Expected Performance Improvements
- **Database queries reduced** by 60-80% for cached data
- **Response time improved** by 70-90% for cache hits
- **System throughput increased** by 40-60%
- **Database load reduced** significantly during peak traffic

## Testing

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class CacheServiceTest {
    
    @Test
    void testCacheProduct() {
        // Tests product caching functionality
    }
    
    @Test
    void testGetCachedProduct_FromSpringCache() {
        // Tests cache hit from Spring Cache
    }
    
    @Test
    void testGetCachedProduct_FromRedis() {
        // Tests fallback to Redis
    }
}
```

### Test Coverage
- Cache hit scenarios
- Cache miss scenarios
- Cache invalidation
- Error handling
- Edge cases (null values, invalid keys)

## Production Considerations

### Redis Configuration
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 30000ms
```

### Monitoring and Alerting
- **Cache hit/miss ratios** should be monitored
- **Redis memory usage** should be tracked
- **Cache eviction rates** should be monitored
- **Response time improvements** should be measured

### Scaling Considerations
- **Redis Cluster** for high availability
- **Cache warming** strategies for cold starts
- **Cache partitioning** for large datasets
- **Backup and recovery** procedures

## Troubleshooting

### Common Issues
1. **Cache not working** - Check Redis connection and Spring Cache configuration
2. **Memory issues** - Monitor Redis memory usage and adjust TTL values
3. **Stale data** - Verify cache invalidation is working correctly
4. **Performance degradation** - Check cache hit ratios and Redis performance

### Debug Commands
```bash
# Redis CLI commands
redis-cli
> INFO memory
> KEYS "*"
> TTL key_name
> FLUSHALL

# Application logs
# Look for cache-related log messages
```

## Best Practices

1. **Cache Key Design** - Use descriptive, consistent key patterns
2. **TTL Strategy** - Set appropriate TTL based on data volatility
3. **Cache Invalidation** - Implement comprehensive invalidation strategies
4. **Memory Management** - Monitor and optimize Redis memory usage
5. **Error Handling** - Implement graceful fallbacks when cache fails
6. **Testing** - Test cache behavior under various scenarios

## Future Enhancements

1. **Cache Warming** - Pre-populate cache with frequently accessed data
2. **Intelligent TTL** - Dynamic TTL based on access patterns
3. **Cache Analytics** - Advanced metrics and insights
4. **Multi-level Caching** - L1 (local) + L2 (Redis) caching strategy
5. **Cache Synchronization** - Cross-instance cache consistency

## Conclusion

This Redis caching implementation provides a robust, scalable caching solution that significantly improves application performance while maintaining data consistency. The dual-layer approach (Spring Cache + Redis) ensures reliability and flexibility for various caching scenarios.
