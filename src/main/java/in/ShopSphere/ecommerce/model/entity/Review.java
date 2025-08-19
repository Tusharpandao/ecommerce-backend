package in.ShopSphere.ecommerce.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_reviews_product_id", columnList = "product_id"),
    @Index(name = "idx_reviews_user_id", columnList = "user_id"),
    @Index(name = "idx_reviews_is_approved", columnList = "is_approved")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column
    private String title;
    
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "is_approved")
    private Boolean isApproved = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Business logic methods
    public String getRatingStars() {
        if (rating == null) return "";
        
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    public boolean isPositive() {
        return rating != null && rating >= 4;
    }
    
    public boolean isNegative() {
        return rating != null && rating <= 2;
    }
    
    public boolean isNeutral() {
        return rating != null && rating == 3;
    }
    
    public String getRatingDescription() {
        if (rating == null) return "No rating";
        
        return switch (rating) {
            case 1 -> "Poor";
            case 2 -> "Fair";
            case 3 -> "Good";
            case 4 -> "Very Good";
            case 5 -> "Excellent";
            default -> "Unknown";
        };
    }
    
    public void approve() {
        this.isApproved = true;
    }
    
    public void reject() {
        this.isApproved = false;
    }
    
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
    
    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }
}
