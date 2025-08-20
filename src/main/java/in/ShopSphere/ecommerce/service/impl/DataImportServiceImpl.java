package in.ShopSphere.ecommerce.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.ShopSphere.ecommerce.dto.product.ExternalProductDto;
import in.ShopSphere.ecommerce.model.entity.*;
import in.ShopSphere.ecommerce.model.entity.UserRole;
import in.ShopSphere.ecommerce.repository.CategoryRepository;
import in.ShopSphere.ecommerce.repository.ProductRepository;
import in.ShopSphere.ecommerce.repository.ReviewRepository;
import in.ShopSphere.ecommerce.repository.UserRepository;
import in.ShopSphere.ecommerce.service.DataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataImportServiceImpl implements DataImportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public int importProductsFromJson(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("File not found: {}", filePath);
                return 0;
            }

            List<ExternalProductDto> products = objectMapper.readValue(file, 
                new TypeReference<List<ExternalProductDto>>() {});
            
            log.info("Found {} products to import from file: {}", products.size(), filePath);
            return importProducts(products);
            
        } catch (IOException e) {
            log.error("Error reading JSON file: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Transactional
    public int importProducts(List<ExternalProductDto> externalProducts) {
        int importedCount = 0;
        
        // Ensure default seller exists
        User defaultSeller = getOrCreateDefaultSeller();
        
        // Process categories first
        Map<String, Category> categoryMap = processCategories(externalProducts);
        
        for (ExternalProductDto externalProduct : externalProducts) {
            try {
                Long productId = importSingleProduct(externalProduct);
                if (productId != null) {
                    importedCount++;
                    log.info("Successfully imported product: {} (ID: {})", 
                        externalProduct.getTitle(), productId);
                }
            } catch (Exception e) {
                log.error("Error importing product {}: {}", externalProduct.getTitle(), e.getMessage(), e);
            }
        }
        
        log.info("Import completed. Successfully imported {} out of {} products", 
            importedCount, externalProducts.size());
        return importedCount;
    }

    @Override
    @Transactional
    public Long importSingleProduct(ExternalProductDto externalProduct) {
        // Get or create category
        Category category = getOrCreateCategory(externalProduct.getCategory());
        
        // Get default seller
        User seller = getOrCreateDefaultSeller();
        
        // Calculate sale price from discount percentage
        BigDecimal salePrice = null;
        if (externalProduct.getDiscountPercentage() != null && 
            externalProduct.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            salePrice = externalProduct.getPrice()
                .multiply(BigDecimal.ONE.subtract(externalProduct.getDiscountPercentage()
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)));
        }
        
        // Create product
        Product product = Product.builder()
            .name(externalProduct.getTitle())
            .description(externalProduct.getDescription())
            .price(externalProduct.getPrice())
            .salePrice(salePrice)
            .sku(externalProduct.getSku())
            .barcode(externalProduct.getMeta() != null ? externalProduct.getMeta().getBarcode() : null)
            .weight(externalProduct.getWeight())
            .dimensions(formatDimensions(externalProduct.getDimensions()))
            .brand(externalProduct.getBrand())
            .tags(externalProduct.getTags())
            .discountPercentage(externalProduct.getDiscountPercentage())
            .warrantyInformation(externalProduct.getWarrantyInformation())
            .shippingInformation(externalProduct.getShippingInformation())
            .returnPolicy(externalProduct.getReturnPolicy())
            .minimumOrderQuantity(externalProduct.getMinimumOrderQuantity())
            .availabilityStatus(externalProduct.getAvailabilityStatus())
            .thumbnail(externalProduct.getThumbnail())
            .stockQuantity(externalProduct.getStock())
            .rating(externalProduct.getRating())
            .reviewCount(externalProduct.getReviews() != null ? externalProduct.getReviews().size() : 0)
            .isActive(true)
            .isFeatured(false)
            .category(category)
            .seller(seller)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // Save product first
        log.info("Saving product: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        log.info("Product saved with ID: {}", savedProduct.getId());
        
        // Process images
        processProductImages(savedProduct, externalProduct.getImages(), externalProduct.getThumbnail());
        log.info("Product images processed");
        
        // Process reviews
        if (externalProduct.getReviews() != null && !externalProduct.getReviews().isEmpty()) {
            processProductReviews(savedProduct, externalProduct.getReviews());
            log.info("Product reviews processed");
        }
        
        // Save the product again to persist the relationships
        log.info("Saving product with relationships");
        savedProduct = productRepository.save(savedProduct);
        log.info("Product with relationships saved successfully");
        
        return savedProduct.getId();
    }

    private User getOrCreateDefaultSeller() {
        return userRepository.findByEmail("system@shopsphere.com")
            .orElseGet(() -> {
                User defaultSeller = User.builder()
                    .email("system@shopsphere.com")
                    .password("$2a$10$dummy.hash.for.system.user") // You should set a proper password
                    .firstName("System")
                    .lastName("Seller")
                    .role(UserRole.SELLER)
                    .isBlocked(false)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                return userRepository.save(defaultSeller);
            });
    }

    private Map<String, Category> processCategories(List<ExternalProductDto> products) {
        Map<String, Category> categoryMap = new HashMap<>();
        
        // Get unique categories
        Set<String> categoryNames = products.stream()
            .map(ExternalProductDto::getCategory)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        for (String categoryName : categoryNames) {
            Category category = getOrCreateCategory(categoryName);
            categoryMap.put(categoryName, category);
        }
        
        return categoryMap;
    }

    private Category getOrCreateCategory(String categoryName) {
        // First try exact match
        Optional<Category> existingCategory = categoryRepository.findByName(categoryName);
        
        // If not found, try case-insensitive search
        if (existingCategory.isEmpty()) {
            List<Category> allCategories = categoryRepository.findAll();
            existingCategory = allCategories.stream()
                .filter(cat -> cat.getName().equalsIgnoreCase(categoryName))
                .findFirst();
        }
        
        return existingCategory.orElseGet(() -> {
            Category newCategory = Category.builder()
                .name(categoryName)
                .description(categoryName)
                .isActive(true)
                .sortOrder(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            return categoryRepository.save(newCategory);
        });
    }

    private String formatDimensions(ExternalProductDto.DimensionsDto dimensions) {
        if (dimensions == null) return null;
        
        return String.format("{\"width\":%s,\"height\":%s,\"depth\":%s}",
            dimensions.getWidth(), dimensions.getHeight(), dimensions.getDepth());
    }
    


    private void processProductImages(Product product, List<String> imageUrls, String thumbnailUrl) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        
        List<ProductImage> productImages = new ArrayList<>();
        
        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            boolean isPrimary = i == 0; // First image is primary
            
            ProductImage productImage = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .altText(product.getName() + " - Image " + (i + 1))
                .isPrimary(isPrimary)
                .sortOrder(i)
                .createdAt(LocalDateTime.now())
                .build();
            
            productImages.add(productImage);
        }
        
        // Add thumbnail if different from first image
        if (thumbnailUrl != null && !thumbnailUrl.equals(imageUrls.get(0))) {
            ProductImage thumbnailImage = ProductImage.builder()
                .product(product)
                .imageUrl(thumbnailUrl)
                .altText(product.getName() + " - Thumbnail")
                .isPrimary(false)
                .sortOrder(imageUrls.size())
                .createdAt(LocalDateTime.now())
                .build();
            
            productImages.add(thumbnailImage);
        }
        
        product.setImages(productImages);
    }

    private void processProductReviews(Product product, List<ExternalProductDto.ReviewDto> externalReviews) {
        List<Review> reviews = new ArrayList<>();
        
        for (ExternalProductDto.ReviewDto externalReview : externalReviews) {
            // Create or get user for reviewer
            User reviewer = getOrCreateReviewerUser(externalReview.getReviewerEmail(), 
                externalReview.getReviewerName());
            
            Review review = Review.builder()
                .product(product)
                .user(reviewer)
                .rating(externalReview.getRating())
                .title("Review by " + externalReview.getReviewerName())
                .comment(externalReview.getComment())
                .isApproved(true) // Auto-approve imported reviews
                .createdAt(externalReview.getDate() != null ? externalReview.getDate() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            reviews.add(review);
        }
        
        product.setReviews(reviews);
    }

    private User getOrCreateReviewerUser(String email, String name) {
        return userRepository.findByEmail(email)
            .orElseGet(() -> {
                // Create a placeholder user for the reviewer
                String[] nameParts = name.split(" ", 2);
                String firstName = nameParts.length > 0 ? nameParts[0] : "Unknown";
                String lastName = nameParts.length > 1 ? nameParts[1] : "User";
                
                User reviewer = User.builder()
                    .email(email)
                    .password("$2a$10$dummy.hash.for.reviewer.user") // Placeholder password
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(UserRole.CUSTOMER)
                    .isBlocked(false)
                    .emailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                return userRepository.save(reviewer);
            });
    }
}
