package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.cart.CartRequest;
import in.ShopSphere.ecommerce.dto.cart.CartResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;

public interface CartService {
    
    CartResponse getCart();
    
    CartResponse addToCart(CartRequest request);
    
    CartResponse updateCartItem(String itemId, Integer quantity);
    
    CartResponse removeFromCart(String itemId);
    
    CartResponse clearCart();
    
    ApiResponse applyCoupon(String couponCode);
    
    ApiResponse removeCoupon();
    
    CartResponse getCartSummary();
    
    ApiResponse moveToWishlist(String itemId);
    
    ApiResponse updateItemQuantity(String itemId, Integer quantity);
    
    ApiResponse checkCartAvailability();
}
