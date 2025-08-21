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
    
    List<Wishlist> findByUserId(String userId);
    
    Page<Wishlist> findByUserId(String userId, Pageable pageable);
    
    Optional<Wishlist> findByUserIdAndProductId(String userId, String productId);
    
    boolean existsByUserIdAndProductId(String userId, String productId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.isActive = true ORDER BY w.createdAt DESC")
    List<Wishlist> findActiveProductsByUserId(@Param("userId") String userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.isActive = true ORDER BY w.createdAt DESC")
    Page<Wishlist> findActiveProductsByUserId(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.category.id = :categoryId")
    List<Wishlist> findByUserIdAndCategoryId(@Param("userId") String userId, @Param("categoryId") String categoryId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.price BETWEEN :minPrice AND :maxPrice")
    List<Wishlist> findByUserIdAndPriceRange(@Param("userId") String userId, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.rating >= :minRating")
    List<Wishlist> findByUserIdAndMinRating(@Param("userId") String userId, @Param("minRating") Double minRating);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND (w.product.name LIKE %:searchTerm% OR w.product.description LIKE %:searchTerm%)")
    Page<Wishlist> searchWishlistByUserId(@Param("userId") String userId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId AND w.product.isActive = true")
    long countActiveProductsByUserId(@Param("userId") String userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.stockQuantity > 0")
    List<Wishlist> findInStockProductsByUserId(@Param("userId") String userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.stockQuantity = 0")
    List<Wishlist> findOutOfStockProductsByUserId(@Param("userId") String userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.salePrice IS NOT NULL AND w.product.salePrice < w.product.price")
    List<Wishlist> findDiscountedProductsByUserId(@Param("userId") String userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.isFeatured = true")
    List<Wishlist> findFeaturedProductsByUserId(@Param("userId") String userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.seller.id = :sellerId")
    List<Wishlist> findByUserIdAndSellerId(@Param("userId") String userId, @Param("sellerId") String sellerId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.category.id IN :categoryIds")
    List<Wishlist> findByUserIdAndCategoryIds(@Param("userId") String userId, @Param("categoryIds") List<String> categoryIds);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.price <= :maxPrice")
    List<Wishlist> findByUserIdAndMaxPrice(@Param("userId") String userId, @Param("maxPrice") Double maxPrice);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.product.price >= :minPrice")
    List<Wishlist> findByUserIdAndMinPrice(@Param("userId") String userId, @Param("minPrice") Double minPrice);
}
