package in.ShopSphere.ecommerce.dto.address;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddressRequest {
    
    @NotBlank(message = "Address type is required")
    @Size(max = 20, message = "Address type must not exceed 20 characters")
    private String addressType;
    
    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address must not exceed 255 characters")
    private String streetAddress;
    
    @Size(max = 255, message = "Street address 2 must not exceed 255 characters")
    private String streetAddress2;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;
    
    @NotBlank(message = "ZIP code is required")
    @Size(max = 20, message = "ZIP code must not exceed 20 characters")
    private String zipCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
    
    private Boolean isDefault = false;
}
