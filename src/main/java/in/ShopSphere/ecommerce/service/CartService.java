package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.cart.CartRequest;
import in.ShopSphere.ecommerce.dto.cart.CartResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;

public interface CartService {
    
    CartResponse getCart();
    
    CartResponse addToCart(CartRequest request);
    
    CartResponse updateCartItem(Long itemId, Integer quantity);
    
    CartResponse removeFromCart(Long itemId);
    
    CartResponse clearCart();
    
    ApiResponse applyCoupon(String couponCode);
    
    ApiResponse removeCoupon();
    
    CartResponse getCartSummary();
    
    ApiResponse moveToWishlist(Long itemId);
    
    ApiResponse updateItemQuantity(Long itemId, Integer quantity);
    
    ApiResponse checkCartAvailability();
}
