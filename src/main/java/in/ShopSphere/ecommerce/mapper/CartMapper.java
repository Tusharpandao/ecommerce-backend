package in.ShopSphere.ecommerce.mapper;

import in.ShopSphere.ecommerce.dto.cart.CartResponse;
import in.ShopSphere.ecommerce.model.entity.Cart;
import in.ShopSphere.ecommerce.model.entity.ProductVariant;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "items")
    CartResponse toCartResponse(Cart cart);
    
    List<CartResponse> toCartResponseList(List<Cart> carts);
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productImage", expression = "java(cartItem.getProduct().getPrimaryImageUrl())")
    @Mapping(target = "variant", expression = "java(toProductVariantInfo(cartItem.getProduct().getVariants().stream().findFirst().orElse(null)))")
    CartResponse.CartItemResponse toCartItemResponse(in.ShopSphere.ecommerce.model.entity.CartItem cartItem);
    
    List<CartResponse.CartItemResponse> toCartItemResponseList(List<in.ShopSphere.ecommerce.model.entity.CartItem> cartItems);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "variantName", source = "variantName")
    @Mapping(target = "variantValue", source = "variantValue")
    @Mapping(target = "priceAdjustment", source = "priceAdjustment")
    CartResponse.CartItemResponse.ProductVariantInfo toProductVariantInfo(ProductVariant productVariant);
}
