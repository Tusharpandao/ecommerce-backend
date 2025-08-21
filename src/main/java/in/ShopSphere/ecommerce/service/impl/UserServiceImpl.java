package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.dto.address.AddressRequest;
import in.ShopSphere.ecommerce.dto.address.AddressResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.user.UserResponse;
import in.ShopSphere.ecommerce.dto.user.UserUpdateRequest;
import in.ShopSphere.ecommerce.exception.BusinessException;
import in.ShopSphere.ecommerce.exception.ResourceNotFoundException;
import in.ShopSphere.ecommerce.mapper.AddressMapper;
import in.ShopSphere.ecommerce.mapper.UserMapper;
import in.ShopSphere.ecommerce.model.entity.*;
import in.ShopSphere.ecommerce.repository.*;
import in.ShopSphere.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return userMapper.toUserResponse(currentUser);
    }
    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // This would need to be implemented with proper user retrieval
        // For now, returning a mock user
        throw new BusinessException("Current user retrieval not implemented");
    }

    @Override
    public UserResponse updateProfile(UserUpdateRequest request) {
        log.info("Updating user profile for user ID: {}", getCurrentAuthenticatedUser().getId());
        
        User currentUser = getCurrentAuthenticatedUser();
        
        // Update user fields
        if (request.getFirstName() != null) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            currentUser.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImage() != null) {
            currentUser.setProfileImage(request.getProfileImage());
        }
        
        currentUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(currentUser);
        
        log.info("User profile updated successfully");
        
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public ApiResponse changePassword(String currentPassword, String newPassword) {
        log.info("Changing password for user ID: {}", getCurrentAuthenticatedUser().getId());
        
        User currentUser = getCurrentAuthenticatedUser();
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        
        // Validate new password
        if (newPassword.length() < 8) {
            throw new BusinessException("New password must be at least 8 characters long");
        }
        
        // Update password
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);
        
        log.info("Password changed successfully");
        
        return ApiResponse.success(null, "Password changed successfully");
    }

    @Override
    public ApiResponse updateProfileImage(String imageUrl) {
        log.info("Updating profile image for user ID: {}", getCurrentAuthenticatedUser().getId());
        
        User currentUser = getCurrentAuthenticatedUser();
        currentUser.setProfileImage(imageUrl);
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);
        
        log.info("Profile image updated successfully");
        
        return ApiResponse.success(null, "Profile image updated successfully");
    }

    @Override
    public ApiResponse deleteAccount() {
        log.info("Deleting account for user ID: {}", getCurrentAuthenticatedUser().getId());
        
        User currentUser = getCurrentAuthenticatedUser();
        
        // Check if user has active orders
        long activeOrderCount = orderRepository.countByUserIdAndStatusNot(currentUser.getId(), OrderStatus.CANCELLED);
        if (activeOrderCount > 0) {
            throw new BusinessException("Cannot delete account with active orders");
        }
        
        // Delete user (this will cascade to related entities)
        userRepository.delete(currentUser);
        
        log.info("Account deleted successfully");
        
        return ApiResponse.success(null, "Account deleted successfully");
    }

    @Override
    public AddressResponse addAddress(AddressRequest request) {
        log.info("Adding address for user ID: {}", getCurrentAuthenticatedUser().getId());
        
        User currentUser = getCurrentAuthenticatedUser();
        
        // Create address
        Address address = addressMapper.toAddress(request);
        address.setUser(currentUser);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        
        // If this is the first address or marked as default, set as default
        if (address.getIsDefault() || addressRepository.countByUser(currentUser) == 0) {
            // Remove default from other addresses
            addressRepository.findByUserAndIsDefaultTrue(currentUser).forEach(addr -> {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            });
            address.setIsDefault(true);
        }
        
        Address savedAddress = addressRepository.save(address);
        
        log.info("Address added successfully with ID: {}", savedAddress.getId());
        
        return addressMapper.toAddressResponse(savedAddress);
    }

    @Override
        public AddressResponse updateAddress(String addressId, AddressRequest request) {
        log.info("Updating address: addressId={}", addressId);
        
        User currentUser = getCurrentAuthenticatedUser();   
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // Check if user owns this address
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only update your own addresses");
        }
        
        // Update address
        addressMapper.updateAddressFromRequest(request, address);
        address.setUpdatedAt(LocalDateTime.now());
        
        // Handle default address logic
        if (request.getIsDefault() != null && request.getIsDefault()) {
            // Remove default from other addresses
            addressRepository.findByUserAndIsDefaultTrue(currentUser).forEach(addr -> {
                if (!addr.getId().equals(addressId)) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            });
            address.setIsDefault(true);
        }
        
        Address updatedAddress = addressRepository.save(address);
        
        log.info("Address updated successfully");
        
        return addressMapper.toAddressResponse(updatedAddress);
    }

    @Override
    public ApiResponse deleteAddress(String addressId) {
        log.info("Deleting address: addressId={}", addressId);
        
        User currentUser = getCurrentAuthenticatedUser();
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // Check if user owns this address
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only delete your own addresses");
        }
        
        // Check if this is the default address
        if (address.getIsDefault()) {
            throw new BusinessException("Cannot delete default address. Set another address as default first.");
        }
        
        addressRepository.delete(address);
        
        log.info("Address deleted successfully");
        
        return ApiResponse.success(null, "Address deleted successfully");
    }

    @Override
    public AddressResponse setDefaultAddress(String addressId) {
        log.info("Setting default address: addressId={}", addressId);
        
        User currentUser = getCurrentAuthenticatedUser();
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        // Check if user owns this address
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only set your own addresses as default");
        }
        
        // Remove default from other addresses
        addressRepository.findByUserAndIsDefaultTrue(currentUser).forEach(addr -> {
            addr.setIsDefault(false);
            addressRepository.save(addr);
        });
        
        // Set this address as default
        address.setIsDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        Address updatedAddress = addressRepository.save(address);
        
        log.info("Default address set successfully");
        
        return addressMapper.toAddressResponse(updatedAddress);
    }

    @Override
    public List<AddressResponse> getUserAddresses() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDesc(currentUser);
        return addressMapper.toAddressResponseList(addresses);
    }

    // Admin methods
    @Override
    public PaginationResponse<UserResponse> getAllUsers(Pageable pageable) {
        // Only admins can view all users
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can view all users");
        }
        
        Page<User> users = userRepository.findAll(pageable);
        List<UserResponse> userResponses = userMapper.toUserResponseList(users.getContent());
        
        return PaginationResponse.<UserResponse>builder()
            .data(userResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(users.getNumber())
                .limit(users.getSize())
                .total(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build())
            .build();
    }

    @Override
    public PaginationResponse<UserResponse> getUsersByRole(UserRole role, Pageable pageable) {
        // Only admins can filter users by role
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can filter users by role");
        }
        
        Page<User> users = userRepository.findByRole(role, pageable);
        List<UserResponse> userResponses = userMapper.toUserResponseList(users.getContent());
        
        return PaginationResponse.<UserResponse>builder()
            .data(userResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(users.getNumber())
                .limit(users.getSize())
                .total(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build())
            .build();
    }

    @Override
    public PaginationResponse<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        // Only admins can search users
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can search users");
        }
        
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        List<UserResponse> userResponses = userMapper.toUserResponseList(users.getContent());
        
        return PaginationResponse.<UserResponse>builder()
            .data(userResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(users.getNumber())
                .limit(users.getSize())
                .total(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build())
            .build();
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(String id) {
        // Only admins can view other users
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(id)) {
            throw new BusinessException("You can only view your own profile");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        return userMapper.toUserResponse(user);
    }

    @Override
    public ApiResponse blockUser(String id) {
        log.info("Blocking user: userId={}", id);
        
        // Only admins can block users
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can block users");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        if (user.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Cannot block admin users");
        }
        
        user.setIsBlocked(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User blocked successfully");
        
        return ApiResponse.success(null, "User blocked successfully");
    }

    @Override
    public ApiResponse unblockUser(String id) {
        log.info("Unblocking user: userId={}", id);
        
        // Only admins can unblock users
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can unblock users");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.setIsBlocked(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User unblocked successfully");
        
        return ApiResponse.success(null, "User unblocked successfully");
    }

    @Override
    public ApiResponse updateUserRole(String id, UserRole newRole) {
        log.info("Updating user role: userId={}, newRole={}", id, newRole);
        
        // Only admins can update user roles
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can update user roles");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        if (user.getId().equals(currentUser.getId())) {
            throw new BusinessException("Cannot change your own role");
        }
        
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User role updated successfully");
        
        return ApiResponse.success(null, "User role updated successfully");
    }

    @Override
    public ApiResponse deleteUser(String id) {
        log.info("Deleting user: userId={}", id);
        
        // Only admins can delete users
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can delete users");
        }
        
        if (currentUser.getId().equals(id)) {
            throw new BusinessException("Cannot delete your own account");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Check if user has active orders
        long activeOrderCount = orderRepository.countByUserIdAndStatusNot(id, OrderStatus.CANCELLED);
        if (activeOrderCount > 0) {
            throw new BusinessException("Cannot delete user with active orders");
        }
        
        userRepository.delete(user);
        
        log.info("User deleted successfully");
        
        return ApiResponse.success(null, "User deleted successfully");
    }

    @Override
    public ApiResponse getUserStatistics() {
        // Only admins can view user statistics
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can view user statistics");
        }
        
        long registeredUsers = userRepository.count();
        long totalActiveUsers = userRepository.countByIsBlockedFalse();
        long totalBlockedUsers = userRepository.countByIsBlockedTrue();
        long currentVerifiedUsers = userRepository.countByEmailVerifiedTrue();
        long currentUnverifiedUsers = userRepository.countByEmailVerifiedFalse();
        
        // Create statistics object
        var stats = new Object() {
            public final long totalUsers = registeredUsers;
            public final long activeUsers = totalActiveUsers;
            public final long blockedUsers = totalBlockedUsers;
            public final long verifiedUsers = currentVerifiedUsers;
            public final long unverifiedUsers = currentUnverifiedUsers;
        };
        
        return ApiResponse.success(stats, "User statistics retrieved successfully");
    }

    @Override
    public Long getTotalUsers() {
        // Only admins can view user statistics
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can view user statistics");
        }
        
        return userRepository.count();
    }

    @Override
    public Long getUsersByRole(UserRole role) {
        // Only admins can view user statistics
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can view user statistics");
        }
        
        return userRepository.countByRole(role);
    }

    @Override
    public Long getActiveUsers() {
        // Only admins can view user statistics
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can view user statistics");
        }
        
        return userRepository.countByIsBlockedFalse();
    }

    @Override
    public Long getBlockedUsers() {
        // Only admins can view user statistics
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can view user statistics");
        }
        
        return userRepository.countByIsBlockedTrue();
    }

   
}
