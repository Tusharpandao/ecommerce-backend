package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.dto.auth.AuthResponse;
import in.ShopSphere.ecommerce.dto.auth.LoginRequest;
import in.ShopSphere.ecommerce.dto.auth.RegisterRequest;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.exception.AuthenticationException;
import in.ShopSphere.ecommerce.exception.BusinessException;
import in.ShopSphere.ecommerce.exception.ValidationException;
import in.ShopSphere.ecommerce.mapper.UserMapper;
import in.ShopSphere.ecommerce.model.entity.User;
import in.ShopSphere.ecommerce.model.entity.UserRole;
import in.ShopSphere.ecommerce.repository.UserRepository;
import in.ShopSphere.ecommerce.security.JwtTokenProvider;
import in.ShopSphere.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    
    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userRepository.findByEmailAndIsBlockedFalse(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("User not found or blocked"));
            
            if (!user.getEmailVerified()) {
                throw new AuthenticationException("Please verify your email before logging in");
            }
            
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            String token = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);
            
            return AuthResponse.builder()
                .user(userMapper.toAuthUserDto(user))
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .build();
                
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getEmail(), e);
            throw new AuthenticationException("Invalid email or password");
        }
    }
    
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }
        
        if (request.getRole() == UserRole.ADMIN) {
            throw new ValidationException("Cannot register as admin");
        }
        
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .role(request.getRole())
            .phoneNumber(request.getPhoneNumber())
            .emailVerified(false)
            .isBlocked(false)
            .emailVerificationToken(generateVerificationToken())
            .build();
        
        user = userRepository.save(user);
        
        // TODO: Send verification email
        
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        return AuthResponse.builder()
            .user(userMapper.toAuthUserDto(user))
            .token(token)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .build();
    }
    
    @Override
    public ApiResponse logout() {
        SecurityContextHolder.clearContext();
        return ApiResponse.success("Logged out successfully");
    }
    
    @Override
    public ApiResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }
        
        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmailAndIsBlockedFalse(email)
            .orElseThrow(() -> new AuthenticationException("User not found"));
        
        String newToken = jwtTokenProvider.generateToken(user);
        
        return ApiResponse.success("Token refreshed successfully", newToken);
    }
    
    @Override
    public ApiResponse forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ValidationException("User not found"));
        
        String resetToken = generateResetToken();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        
        // TODO: Send password reset email
        
        return ApiResponse.success("Password reset instructions sent to your email");
    }
    
    @Override
    public ApiResponse resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new ValidationException("Invalid reset token"));
        
        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Reset token has expired");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userRepository.save(user);
        
        return ApiResponse.success("Password reset successfully");
    }
    
    @Override
    public ApiResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new ValidationException("Invalid verification token"));
        
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        
        return ApiResponse.success("Email verified successfully");
    }
    
    @Override
    public AuthResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated");
        }
        
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new AuthenticationException("User not found"));
        
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        return AuthResponse.builder()
            .user(userMapper.toAuthUserDto(user))
            .token(token)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .build();
    }
    
    @Override
    public ApiResponse changePassword(String currentPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new AuthenticationException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ApiResponse.success("Password changed successfully");
    }
    
    @Override
    public ApiResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ValidationException("User not found"));
        
        if (user.getEmailVerified()) {
            throw new BusinessException("Email is already verified");
        }
        
        String newToken = generateVerificationToken();
        user.setEmailVerificationToken(newToken);
        userRepository.save(user);
        
        // TODO: Send verification email
        
        return ApiResponse.success("Verification email sent");
    }
    
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
    
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}
