package in.ShopSphere.ecommerce.mapper;

import in.ShopSphere.ecommerce.dto.review.ReviewRequest;
import in.ShopSphere.ecommerce.dto.review.ReviewResponse;
import in.ShopSphere.ecommerce.model.entity.Review;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isApproved", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toReview(ReviewRequest request);
    
    @Mapping(target = "product", source = "product")
    @Mapping(target = "user", source = "user")
    ReviewResponse toReviewResponse(Review review);
    
    List<ReviewResponse> toReviewResponseList(List<Review> reviews);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isApproved", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateReviewFromRequest(ReviewRequest request, @MappingTarget Review review);
}
