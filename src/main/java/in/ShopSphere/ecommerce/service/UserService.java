package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.address.AddressRequest;
import in.ShopSphere.ecommerce.dto.address.AddressResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.user.UserResponse;
import in.ShopSphere.ecommerce.dto.user.UserUpdateRequest;
import in.ShopSphere.ecommerce.model.entity.UserRole;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    
    UserResponse getCurrentUser();
    
    UserResponse updateProfile(UserUpdateRequest request);
    
    ApiResponse changePassword(String currentPassword, String newPassword);
    
    ApiResponse updateProfileImage(String imageUrl);
    
    ApiResponse deleteAccount();
    
    // Address management
    List<AddressResponse> getUserAddresses();
    
    AddressResponse addAddress(AddressRequest request);
    
    AddressResponse updateAddress(Long id, AddressRequest request);
    
    ApiResponse deleteAddress(Long id);
    
    AddressResponse setDefaultAddress(Long id);
    
    // Admin operations
    PaginationResponse<UserResponse> getAllUsers(Pageable pageable);
    
    PaginationResponse<UserResponse> getUsersByRole(UserRole role, Pageable pageable);
    
    PaginationResponse<UserResponse> searchUsers(String searchTerm, Pageable pageable);
    
    UserResponse getUserById(Long id);
    
    ApiResponse blockUser(Long id);
    
    ApiResponse unblockUser(Long id);
    
    ApiResponse updateUserRole(Long id, UserRole role);
    
    ApiResponse deleteUser(Long id);
    
    // Statistics
    Long getTotalUsers();
    
    Long getUsersByRole(UserRole role);
    
    Long getActiveUsers();
    
    Long getBlockedUsers();
    ApiResponse getUserStatistics();
}
