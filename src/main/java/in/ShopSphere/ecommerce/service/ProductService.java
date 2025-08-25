package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.common.SearchFilters;
import in.ShopSphere.ecommerce.dto.product.ProductRequest;
import in.ShopSphere.ecommerce.dto.product.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    
    ProductResponse createProduct(ProductRequest request);
    
    ProductResponse updateProduct(String id, ProductRequest request);
    
    ProductResponse getProductById(String id);
    
    ProductResponse getProductBySku(String sku);
    
    PaginationResponse<ProductResponse> getAllProducts(Pageable pageable);
    
    PaginationResponse<ProductResponse> searchProducts(String searchTerm, Pageable pageable);
    
    PaginationResponse<ProductResponse> searchProductsWithFilters(SearchFilters filters, Pageable pageable);
    
    PaginationResponse<ProductResponse> getProductsByCategory(String categoryId, Pageable pageable);
    
    PaginationResponse<ProductResponse> getProductsBySeller(String sellerId, Pageable pageable);
    
    List<ProductResponse> getFeaturedProducts();
    
    List<ProductResponse> getLatestProducts(int limit);
    
    List<ProductResponse> getTopRatedProducts(int limit);
    
    List<ProductResponse> getDiscountedProducts();
    
    List<ProductResponse> getLowStockProducts();
    
    List<ProductResponse> getOutOfStockProducts();
    
    PaginationResponse<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    PaginationResponse<ProductResponse> getProductsByRating(Double minRating, Pageable pageable);
    
    ApiResponse deleteProduct(String id);
    
    ApiResponse toggleProductStatus(String id);
    
    ApiResponse toggleFeaturedStatus(String id);
    
    ApiResponse updateStock(String id, Integer quantity);
    
    ApiResponse updatePrice(String id, BigDecimal price, BigDecimal salePrice);
    
    void clearProductsCache();
}
