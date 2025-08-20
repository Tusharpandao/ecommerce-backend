package in.ShopSphere.ecommerce.service;

import in.ShopSphere.ecommerce.dto.product.ExternalProductDto;

import java.util.List;

public interface DataImportService {
    
    /**
     * Import products from external JSON file
     * @param filePath Path to the JSON file
     * @return Number of products imported
     */
    int importProductsFromJson(String filePath);
    
    /**
     * Import products from a list of external product DTOs
     * @param products List of external products
     * @return Number of products imported
     */
    int importProducts(List<ExternalProductDto> products);
    
    /**
     * Import a single external product
     * @param externalProduct External product data
     * @return Imported product ID
     */
    Long importSingleProduct(ExternalProductDto externalProduct);
}
