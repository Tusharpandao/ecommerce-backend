package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.Cart;
import in.ShopSphere.ecommerce.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    
    Optional<Cart> findByUser(User user);
    
    Optional<Cart> findByUserId(String userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") String userId);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItemsAndProducts(@Param("userId") String userId);
    
    boolean existsByUserId(String userId);
    
    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :date")
    List<Cart> findAbandonedCarts(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.items IS NOT EMPTY")
    Optional<Cart> findNonEmptyCartByUserId(@Param("userId") String userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND SIZE(c.items) > 0")
    Optional<Cart> findCartWithItemsByUserId(@Param("userId") String userId);
}
    