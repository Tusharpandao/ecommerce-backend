package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.model.entity.Category;
import in.ShopSphere.ecommerce.model.entity.Product;
import in.ShopSphere.ecommerce.model.entity.User;
import in.ShopSphere.ecommerce.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private CacheServiceImpl cacheService;

    private Product testProduct;
    private Category testCategory;
    private User testUser;

    @BeforeEach
    void setUp() {
        cacheService = new CacheServiceImpl(redisTemplate, cacheManager);

        // Setup test data
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setSku("TEST-001");
        testProduct.setIsActive(true);
        testProduct.setCreatedAt(LocalDateTime.now());

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");
        testCategory.setIsActive(true);
        testCategory.setCreatedAt(LocalDateTime.now());

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(in.ShopSphere.ecommerce.model.entity.UserRole.CUSTOMER);
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup Redis template mocks
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCacheProduct() {
        // Given
        when(cacheManager.getCache("products")).thenReturn(cache);

        // When
        cacheService.cacheProduct(testProduct);

        // Then
        verify(valueOperations).set(eq("product:1"), eq(testProduct), any());
        verify(cache).put(eq(1L), eq(testProduct));
    }

    @Test
    void testGetCachedProduct_FromSpringCache() {
        // Given
        when(cacheManager.getCache("products")).thenReturn(cache);
        when(cache.get(1L)).thenReturn(new Cache.ValueWrapper() {
            @Override
            public Object get() {
                return testProduct;
            }
        });

        // When
        Optional<Product> result = cacheService.getCachedProduct(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProduct, result.get());
        verify(cache).get(1L);
        verify(valueOperations, never()).get(any());
    }

    @Test
    void testGetCachedProduct_FromRedis() {
        // Given
        when(cacheManager.getCache("products")).thenReturn(cache);
        when(cache.get(1L)).thenReturn(null);
        when(valueOperations.get("product:1")).thenReturn(testProduct);

        // When
        Optional<Product> result = cacheService.getCachedProduct(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProduct, result.get());
        verify(cache).get(1L);
        verify(valueOperations).get("product:1");
    }

    @Test
    void testGetCachedProduct_NotFound() {
        // Given
        when(cacheManager.getCache("products")).thenReturn(cache);
        when(cache.get(1L)).thenReturn(null);
        when(valueOperations.get("product:1")).thenReturn(null);

        // When
        Optional<Product> result = cacheService.getCachedProduct(1L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testInvalidateProductCache() {
        // Given
        when(cacheManager.getCache("products")).thenReturn(cache);

        // When
        cacheService.invalidateProductCache(1L);

        // Then
        verify(cache).evict(1L);
        verify(redisTemplate).delete("product:1");
    }

    @Test
    void testCacheCategory() {
        // Given
        when(cacheManager.getCache("categories")).thenReturn(cache);

        // When
        cacheService.cacheCategory(testCategory);

        // Then
        verify(valueOperations).set(eq("category:1"), eq(testCategory), any());
        verify(cache).put(eq(1L), eq(testCategory));
    }

    @Test
    void testGetCachedCategory_FromSpringCache() {
        // Given
        when(cacheManager.getCache("categories")).thenReturn(cache);
        when(cache.get(1L)).thenReturn(new Cache.ValueWrapper() {
            @Override
            public Object get() {
                return testCategory;
            }
        });

        // When
        Optional<Category> result = cacheService.getCachedCategory(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCategory, result.get());
        verify(cache).get(1L);
        verify(valueOperations, never()).get(any());
    }

    @Test
    void testCacheCart() {
        // Given
        when(cacheManager.getCache("carts")).thenReturn(cache);
        Object cartData = "cart data";

        // When
        cacheService.cacheCart(1L, cartData);

        // Then
        verify(valueOperations).set(eq("cart:1"), eq(cartData), any());
        verify(cache).put(eq(1L), eq(cartData));
    }

    @Test
    void testGetCachedCart_FromSpringCache() {
        // Given
        when(cacheManager.getCache("carts")).thenReturn(cache);
        Object cartData = "cart data";
        when(cache.get(1L)).thenReturn(new Cache.ValueWrapper() {
            @Override
            public Object get() {
                return cartData;
            }
        });

        // When
        Optional<Object> result = cacheService.getCachedCart(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(cartData, result.get());
        verify(cache).get(1L);
        verify(valueOperations, never()).get(any());
    }

    @Test
    void testInvalidateCartCache() {
        // Given
        when(cacheManager.getCache("carts")).thenReturn(cache);

        // When
        cacheService.invalidateCartCache(1L);

        // Then
        verify(cache).evict(1L);
        verify(redisTemplate).delete("cart:1");
    }

    @Test
    void testCacheSearchResults() {
        // Given
        when(cacheManager.getCache("search")).thenReturn(cache);
        Object searchResults = "search results";
        String searchKey = "test search";

        // When
        cacheService.cacheSearchResults(searchKey, searchResults);

        // Then
        verify(valueOperations).set(eq("search:" + searchKey.hashCode()), eq(searchResults), any());
        verify(cache).put(eq(searchKey.hashCode()), eq(searchResults));
    }

    @Test
    void testGetCachedSearchResults_FromSpringCache() {
        // Given
        when(cacheManager.getCache("search")).thenReturn(cache);
        Object searchResults = "search results";
        String searchKey = "test search";
        when(cache.get(searchKey.hashCode())).thenReturn(new Cache.ValueWrapper() {
            @Override
            public Object get() {
                return searchResults;
            }
        });

        // When
        Optional<Object> result = cacheService.getCachedSearchResults(searchKey);

        // Then
        assertTrue(result.isPresent());
        assertEquals(searchResults, result.get());
        verify(cache).get(searchKey.hashCode());
        verify(valueOperations, never()).get(any());
    }

    @Test
    void testCacheGeneric() {
        // Given
        String key = "test:key";
        String value = "test value";
        long ttl = 3600;

        // When
        cacheService.cache(key, value, ttl);

        // Then
        verify(valueOperations).set(eq(key), eq(value), any());
    }

    @Test
    void testGetGeneric() {
        // Given
        String key = "test:key";
        String value = "test value";
        when(valueOperations.get(key)).thenReturn(value);

        // When
        Optional<String> result = cacheService.get(key, String.class);

        // Then
        assertTrue(result.isPresent());
        assertEquals(value, result.get());
    }

    @Test
    void testInvalidate() {
        // Given
        String key = "test:key";

        // When
        cacheService.invalidate(key);

        // Then
        verify(redisTemplate).delete(key);
    }

    @Test
    void testClearCache() {
        // Given
        String cacheName = "products";
        Set<String> keys = Set.of("product:1", "product:2");
        when(redisTemplate.keys(cacheName + "*")).thenReturn(keys);
        when(cacheManager.getCache(cacheName)).thenReturn(cache);

        // When
        cacheService.clearCache(cacheName);

        // Then
        verify(cache).clear();
        verify(redisTemplate).delete(keys);
    }

    @Test
    void testClearAllCaches() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(Set.of("products", "categories"));
        when(cacheManager.getCache("products")).thenReturn(cache);
        when(cacheManager.getCache("categories")).thenReturn(cache);

        // When
        cacheService.clearAllCaches();

        // Then
        verify(cache, times(2)).clear();
        verify(redisTemplate.getConnectionFactory().getConnection()).flushAll();
    }

    @Test
    void testGetCacheSize() {
        // Given
        String cacheName = "products";
        Set<String> keys = Set.of("product:1", "product:2");
        when(redisTemplate.keys(cacheName + "*")).thenReturn(keys);

        // When
        long size = cacheService.getCacheSize(cacheName);

        // Then
        assertEquals(2, size);
    }

    @Test
    void testCacheProduct_NullProduct() {
        // When
        cacheService.cacheProduct(null);

        // Then
        verify(valueOperations, never()).set(any(), any(), any());
        verify(cacheManager, never()).getCache(any());
    }

    @Test
    void testCacheProduct_NullId() {
        // Given
        testProduct.setId(null);

        // When
        cacheService.cacheProduct(testProduct);

        // Then
        verify(valueOperations, never()).set(any(), any(), any());
        verify(cacheManager, never()).getCache(any());
    }

    @Test
    void testGetCachedProduct_NullId() {
        // When
        Optional<Product> result = cacheService.getCachedProduct(null);

        // Then
        assertFalse(result.isPresent());
        verify(cacheManager, never()).getCache(any());
        verify(valueOperations, never()).get(any());
    }
}
