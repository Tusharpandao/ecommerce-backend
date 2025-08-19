package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.auth.AuthResponse;
import in.ShopSphere.ecommerce.dto.auth.LoginRequest;
import in.ShopSphere.ecommerce.dto.auth.RegisterRequest;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;

public interface AuthService {
    
    AuthResponse login(LoginRequest request);
    
    AuthResponse register(RegisterRequest request);
    
    ApiResponse logout();
    
    ApiResponse refreshToken(String refreshToken);
    
    ApiResponse forgotPassword(String email);
    
    ApiResponse resetPassword(String token, String newPassword);
    
    ApiResponse verifyEmail(String token);
    
    AuthResponse getCurrentUser();
    
    ApiResponse changePassword(String currentPassword, String newPassword);
    
    ApiResponse resendVerificationEmail(String email);
}
