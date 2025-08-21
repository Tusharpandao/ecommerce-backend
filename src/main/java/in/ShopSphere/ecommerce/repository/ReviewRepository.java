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
    
    List<Review> findByProductId(String productId);
    
    Page<Review> findByProductId(String productId, Pageable pageable);
    
    List<Review> findByProductIdAndIsApprovedTrue(String productId);
    
    Page<Review> findByProductIdAndIsApprovedTrue(String productId, Pageable pageable);
    
    List<Review> findByUser(User user);
    
    Page<Review> findByUser(User user, Pageable pageable);
    
    List<Review> findByUserId(String userId);
    
    Page<Review> findByUserId(String userId, Pageable pageable);
    
    List<Review> findByProductIdAndUserId(String productId, String userId);
    
    Optional<Review> findByProductIdAndUserIdAndIsApprovedTrue(String productId, String userId);
    
    List<Review> findByIsApproved(Boolean isApproved);
    
    Page<Review> findByIsApproved(Boolean isApproved, Pageable pageable);
    
    List<Review> findByRating(Integer rating);
    
    List<Review> findByProductIdAndRating(String productId, Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating >= :minRating AND r.isApproved = true")
    List<Review> findByProductIdAndMinRating(@Param("productId") String productId, @Param("minRating") Integer minRating);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating BETWEEN :minRating AND :maxRating AND r.isApproved = true")
    List<Review> findByProductIdAndRatingRange(@Param("productId") String productId, @Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findRecentApprovedReviewsByProduct(@Param("productId") String productId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isApproved = true ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findTopRatedApprovedReviewsByProduct(@Param("productId") String productId);
    
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.isApproved = true ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByUser(@Param("userId") String userId);
    
    @Query("SELECT r FROM Review r WHERE r.isApproved = false ORDER BY r.createdAt ASC")
    Page<Review> findPendingReviews(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isApproved = false ORDER BY r.createdAt ASC")
        List<Review> findPendingReviewsByProduct(@Param("productId") String productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    long countApprovedReviewsByProduct(@Param("productId") String productId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    Double getAverageRatingByProduct(@Param("productId") String productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.rating = :rating AND r.isApproved = true")
    long countReviewsByProductAndRating(@Param("productId") String productId, @Param("rating") Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.title LIKE %:searchTerm% OR r.comment LIKE %:searchTerm%")
    Page<Review> searchReviews(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND (r.title LIKE %:searchTerm% OR r.comment LIKE %:searchTerm%)")
    Page<Review> searchReviewsByProduct(@Param("productId") String productId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND (r.title LIKE %:searchTerm% OR r.comment LIKE %:searchTerm%)")
    Page<Review> searchReviewsByUser(@Param("userId") String userId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    boolean existsByProductIdAndUserId(String productId, String userId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.user.id = :userId AND r.id != :reviewId")
    Optional<Review> findOtherReviewByProductAndUser(@Param("productId") String productId, @Param("userId") String userId, @Param("reviewId") String reviewId);
}
