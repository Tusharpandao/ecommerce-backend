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
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    Optional<Cart> findByUser(User user);
    
    Optional<Cart> findByUserId(Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItemsAndProducts(@Param("userId") Long userId);
    
    boolean existsByUserId(Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :date")
    List<Cart> findAbandonedCarts(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.items IS NOT EMPTY")
    Optional<Cart> findNonEmptyCartByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND SIZE(c.items) > 0")
    Optional<Cart> findCartWithItemsByUserId(@Param("userId") Long userId);
}
