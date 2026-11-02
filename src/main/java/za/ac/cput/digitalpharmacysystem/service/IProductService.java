package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import za.ac.cput.digitalpharmacysystem.domain.Product;

import java.util.List;
import java.util.Map;

/*
 * Asenathi Nini
 * 230452817
 */
public interface IProductService extends IService<Product, String> {
    List<Product> getByCategoryId(String categoryId);

    Page<Product> search(String q, String categoryId, Boolean rxRequired, boolean inStockOnly, Pageable pageable);

    List<Product> getByRxRequired(boolean rxRequired);

    List<Product> getLowStock(int threshold);

    Product deductStock(String productId, int quantity);

    void deductStock(Map<String, Integer> items);

    Product restoreStock(String productId, int quantity);

    Product setStock(String productId, int quantity);

    Page<Product> search(String q, String categoryId, Boolean rxRequired, boolean inStockOnly, boolean activeOnly, Pageable pageable);

    /** Deactivated products disappear from the catalogue and cannot be ordered. */
    Product setActive(String productId, boolean active);
}