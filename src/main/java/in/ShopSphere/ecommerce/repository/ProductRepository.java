package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.Product;
import in.ShopSphere.ecommerce.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    Optional<Product> findByBarcode(String barcode);
    
    List<Product> findBySeller(User seller);
    
    List<Product> findBySellerAndIsActiveTrue(User seller);
    
    Page<Product> findBySeller(User seller, Pageable pageable);
    
    Page<Product> findBySellerAndIsActiveTrue(User seller, Pageable pageable);
    
    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);
    
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
    
    List<Product> findByIsActiveTrue();
    
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();
    
    Page<Product> findByIsFeaturedTrueAndIsActiveTrue(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity <= p.minStockLevel")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.rating >= :minRating")
    Page<Product> findByMinRating(@Param("minRating") Double minRating, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm% OR p.sku LIKE %:searchTerm%)")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.category.id = :categoryId AND (p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%)")
    Page<Product> searchProductsInCategory(@Param("categoryId") Long categoryId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.rating DESC, p.reviewCount DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Page<Product> findLatestProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.salePrice IS NOT NULL AND p.salePrice < p.price")
    Page<Product> findDiscountedProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.category.id IN :categoryIds")
    Page<Product> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.seller.id = :sellerId AND p.category.id = :categoryId")
    Page<Product> findBySellerAndCategory(@Param("sellerId") Long sellerId, @Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.isActive = true")
    long countActiveProductsBySeller(@Param("sellerId") Long sellerId);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity > 0 ORDER BY p.stockQuantity ASC")
    List<Product> findProductsByStockAscending();
    
    boolean existsBySku(String sku);
    
    boolean existsBySkuAndIdNot(String sku, Long id);
    
    boolean existsByBarcode(String barcode);
    
    boolean existsByBarcodeAndIdNot(String barcode, Long id);
}
