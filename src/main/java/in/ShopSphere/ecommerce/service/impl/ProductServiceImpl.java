package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.dto.common.PaginationResponse;
import in.ShopSphere.ecommerce.dto.product.ProductRequest;
import in.ShopSphere.ecommerce.dto.product.ProductResponse;
import in.ShopSphere.ecommerce.exception.BusinessException;
import in.ShopSphere.ecommerce.exception.ResourceNotFoundException;
import in.ShopSphere.ecommerce.mapper.ProductMapper;
import in.ShopSphere.ecommerce.model.entity.Category;
import in.ShopSphere.ecommerce.model.entity.Product;
import in.ShopSphere.ecommerce.model.entity.User;
import in.ShopSphere.ecommerce.repository.CategoryRepository;
import in.ShopSphere.ecommerce.repository.ProductRepository;
import in.ShopSphere.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import in.ShopSphere.ecommerce.dto.common.SearchFilters;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getName());
        
        // Check if SKU already exists
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("Product with SKU " + request.getSku() + " already exists");
        }
        
        // Check if barcode already exists (if provided)
        if (request.getBarcode() != null && productRepository.existsByBarcode(request.getBarcode())) {
            throw new BusinessException("Product with barcode " + request.getBarcode() + " already exists");
        }
        
        // Get current user (seller)
        User currentUser = getCurrentUser();
        if (currentUser.getRole().name().equals("CUSTOMER")) {
            throw new BusinessException("Customers cannot create products");
        }
        
        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        
        // Create product
        Product product = productMapper.toProduct(request);
        product.setSeller(currentUser);
        product.setCategory(category);
        product.setRating(BigDecimal.ZERO);
        product.setReviewCount(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ProductResponse updateProduct(String id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Check if current user can update this product
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !product.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only update your own products");
        }
        
        // Check if SKU already exists (if changed)
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("Product with SKU " + request.getSku() + " already exists");
        }
        
        // Check if barcode already exists (if changed)
        if (request.getBarcode() != null && !request.getBarcode().equals(product.getBarcode()) && 
            productRepository.existsByBarcode(request.getBarcode())) {
            throw new BusinessException("Product with barcode " + request.getBarcode() + " already exists");
        }
        
        // Update product
        productMapper.updateProductFromRequest(request, product);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());
        
        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        if (!product.getIsActive()) {
            throw new BusinessException("Product is not active");
        }
        
        return productMapper.toProductResponse(product);
    }

    @Override
    @Cacheable(value = "product", key = "#sku")
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        
        if (!product.getIsActive()) {
            throw new BusinessException("Product is not active");
        }
        
        return productMapper.toProductResponse(product);
    }

    @Override
    // @Cacheable(value = "products") // Temporarily disabled due to Redis deserialization issue
    public PaginationResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        List<ProductResponse> productResponses = productMapper.toProductResponseList(products.getContent());
        
        return PaginationResponse.<ProductResponse>builder()
            .data(productResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(products.getNumber())
                .limit(products.getSize())
                .total(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build())
            .build();
    }

    @Override
    // @Cacheable(value = "products", key = "#searchTerm + #pageable.pageNumber + #pageable.pageSize") // Temporarily disabled
    public PaginationResponse<ProductResponse> searchProducts(String searchTerm, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);
        List<ProductResponse> productResponses = productMapper.toProductResponseList(products.getContent());
        
        return PaginationResponse.<ProductResponse>builder()
            .data(productResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(products.getNumber())
                .limit(products.getSize())
                .total(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build())
            .build();
    }

    @Override
    public PaginationResponse<ProductResponse> searchProductsWithFilters(SearchFilters filters, Pageable pageable) {
        log.info("Searching products with filters: {}", filters);
        
        // Start with all active products
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        List<Product> filteredProducts = products.getContent();
        
        // Apply category filter
        if (filters.getCategory() != null && !filters.getCategory().isEmpty()) {
            // Find category by name
            Category category = categoryRepository.findByName(filters.getCategory())
                .orElse(null);
            
            if (category != null) {
                filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getCategory().getName().equals(filters.getCategory()))
                    .toList();
            }
        }
        
        // Apply price filters
        if (filters.getMinPrice() != null) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> p.getCurrentPrice().doubleValue() >= filters.getMinPrice())
                .toList();
        }
        
        if (filters.getMaxPrice() != null) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> p.getCurrentPrice().doubleValue() <= filters.getMaxPrice())
                .toList();
        }
        
        // Apply rating filter
        if (filters.getRating() != null) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> p.getRating().doubleValue() >= filters.getRating())
                .toList();
        }
        
        // Apply stock filter
        if (filters.getInStock() != null && filters.getInStock()) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> p.getStockQuantity() > 0)
                .toList();
        }
        
        // Apply search term filter
        if (filters.getSearchTerm() != null && !filters.getSearchTerm().isEmpty()) {
            String searchTerm = filters.getSearchTerm().toLowerCase();
            filteredProducts = filteredProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchTerm) || 
                           p.getDescription().toLowerCase().contains(searchTerm))
                .toList();
        }
        
        // Convert to responses
        List<ProductResponse> productResponses = productMapper.toProductResponseList(filteredProducts);
        
        // Create pagination info (simplified for filtered results)
        int totalElements = filteredProducts.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        
        return PaginationResponse.<ProductResponse>builder()
            .data(productResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(pageable.getPageNumber())
                .limit(pageable.getPageSize())
                .total(totalElements)
                .totalPages(totalPages)
                .build())
            .build();
    }

    @Override
    // @Cacheable(value = "products", key = "#categoryId + #pageable.pageNumber + #pageable.pageSize") // Temporarily disabled
    public PaginationResponse<ProductResponse> getProductsByCategory(String categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        List<ProductResponse> productResponses = productMapper.toProductResponseList(products.getContent());
        
        return PaginationResponse.<ProductResponse>builder()
            .data(productResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(products.getNumber())
                .limit(products.getSize())
                .total(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build())
            .build();
    }

    @Override
    // @Cacheable(value = "products", key = "#sellerId + #pageable.pageNumber + #pageable.pageSize") // Temporarily disabled
    public PaginationResponse<ProductResponse> getProductsBySeller(String sellerId, Pageable pageable) {
        // This would need a custom query or we'd need to get the User first
        // For now, implementing a basic version
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        List<ProductResponse> productResponses = products.getContent().stream()
            .filter(p -> p.getSeller().getId().equals(sellerId))
            .map(productMapper::toProductResponse)
            .toList();
        
        return PaginationResponse.<ProductResponse>builder()
            .data(productResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(products.getNumber())
                .limit(products.getSize())
                .total(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build())
            .build();
    }

    @Override
    @Cacheable(value = "products", key = "#limit")
    public List<ProductResponse> getFeaturedProducts() {
        List<Product> products = productRepository.findByIsFeaturedTrueAndIsActiveTrue();
        return productMapper.toProductResponseList(products);
    }

    @Override
    @Cacheable(value = "products", key = "#limit + #pageable.pageNumber + #pageable.pageSize")
    public List<ProductResponse> getLatestProducts(int limit) {
        // This would need a custom query with limit
        // For now, implementing with pagination
        Pageable pageable = Pageable.ofSize(limit);
        Page<Product> products = productRepository.findLatestProducts(pageable);
        return productMapper.toProductResponseList(products.getContent());
    }

    @Override
    @Cacheable(value = "products", key = "#limit + #pageable.pageNumber + #pageable.pageSize")
    public List<ProductResponse> getTopRatedProducts(int limit) {
        // This would need a custom query with limit
        // For now, implementing with pagination
        Pageable pageable = Pageable.ofSize(limit);
        Page<Product> products = productRepository.findTopRatedProducts(pageable);
        return productMapper.toProductResponseList(products.getContent());
    }

    @Override
    @Cacheable(value = "products", key = "#pageable.pageNumber + #pageable.pageSize")
    public List<ProductResponse> getDiscountedProducts() {
        Pageable pageable = Pageable.ofSize(100); // Large page size for all discounted products
        Page<Product> products = productRepository.findDiscountedProducts(pageable);
        return productMapper.toProductResponseList(products.getContent());
    }

    @Override
    @Cacheable(value = "products")
    public List<ProductResponse> getLowStockProducts() {
        List<Product> products = productRepository.findLowStockProducts();
        return productMapper.toProductResponseList(products);
    }

    @Override
    @Cacheable(value = "products")
    public List<ProductResponse> getOutOfStockProducts() {
        List<Product> products = productRepository.findOutOfStockProducts();
        return productMapper.toProductResponseList(products);
    }

    @Override
    @Cacheable(value = "products", key = "#minPrice + #maxPrice + #pageable.pageNumber + #pageable.pageSize")
    public PaginationResponse<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        List<ProductResponse> productResponses = productMapper.toProductResponseList(products.getContent());
        
        return PaginationResponse.<ProductResponse>builder()
            .data(productResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(products.getNumber())
                .limit(products.getSize())
                .total(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build())
            .build();
    }

    @Override
    @Cacheable(value = "products", key = "#minRating + #pageable.pageNumber + #pageable.pageSize")
    public PaginationResponse<ProductResponse> getProductsByRating(Double minRating, Pageable pageable) {
        Page<Product> products = productRepository.findByMinRating(minRating, pageable);
        List<ProductResponse> productResponses = productMapper.toProductResponseList(products.getContent());
        
        return PaginationResponse.<ProductResponse>builder()
            .data(productResponses)
            .pagination(PaginationResponse.PaginationInfo.builder()
                .page(products.getNumber())
                .limit(products.getSize())
                .total(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build())
            .build();
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ApiResponse deleteProduct(String id) {
        log.info("Deleting product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Check if current user can delete this product
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !product.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only delete your own products");
        }
        
        productRepository.delete(product);
        log.info("Product deleted successfully with ID: {}", id);
        
        return ApiResponse.success(null, "Product deleted successfully");
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ApiResponse toggleProductStatus(String id) {
        log.info("Toggling product status with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Check if current user can toggle this product
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !product.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only toggle your own products");
        }
        
        product.setIsActive(!product.getIsActive());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        
        String status = product.getIsActive() ? "activated" : "deactivated";
        log.info("Product {} successfully with ID: {}", status, id);
        
        return ApiResponse.success(null, "Product " + status + " successfully");
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ApiResponse toggleFeaturedStatus(String id) {
        log.info("Toggling featured status for product with ID: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Only admins can toggle featured status
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("Only admins can toggle featured status");
        }
        
        product.setIsFeatured(!product.getIsFeatured());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        
        String status = product.getIsFeatured() ? "featured" : "unfeatured";
        log.info("Product {} successfully with ID: {}", status, id);
        
        return ApiResponse.success(null, "Product " + status + " successfully");
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ApiResponse updateStock(String id, Integer quantity) {
        log.info("Updating stock for product with ID: {} to quantity: {}", id, quantity);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Check if current user can update this product
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !product.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only update your own products");
        }
        
        if (quantity < 0) {
            throw new BusinessException("Stock quantity cannot be negative");
        }
        
        product.setStockQuantity(quantity);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        
        log.info("Stock updated successfully for product with ID: {}", id);
        
        return ApiResponse.success(null, "Stock updated successfully");
    }

    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public ApiResponse updatePrice(String id, BigDecimal price, BigDecimal salePrice) {
        log.info("Updating price for product with ID: {} to price: {}, salePrice: {}", id, price, salePrice);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        
        // Check if current user can update this product
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().name().equals("ADMIN") && !product.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only update your own products");
        }
        
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be greater than 0");
        }
        
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Sale price must be greater than 0");
        }
        
        if (salePrice != null && salePrice.compareTo(price) >= 0) {
            throw new BusinessException("Sale price must be less than regular price");
        }
        
        product.setPrice(price);
        product.setSalePrice(salePrice);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        
        log.info("Price updated successfully for product with ID: {}", id);
        
        return ApiResponse.success(null, "Product price updated successfully");
    }
    
    @Override
    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void clearProductsCache() {
        log.info("Products cache cleared");
        // The @CacheEvict annotation will handle clearing the cache
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // This would need to be implemented with proper user retrieval
        // For now, returning a mock user
        throw new BusinessException("Current user retrieval not implemented");
    }
}
