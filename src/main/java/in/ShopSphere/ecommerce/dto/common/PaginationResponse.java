package in.ShopSphere.ecommerce.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
    
    private List<T> data;
    private PaginationInfo pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int page;
        private int limit;
        private long total;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        private int nextPage;
        private int previousPage;
        
        public static PaginationInfo of(int page, int limit, long total) {
            int totalPages = (int) Math.ceil((double) total / limit);
            return PaginationInfo.builder()
                    .page(page)
                    .limit(limit)
                    .total(total)
                    .totalPages(totalPages)
                    .hasNext(page < totalPages)
                    .hasPrevious(page > 1)
                    .nextPage(page < totalPages ? page + 1 : page)
                    .previousPage(page > 1 ? page - 1 : 1)
                    .build();
        }
        
        public void setSize(int size) {
            this.limit = size;
        }
    }
    
    public static <T> PaginationResponse<T> of(List<T> data, int page, int limit, long total) {
        return PaginationResponse.<T>builder()
                .data(data)
                .pagination(PaginationInfo.of(page, limit, total))
                .build();
    }
    
    public static <T> PaginationResponse<T> of(List<T> data, PaginationInfo pagination) {
        return PaginationResponse.<T>builder()
                .data(data)
                .pagination(pagination)
                .build();
    }
}
