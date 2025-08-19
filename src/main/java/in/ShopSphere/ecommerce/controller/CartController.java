package in.ShopSphere.ecommerce.controller;

import in.ShopSphere.ecommerce.dto.cart.CartRequest;
import in.ShopSphere.ecommerce.dto.cart.CartResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieves the current user's shopping cart.")
    public ResponseEntity<CartResponse> getCart() {
        log.info("Getting cart for current user");
        CartResponse response = cartService.getCart();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart", description = "Adds a product to the shopping cart.")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody CartRequest request) {
        log.info("Adding item to cart: productId={}, quantity={}", request.getProductId(), request.getQuantity());
        CartResponse response = cartService.addToCart(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Updates the quantity of an item in the cart.")
    public ResponseEntity<CartResponse> updateCartItem(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId,
            @Parameter(description = "New quantity") @RequestParam Integer quantity) {
        log.info("Updating cart item: itemId={}, quantity={}", itemId, quantity);
        CartResponse response = cartService.updateCartItem(itemId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the shopping cart.")
    public ResponseEntity<CartResponse> removeFromCart(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId) {
        log.info("Removing item from cart: itemId={}", itemId);
        CartResponse response = cartService.removeFromCart(itemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart", description = "Removes all items from the shopping cart.")
    public ResponseEntity<CartResponse> clearCart() {
        log.info("Clearing cart for current user");
        CartResponse response = cartService.clearCart();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/coupon/apply")
    @Operation(summary = "Apply coupon", description = "Applies a coupon code to the cart.")
    public ResponseEntity<ApiResponse> applyCoupon(
            @Parameter(description = "Coupon code") @RequestParam String couponCode) {
        log.info("Applying coupon: {}", couponCode);
        ApiResponse response = cartService.applyCoupon(couponCode);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/coupon/remove")
    @Operation(summary = "Remove coupon", description = "Removes the applied coupon from the cart.")
    public ResponseEntity<ApiResponse> removeCoupon() {
        log.info("Removing coupon from cart");
        ApiResponse response = cartService.removeCoupon();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get cart summary", description = "Retrieves a summary of the shopping cart.")
    public ResponseEntity<CartResponse> getCartSummary() {
        log.info("Getting cart summary for current user");
        CartResponse response = cartService.getCartSummary();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{itemId}/move-to-wishlist")
    @Operation(summary = "Move item to wishlist", description = "Moves an item from cart to wishlist.")
    public ResponseEntity<ApiResponse> moveToWishlist(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId) {
        log.info("Moving item to wishlist: itemId={}", itemId);
        ApiResponse response = cartService.moveToWishlist(itemId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/availability")
    @Operation(summary = "Check cart availability", description = "Checks if all items in the cart are available.")
    public ResponseEntity<ApiResponse> checkCartAvailability() {
        log.info("Checking cart availability for current user");
        ApiResponse response = cartService.checkCartAvailability();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/items/{itemId}/quantity")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of a specific item in the cart.")
    public ResponseEntity<ApiResponse> updateItemQuantity(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId,
            @Parameter(description = "New quantity") @RequestParam Integer quantity) {
        log.info("Updating item quantity: itemId={}, quantity={}", itemId, quantity);
        ApiResponse response = cartService.updateItemQuantity(itemId, quantity);
        return ResponseEntity.ok(response);
    }
}
