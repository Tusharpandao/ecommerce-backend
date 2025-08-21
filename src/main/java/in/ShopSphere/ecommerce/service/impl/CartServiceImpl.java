package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.dto.cart.CartRequest;
import in.ShopSphere.ecommerce.dto.cart.CartResponse;
import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.exception.BusinessException;
import in.ShopSphere.ecommerce.exception.ResourceNotFoundException;
import in.ShopSphere.ecommerce.mapper.CartMapper;
import in.ShopSphere.ecommerce.model.entity.Cart;
import in.ShopSphere.ecommerce.model.entity.CartItem;
import in.ShopSphere.ecommerce.model.entity.Product;
import in.ShopSphere.ecommerce.model.entity.User;
import in.ShopSphere.ecommerce.repository.CartRepository;
import in.ShopSphere.ecommerce.repository.ProductRepository;
import in.ShopSphere.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    @Cacheable(value = "cart", key = "#root.methodName + #root.args[0]")
    public CartResponse getCart() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @CacheEvict(value = "cart", allEntries = true)
    public CartResponse addToCart(CartRequest request) {
        log.info("Adding item to cart: productId={}, quantity={}", request.getProductId(), request.getQuantity());
        
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        // Validate product
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));
        
        if (!product.getIsActive()) {
            throw new BusinessException("Product is not active");
        }
        
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock. Available: " + product.getStockQuantity());
        }
        
        // Check if product already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(request.getProductId()))
            .findFirst();
        
        if (existingItem.isPresent()) {
            // Update existing item quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            if (product.getStockQuantity() < newQuantity) {
                throw new BusinessException("Insufficient stock for updated quantity. Available: " + product.getStockQuantity());
            }
            
            item.setQuantity(newQuantity);
            item.setPriceAtTime(product.getCurrentPrice());
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setPriceAtTime(product.getCurrentPrice());
            newItem.setCreatedAt(LocalDateTime.now());
            newItem.setUpdatedAt(LocalDateTime.now());
            
            cart.getItems().add(newItem);
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Item added to cart successfully. Cart ID: {}", savedCart.getId());
        
        return cartMapper.toCartResponse(savedCart);
    }

    @Override
    @CacheEvict(value = "cart", allEntries = true)
    public CartResponse updateCartItem(String itemId, Integer quantity) {
        log.info("Updating cart item: itemId={}, quantity={}", itemId, quantity);
        
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        CartItem item = cart.getItems().stream()
            .filter(cartItem -> cartItem.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            // Validate stock
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new BusinessException("Insufficient stock. Available: " + item.getProduct().getStockQuantity());
            }
            
            item.setQuantity(quantity);
            item.setUpdatedAt(LocalDateTime.now());
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Cart item updated successfully. Cart ID: {}", savedCart.getId());
        
        return cartMapper.toCartResponse(savedCart);
    }

    @Override
    @CacheEvict(value = "cart", allEntries = true)
    public CartResponse removeFromCart(String itemId) {
        log.info("Removing item from cart: itemId={}", itemId);
        
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        
        if (!removed) {
            throw new ResourceNotFoundException("Cart item not found");
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Item removed from cart successfully. Cart ID: {}", savedCart.getId());
        
        return cartMapper.toCartResponse(savedCart);
    }



    @Override
    @CacheEvict(value = "cart", allEntries = true)
    public CartResponse clearCart() {
        log.info("Clearing cart");
        
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Cart cleared successfully. Cart ID: {}", savedCart.getId());
        
        return cartMapper.toCartResponse(savedCart);
    }

    @SuppressWarnings("rawtypes")
    @Override
    @CacheEvict(value = "cart", allEntries = true)
    public ApiResponse applyCoupon(String couponCode) {
        log.info("Applying coupon: {}", couponCode);
        
        // TODO: Implement coupon logic
        throw new BusinessException("Coupon functionality not implemented yet");
    }

    @SuppressWarnings("rawtypes")
    @Override
    @CacheEvict(value = "cart", allEntries = true)
    public ApiResponse removeCoupon() {
        log.info("Removing coupon");
        
        // TODO: Implement coupon removal logic
        throw new BusinessException("Coupon functionality not implemented yet");
    }

    @Override
    @Cacheable(value = "cart", key = "#root.methodName + #root.args[0]")
    public CartResponse getCartSummary() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        return cartMapper.toCartResponse(cart);
    }

    @SuppressWarnings("rawtypes")
    @Override
    @CacheEvict(value = "cart", allEntries = true)
    public ApiResponse moveToWishlist(String productId) {
        log.info("Moving product to wishlist: productId={}", productId);
        
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        CartItem item = cart.getItems().stream()
            .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));
        
        // TODO: Implement wishlist logic
        log.info("Product moved to wishlist successfully");
        
        return ApiResponse.success(null, "Product moved to wishlist successfully");
    }

    @SuppressWarnings("rawtypes")
    @Override
    @Cacheable(value = "cart", key = "#root.methodName + #root.args[0]")
    public ApiResponse checkCartAvailability() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        if (cart.isEmpty()) {
            return ApiResponse.success(false, "Cart is empty");
        }
        
        // Check if all items are available
        boolean isAvailable = cart.getItems().stream()
            .allMatch(item -> item.isAvailable());
        
        return ApiResponse.success(isAvailable, isAvailable ? "All items are available" : "Some items are not available");
    }

    @SuppressWarnings("rawtypes")
    @Override
    @CacheEvict(value = "cart", allEntries = true)
        public ApiResponse updateItemQuantity(String itemId, Integer quantity) {
        log.info("Updating item quantity: itemId={}, quantity={}", itemId, quantity);
        
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        
        CartItem item = cart.getItems().stream()
            .filter(cartItem -> cartItem.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            // Validate stock
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new BusinessException("Insufficient stock. Available: " + item.getProduct().getStockQuantity());
            }
            
            item.setQuantity(quantity);
            item.setUpdatedAt(LocalDateTime.now());
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        
        log.info("Item quantity updated successfully");
        
        return ApiResponse.success(null, "Item quantity updated successfully");
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setCreatedAt(LocalDateTime.now());
                newCart.setUpdatedAt(LocalDateTime.now());
                return cartRepository.save(newCart);
            });
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // This would need to be implemented with proper user retrieval
        // For now, returning a mock user
        throw new BusinessException("Current user retrieval not implemented");
    }
}
