package in.ShopSphere.ecommerce.mapper;

import in.ShopSphere.ecommerce.dto.auth.AuthResponse;
import in.ShopSphere.ecommerce.dto.user.UserResponse;
import in.ShopSphere.ecommerce.dto.user.UserUpdateRequest;
import in.ShopSphere.ecommerce.model.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "orderCount", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "wishlistCount", ignore = true)
    UserResponse toUserResponse(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isBlocked", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetExpires", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "wishlist", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isBlocked", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetExpires", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "wishlist", ignore = true)
    User toUser(UserUpdateRequest request);
    
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "createdAt", expression = "java(user.getCreatedAt().toString())")
    @Mapping(target = "updatedAt", expression = "java(user.getUpdatedAt().toString())")
    AuthResponse.UserDto toAuthUserDto(User user);
    
    List<UserResponse> toUserResponseList(List<User> users);
}
