package in.ShopSphere.ecommerce.controller;

import in.ShopSphere.ecommerce.dto.address.AddressRequest;
import in.ShopSphere.ecommerce.dto.address.AddressResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.user.UserResponse;
import in.ShopSphere.ecommerce.dto.user.UserUpdateRequest;
import in.ShopSphere.ecommerce.model.entity.UserRole;
import in.ShopSphere.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user", description = "Retrieves the current authenticated user's profile.")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Getting current user profile");
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile", description = "Updates the current user's profile information.")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating current user profile");
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Changes the current user's password.")
    public ResponseEntity<ApiResponse> changePassword(
            @Parameter(description = "Current password") @RequestParam String currentPassword,
            @Parameter(description = "New password") @RequestParam String newPassword) {
        log.info("Changing password for current user");
        ApiResponse response = userService.changePassword(currentPassword, newPassword);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update profile image", description = "Updates the current user's profile image.")
    public ResponseEntity<ApiResponse> updateProfileImage(
            @Parameter(description = "Profile image URL") @RequestParam String imageUrl) {
        log.info("Updating profile image for current user");
        ApiResponse response = userService.updateProfileImage(imageUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete account", description = "Deletes the current user's account.")
    public ResponseEntity<ApiResponse> deleteAccount() {
        log.info("Deleting current user account");
        ApiResponse response = userService.deleteAccount();
        return ResponseEntity.ok(response);
    }

    // Address management
    @GetMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user addresses", description = "Retrieves all addresses for the current user.")
    public ResponseEntity<List<AddressResponse>> getUserAddresses() {
        log.info("Getting addresses for current user");
        List<AddressResponse> response = userService.getUserAddresses();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add address", description = "Adds a new address for the current user.")
    public ResponseEntity<AddressResponse> addAddress(
            @Valid @RequestBody AddressRequest request) {
        log.info("Adding address for current user");
        AddressResponse response = userService.addAddress(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update address", description = "Updates an existing address for the current user.")
    public ResponseEntity<AddressResponse> updateAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        log.info("Updating address: addressId={}", addressId);
        AddressResponse response = userService.updateAddress(addressId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete address", description = "Deletes an address for the current user.")
    public ResponseEntity<ApiResponse> deleteAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        log.info("Deleting address: addressId={}", addressId);
        ApiResponse response = userService.deleteAddress(addressId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set default address", description = "Sets an address as the default for the current user.")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        log.info("Setting default address: addressId={}", addressId);
        AddressResponse response = userService.setDefaultAddress(addressId);
        return ResponseEntity.ok(response);
    }

    // Admin operations
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination. Admin only.")
    public ResponseEntity<PaginationResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting all users");
        PaginationResponse<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Retrieves users by role with pagination. Admin only.")
    public ResponseEntity<PaginationResponse<UserResponse>> getUsersByRole(
            @Parameter(description = "User role") @PathVariable UserRole role,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting users by role: {}", role);
        PaginationResponse<UserResponse> response = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users", description = "Searches users by various criteria. Admin only.")
    public ResponseEntity<PaginationResponse<UserResponse>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching users with term: {}", searchTerm);
        PaginationResponse<UserResponse> response = userService.searchUsers(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by ID. Admin only.")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Getting user by ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block user", description = "Blocks a user. Admin only.")
    public ResponseEntity<ApiResponse> blockUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Blocking user: userId={}", id);
        ApiResponse response = userService.blockUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unblock user", description = "Unblocks a user. Admin only.")
    public ResponseEntity<ApiResponse> unblockUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Unblocking user: userId={}", id);
        ApiResponse response = userService.unblockUser(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Updates a user's role. Admin only.")
    public ResponseEntity<ApiResponse> updateUserRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "New role") @RequestParam UserRole role) {
        log.info("Updating user role: userId={}, newRole={}", id, role);
        ApiResponse response = userService.updateUserRole(id, role);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user. Admin only.")
    public ResponseEntity<ApiResponse> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Deleting user: userId={}", id);
        ApiResponse response = userService.deleteUser(id);
        return ResponseEntity.ok(response);
    }

    // Statistics


    @GetMapping("/statistics/total")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get total users count", description = "Retrieves total users count. Admin only.")
    public ResponseEntity<Long> getTotalUsers() {
        log.info("Getting total users count");
        Long count = userService.getTotalUsers();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users count by role", description = "Retrieves users count by role. Admin only.")
    public ResponseEntity<Long> getUsersByRole(
            @Parameter(description = "User role") @PathVariable UserRole role) {
        log.info("Getting users count by role: {}", role);
        Long count = userService.getUsersByRole(role);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active users count", description = "Retrieves active users count. Admin only.")
    public ResponseEntity<Long> getActiveUsers() {
        log.info("Getting active users count");
        Long count = userService.getActiveUsers();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/blocked")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get blocked users count", description = "Retrieves blocked users count. Admin only.")
    public ResponseEntity<Long> getBlockedUsers() {
        log.info("Getting blocked users count");
        Long count = userService.getBlockedUsers();
        return ResponseEntity.ok(count);
    }
}
