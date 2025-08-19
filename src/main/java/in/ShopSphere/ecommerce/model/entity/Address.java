package in.ShopSphere.ecommerce.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Size(max = 50, message = "Address type must not exceed 50 characters")
    @Column(name = "address_type")
    private String addressType = "shipping"; // shipping, billing
    
    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address must not exceed 255 characters")
    @Column(name = "street_address", nullable = false)
    private String streetAddress;
    
    @Size(max = 255, message = "Street address 2 must not exceed 255 characters")
    @Column(name = "street_address2")
    private String streetAddress2;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(nullable = false)
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    @Column(nullable = false)
    private String state;
    
    @NotBlank(message = "ZIP code is required")
    @Size(max = 20, message = "ZIP code must not exceed 20 characters")
    @Column(name = "zip_code", nullable = false)
    private String zipCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(nullable = false)
    private String country;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Business logic methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        address.append(streetAddress);
        
        if (streetAddress2 != null && !streetAddress2.trim().isEmpty()) {
            address.append(", ").append(streetAddress2);
        }
        
        address.append(", ").append(city)
               .append(", ").append(state)
               .append(" ").append(zipCode)
               .append(", ").append(country);
        
        return address.toString();
    }
    
    public String getShortAddress() {
        return city + ", " + state + " " + zipCode;
    }
    
    public boolean isShippingAddress() {
        return "shipping".equalsIgnoreCase(addressType);
    }
    
    public boolean isBillingAddress() {
        return "billing".equalsIgnoreCase(addressType);
    }
    
    public void setAsDefault() {
        this.isDefault = true;
    }
    
    public void setAsNonDefault() {
        this.isDefault = false;
    }
}
