package za.ac.cput.digitalpharmacysystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.ac.cput.digitalpharmacysystem.domain.Product;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByCategory_CategoryId(String categoryId);

    List<Product> findByRxRequired(boolean rxRequired);

    List<Product> findByStockQuantityLessThanEqualOrderByStockQuantityAsc(int threshold);


    @Query("SELECT p FROM Product p WHERE " +
            "(:q = '' OR LOWER(p.productName) LIKE CONCAT('%', :q, '%') " +
            "   OR LOWER(p.description) LIKE CONCAT('%', :q, '%') " +
            "   OR LOWER(p.sku) LIKE CONCAT('%', :q, '%')) " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:rxRequired IS NULL OR p.rxRequired = :rxRequired) " +
            "AND p.stockQuantity >= :minStock " +
            "AND (:activeOnly = false OR p.active = true)")
    Page<Product> search(@Param("q") String q,
                         @Param("categoryId") String categoryId,
                         @Param("rxRequired") Boolean rxRequired,
                         @Param("minStock") int minStock,
                         @Param("activeOnly") boolean activeOnly,
                         Pageable pageable);


    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty " +
            "WHERE p.productId = :id AND p.stockQuantity >= :qty")
    int deductStock(@Param("id") String id, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :qty WHERE p.productId = :id")
    int restoreStock(@Param("id") String id, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = :qty WHERE p.productId = :id")
    int setStock(@Param("id") String id, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Product p SET p.active = :active WHERE p.productId = :id")
    int setActive(@Param("id") String id, @Param("active") boolean active);
}
