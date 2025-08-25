package in.ShopSphere.ecommerce.dto.common;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilters {
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private Double rating;
    private Boolean inStock;
    private String searchTerm;
}
