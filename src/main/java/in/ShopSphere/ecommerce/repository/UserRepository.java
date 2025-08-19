package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.User;
import in.ShopSphere.ecommerce.model.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndIsBlockedFalse(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);
    
    List<User> findByRole(UserRole role);
    
    Page<User> findByRole(UserRole role, Pageable pageable);
    
    Page<User> findByIsBlocked(Boolean isBlocked, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isBlocked = false")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findUsersWithInactiveLogin(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt >= :startDate")
    long countUsersByRoleAndDateRange(@Param("role") UserRole role, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:searchTerm% OR u.firstName LIKE %:searchTerm% OR u.lastName LIKE %:searchTerm%")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :date")
    List<User> findUnverifiedUsersOlderThan(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isBlocked = false")
    long countByIsBlockedFalse();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isBlocked = true")
    long countByIsBlockedTrue();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
    long countByEmailVerifiedTrue();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = false")
    long countByEmailVerifiedFalse();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
}
