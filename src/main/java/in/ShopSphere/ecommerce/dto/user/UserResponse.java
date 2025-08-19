package in.ShopSphere.ecommerce.dto.user;

import in.ShopSphere.ecommerce.model.entity.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Boolean isBlocked;
    private String phoneNumber;
    private String profileImage;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<AddressSummary> addresses;
    private Integer orderCount;
    private Integer reviewCount;
    private Integer wishlistCount;
    
    @Data
    public static class AddressSummary {
        private Long id;
        private String addressType;
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private Boolean isDefault;
    }
}
