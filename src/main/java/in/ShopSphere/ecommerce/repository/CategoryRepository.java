package in.ShopSphere.ecommerce.repository;

import in.ShopSphere.ecommerce.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByIsActiveTrue();
    
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();
    
    List<Category> findByParentIsNullAndIsActiveTrue();
    
    List<Category> findByParentIdAndIsActiveTrue(Long parentId);
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findRootCategories();
    
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findSubCategories(@Param("parentId") Long parentId);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND (c.name LIKE %:searchTerm% OR c.description LIKE %:searchTerm%)")
    Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.sortOrder ASC")
    Page<Category> findAllActive(Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
    long countSubCategories(@Param("parentId") Long parentId);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.id IN (SELECT DISTINCT p.category.id FROM Product p WHERE p.isActive = true)")
    List<Category> findCategoriesWithActiveProducts();
    
    boolean existsByNameAndParentId(String name, Long parentId);
    
    boolean existsByNameAndParentIdAndIdNot(String name, Long parentId, Long id);
}
