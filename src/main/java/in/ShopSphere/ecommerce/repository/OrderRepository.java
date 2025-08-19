package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.Order;
import in.ShopSphere.ecommerce.model.entity.OrderStatus;
import in.ShopSphere.ecommerce.model.entity.PaymentStatus;
import in.ShopSphere.ecommerce.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByUser(User user);
    
    Page<Order> findByUser(User user, Pageable pageable);
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    List<Order> findByStatus(OrderStatus status);
    
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);
    
    Page<Order> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);
    
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    
    List<Order> findByUserAndPaymentStatus(User user, PaymentStatus paymentStatus);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.paymentStatus = :paymentStatus")
    List<Order> findByUserIdAndPaymentStatus(@Param("userId") Long userId, @Param("paymentStatus") PaymentStatus paymentStatus);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate")
    List<Order> findByCreatedAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT o FROM Order o WHERE o.estimatedDeliveryDate = :date")
    List<Order> findByEstimatedDeliveryDate(@Param("date") LocalDate date);
    
    @Query("SELECT o FROM Order o WHERE o.actualDeliveryDate = :date")
    List<Order> findByActualDeliveryDate(@Param("date") LocalDate date);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :startDate")
    List<Order> findByStatusAndCreatedAfter(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :paymentStatus AND o.createdAt >= :startDate")
    List<Order> findByPaymentStatusAndCreatedAfter(@Param("paymentStatus") PaymentStatus paymentStatus, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount")
    Page<Order> findByMinTotalAmount(@Param("minAmount") Double minAmount, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :minAmount AND :maxAmount")
    Page<Order> findByTotalAmountRange(@Param("minAmount") Double minAmount, @Param("maxAmount") Double maxAmount, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderNumber LIKE %:searchTerm% OR o.user.email LIKE %:searchTerm%")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND (o.orderNumber LIKE %:searchTerm% OR o.status = :status)")
    Page<Order> searchOrdersByUser(@Param("userId") Long userId, @Param("searchTerm") String searchTerm, @Param("status") OrderStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt >= :startDate")
    long countByStatusAndDateRange(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = :paymentStatus AND o.createdAt >= :startDate")
    long countByPaymentStatusAndDateRange(@Param("paymentStatus") PaymentStatus paymentStatus, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findRecentOrdersByUser(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    Page<Order> findByStatuses(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.paymentStatus IN :paymentStatuses")
    Page<Order> findByPaymentStatuses(@Param("paymentStatuses") List<PaymentStatus> paymentStatuses, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN :statuses")
    List<Order> findByUserIdAndStatuses(@Param("userId") Long userId, @Param("statuses") List<OrderStatus> statuses);
    
    boolean existsByOrderNumber(String orderNumber);
    
    @Query("SELECT MAX(o.orderNumber) FROM Order o WHERE o.orderNumber LIKE :prefix%")
    String findLastOrderNumberByPrefix(@Param("prefix") String prefix);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :minAmount AND :maxAmount ORDER BY o.createdAt DESC")
    Page<Order> findByAmountRange(@Param("minAmount") Double minAmount, @Param("maxAmount") Double maxAmount, Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status != :status")
    long countByUserIdAndStatusNot(@Param("userId") Long userId, @Param("status") OrderStatus status);
}
