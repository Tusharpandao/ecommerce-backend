package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.Address;
import in.ShopSphere.ecommerce.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUser(User user);
    
    List<Address> findByUserId(Long userId);
    
    List<Address> findByUserAndIsDefaultTrue(User user);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findByUserIdOrdered(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.addressType = :addressType")
    List<Address> findByUserIdAndAddressType(@Param("userId") Long userId, @Param("addressType") String addressType);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.city = :city")
    List<Address> findByUserIdAndCity(@Param("userId") Long userId, @Param("city") String city);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.state = :state")
    List<Address> findByUserIdAndState(@Param("userId") Long userId, @Param("state") String state);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.country = :country")
    List<Address> findByUserIdAndCountry(@Param("userId") Long userId, @Param("country") String country);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.zipCode = :zipCode")
    List<Address> findByUserIdAndZipCode(@Param("userId") Long userId, @Param("zipCode") String zipCode);
    
    boolean existsByUserIdAndIsDefaultTrue(Long userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.id != :addressId AND a.isDefault = true")
    List<Address> findOtherDefaultAddresses(@Param("userId") Long userId, @Param("addressId") Long addressId);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT a FROM Address a WHERE a.user = :user ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findByUserOrderByIsDefaultDesc(@Param("user") User user);
}
