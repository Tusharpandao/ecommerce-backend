package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.Review;
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
public interface ReviewRepository extends JpaRepository<Review, String> {
    
    List<Review> findByProductId(Long productId);
    
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    List<Review> findByProductIdAndIsApprovedTrue(Long productId);
    
    Page<Review> findByProductIdAndIsApprovedTrue(Long productId, Pageable pageable);
    
    List<Review> findByUser(User user);
    
    Page<Review> findByUser(User user, Pageable pageable);
    
    List<Review> findByUserId(Long userId);
    
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    List<Review> findByProductIdAndUserId(Long productId, Long userId);
    
    Optional<Review> findByProductIdAndUserIdAndIsApprovedTrue(Long productId, Long userId);
    
    List<Review> findByIsApproved(Boolean isApproved);
    
    Page<Review> findByIsApproved(Boolean isApproved, Pageable pageable);
    
    List<Review> findByRating(Integer rating);
    
    List<Review> findByProductIdAndRating(Long productId, Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating >= :minRating AND r.isApproved = true")
    List<Review> findByProductIdAndMinRating(@Param("productId") Long productId, @Param("minRating") Integer minRating);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating BETWEEN :minRating AND :maxRating AND r.isApproved = true")
    List<Review> findByProductIdAndRatingRange(@Param("productId") Long productId, @Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findRecentApprovedReviewsByProduct(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isApproved = true ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findTopRatedApprovedReviewsByProduct(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByUser(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Review r WHERE r.isApproved = false ORDER BY r.createdAt ASC")
    Page<Review> findPendingReviews(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isApproved = false ORDER BY r.createdAt ASC")
    List<Review> findPendingReviewsByProduct(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    long countApprovedReviewsByProduct(@Param("productId") Long productId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    Double getAverageRatingByProduct(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.rating = :rating AND r.isApproved = true")
    long countReviewsByProductAndRating(@Param("productId") Long productId, @Param("rating") Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.title LIKE %:searchTerm% OR r.comment LIKE %:searchTerm%")
    Page<Review> searchReviews(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND (r.title LIKE %:searchTerm% OR r.comment LIKE %:searchTerm%)")
    Page<Review> searchReviewsByProduct(@Param("productId") Long productId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND (r.title LIKE %:searchTerm% OR r.comment LIKE %:searchTerm%)")
    Page<Review> searchReviewsByUser(@Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.user.id = :userId AND r.id != :reviewId")
    Optional<Review> findOtherReviewByProductAndUser(@Param("productId") Long productId, @Param("userId") Long userId, @Param("reviewId") Long reviewId);
}
