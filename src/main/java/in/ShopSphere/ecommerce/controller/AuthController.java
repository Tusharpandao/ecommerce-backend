package in.ShopSphere.ecommerce.controller;

import in.ShopSphere.ecommerce.dto.auth.AuthResponse;
import in.ShopSphere.ecommerce.dto.auth.LoginRequest;
import in.ShopSphere.ecommerce.dto.auth.RegisterRequest;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.login(request);
            
            // Set JWT token in HTTP-only cookie
            response.addHeader("Set-Cookie", 
                "jwt=" + authResponse.getToken() + "; HttpOnly; Secure; SameSite=Strict; Max-Age=" + 
                (authResponse.getExpiresIn() / 1000));
            
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Login failed", e.getMessage()));
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse authResponse = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authResponse, "Registration successful"));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Registration failed", e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and clear session")
    public ResponseEntity<ApiResponse> logout(HttpServletResponse response) {
        try {
            ApiResponse result = authService.logout();
            
            // Clear JWT cookie
            response.addHeader("Set-Cookie", 
                "jwt=; HttpOnly; Secure; SameSite=Strict; Max-Age=0");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Logout failed", e.getMessage()));
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT access token using refresh token")
    public ResponseEntity<ApiResponse> refreshToken(@RequestParam String refreshToken) {
        try {
            ApiResponse result = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token refresh failed", e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        try {
            ApiResponse result = authService.forgotPassword(email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Forgot password failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Forgot password failed", e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using reset token")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            ApiResponse result = authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Password reset failed", e.getMessage()));
        }
    }
    
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify email using verification token")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        try {
            ApiResponse result = authService.verifyEmail(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Email verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email verification failed", e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        try {
            AuthResponse authResponse = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success(authResponse, "User retrieved successfully"));
        } catch (Exception e) {
            log.error("Get current user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Get current user failed", e.getMessage()));
        }
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change current user password")
    public ResponseEntity<ApiResponse> changePassword(@RequestParam String currentPassword, @RequestParam String newPassword) {
        try {
            ApiResponse result = authService.changePassword(currentPassword, newPassword);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Password change failed", e.getMessage()));
        }
    }
    
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resend email verification link")
    public ResponseEntity<ApiResponse> resendVerificationEmail(@RequestParam String email) {
        try {
            ApiResponse result = authService.resendVerificationEmail(email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Resend verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Resend verification failed", e.getMessage()));
        }
    }
}
