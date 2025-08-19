package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.Wishlist;
import in.ShopSphere.ecommerce.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, String> {
    
    List<Wishlist> findByUser(User user);
    
    List<Wishlist> findByUserId(Long userId);
    
    Page<Wishlist> findByUserId(Long userId, Pageable pageable);
    
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.isActive = true ORDER BY w.createdAt DESC")
    List<Wishlist> findActiveProductsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.isActive = true ORDER BY w.createdAt DESC")
    Page<Wishlist> findActiveProductsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.category.id = :categoryId")
    List<Wishlist> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.price BETWEEN :minPrice AND :maxPrice")
    List<Wishlist> findByUserIdAndPriceRange(@Param("userId") Long userId, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.rating >= :minRating")
    List<Wishlist> findByUserIdAndMinRating(@Param("userId") Long userId, @Param("minRating") Double minRating);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND (w.product.name LIKE %:searchTerm% OR w.product.description LIKE %:searchTerm%)")
    Page<Wishlist> searchWishlistByUserId(@Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId AND w.product.isActive = true")
    long countActiveProductsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.stockQuantity > 0")
    List<Wishlist> findInStockProductsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.stockQuantity = 0")
    List<Wishlist> findOutOfStockProductsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.salePrice IS NOT NULL AND w.product.salePrice < w.product.price")
    List<Wishlist> findDiscountedProductsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.isFeatured = true")
    List<Wishlist> findFeaturedProductsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.seller.id = :sellerId")
    List<Wishlist> findByUserIdAndSellerId(@Param("userId") Long userId, @Param("sellerId") Long sellerId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.category.id IN :categoryIds")
    List<Wishlist> findByUserIdAndCategoryIds(@Param("userId") Long userId, @Param("categoryIds") List<Long> categoryIds);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.price <= :maxPrice")
    List<Wishlist> findByUserIdAndMaxPrice(@Param("userId") Long userId, @Param("maxPrice") Double maxPrice);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.price >= :minPrice")
    List<Wishlist> findByUserIdAndMinPrice(@Param("userId") Long userId, @Param("minPrice") Double minPrice);
}
