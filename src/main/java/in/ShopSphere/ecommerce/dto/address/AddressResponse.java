package in.ShopSphere.ecommerce.dto.address;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressResponse {
    
    private Long id;
    private String addressType;
    private String streetAddress;
    private String streetAddress2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private UserSummary user;
    
    @Data
    public static class UserSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
