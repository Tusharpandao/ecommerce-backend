package in.ShopSphere.ecommerce.controller;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.product.ProductRequest;
import in.ShopSphere.ecommerce.dto.product.ProductResponse;
import in.ShopSphere.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Create product", description = "Create a new product (Seller/Admin only)")
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse product = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Product created successfully"));
        } catch (Exception e) {
            log.error("Product creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Product creation failed", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Update product", description = "Update an existing product (Seller/Admin only)")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse product = productService.updateProduct(id, request);
            return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
        } catch (Exception e) {
            log.error("Product update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Product update failed", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get product details by ID")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable String id) {
        try {
            ProductResponse product = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
        } catch (Exception e) {
            log.error("Product retrieval failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Get product details by SKU")
    public ResponseEntity<ApiResponse> getProductBySku(@PathVariable String sku) {
        try {
            ProductResponse product = productService.getProductBySku(sku);
            return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
        } catch (Exception e) {
            log.error("Product retrieval failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all products", description = "Get paginated list of all products")
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            PaginationResponse<ProductResponse> products = productService.getAllProducts(pageable);
            return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
        } catch (Exception e) {
            log.error("Products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Products retrieval failed", e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by term")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            PaginationResponse<ProductResponse> products = productService.searchProducts(q, pageable);
            return ResponseEntity.ok(ApiResponse.success(products, "Products search completed"));
        } catch (Exception e) {
            log.error("Product search failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Product search failed", e.getMessage()));
        }
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Get products in a specific category")
    public ResponseEntity<ApiResponse> getProductsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            PaginationResponse<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
            return ResponseEntity.ok(ApiResponse.success(products, "Category products retrieved"));
        } catch (Exception e) {
            log.error("Category products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Category products retrieval failed", e.getMessage()));
        }
    }
    
    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get products by seller", description = "Get products from a specific seller")
    public ResponseEntity<ApiResponse> getProductsBySeller(
            @PathVariable String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            PaginationResponse<ProductResponse> products = productService.getProductsBySeller(sellerId, pageable);
            return ResponseEntity.ok(ApiResponse.success(products, "Seller products retrieved"));
        } catch (Exception e) {
            log.error("Seller products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Seller products retrieval failed", e.getMessage()));
        }
    }
    
    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Get list of featured products")
    public ResponseEntity<ApiResponse> getFeaturedProducts() {
        try {
            List<ProductResponse> products = productService.getFeaturedProducts();
            return ResponseEntity.ok(ApiResponse.success(products, "Featured products retrieved"));
        } catch (Exception e) {
            log.error("Featured products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Featured products retrieval failed", e.getMessage()));
        }
    }
    
    @GetMapping("/latest")
    @Operation(summary = "Get latest products", description = "Get list of latest products")
    public ResponseEntity<ApiResponse> getLatestProducts(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProductResponse> products = productService.getLatestProducts(limit);
            return ResponseEntity.ok(ApiResponse.success(products, "Latest products retrieved"));
        } catch (Exception e) {
            log.error("Latest products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Latest products retrieval failed", e.getMessage()));
        }
    }
    
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated products", description = "Get list of top rated products")
    public ResponseEntity<ApiResponse> getTopRatedProducts(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProductResponse> products = productService.getTopRatedProducts(limit);
            return ResponseEntity.ok(ApiResponse.success(products, "Top rated products retrieved"));
        } catch (Exception e) {
            log.error("Top rated products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Top rated products retrieval failed", e.getMessage()));
        }
    }
    
    @GetMapping("/discounted")
    @Operation(summary = "Get discounted products", description = "Get list of discounted products")
    public ResponseEntity<ApiResponse> getDiscountedProducts() {
        try {
            List<ProductResponse> products = productService.getDiscountedProducts();
            return ResponseEntity.ok(ApiResponse.success(products, "Discounted products retrieved"));
        } catch (Exception e) {
            log.error("Discounted products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Discounted products retrieval failed", e.getMessage()));
        }
    }
    
    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range", description = "Get products within price range")
    public ResponseEntity<ApiResponse> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            PaginationResponse<ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
            return ResponseEntity.ok(ApiResponse.success(products, "Price range products retrieved"));
        } catch (Exception e) {
            log.error("Price range products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Price range products retrieval failed", e.getMessage()));
        }
    }
    
    @GetMapping("/rating/{minRating}")
    @Operation(summary = "Get products by minimum rating", description = "Get products with minimum rating")
    public ResponseEntity<ApiResponse> getProductsByRating(
            @PathVariable Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            PaginationResponse<ProductResponse> products = productService.getProductsByRating(minRating, pageable);
            return ResponseEntity.ok(ApiResponse.success(products, "Rating filtered products retrieved"));
        } catch (Exception e) {
            log.error("Rating filtered products retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Rating filtered products retrieval failed", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Delete product", description = "Delete a product (Seller/Admin only)")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable String id) {
        try {
            ApiResponse result = productService.deleteProduct(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Product deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Product deletion failed", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Toggle product status", description = "Toggle product active status (Seller/Admin only)")
    public ResponseEntity<ApiResponse> toggleProductStatus(@PathVariable String id) {
        try {
            ApiResponse result = productService.toggleProductStatus(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Product status toggle failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Product status toggle failed", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle featured status", description = "Toggle product featured status (Admin only)")
    public ResponseEntity<ApiResponse> toggleFeaturedStatus(@PathVariable String id) {
        try {
            ApiResponse result = productService.toggleFeaturedStatus(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Featured status toggle failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Featured status toggle failed", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Update stock", description = "Update product stock quantity (Seller/Admin only)")
    public ResponseEntity<ApiResponse> updateStock(@PathVariable String id, @RequestParam Integer quantity) {
        try {
            ApiResponse result = productService.updateStock(id, quantity);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Stock update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Stock update failed", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Update price", description = "Update product price (Seller/Admin only)")
    public ResponseEntity<ApiResponse> updatePrice(
            @PathVariable String id,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) BigDecimal salePrice) {
        try {
            ApiResponse result = productService.updatePrice(id, price, salePrice);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Price update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Price update failed", e.getMessage()));
        }
    }
}
