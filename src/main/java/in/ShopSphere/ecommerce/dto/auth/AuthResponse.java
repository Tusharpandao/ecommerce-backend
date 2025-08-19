package in.ShopSphere.ecommerce.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private UserDto user;
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private Boolean isBlocked;
        private Boolean emailVerified;
        private String phoneNumber;
        private String profileImage;
        private String createdAt;
        private String updatedAt;
    }
}
