package in.ShopSphere.ecommerce.dto.address;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressResponse {
    
    private String id;
    private String addressType;
    private String streetAddress;
    private String streetAddress2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Boolean isDefault;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private UserSummary user;
    
    @Data
    public static class UserSummary {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
