package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.exception.InsufficientStockException;
import za.ac.cput.digitalpharmacysystem.repository.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/*
 * Asenathi Nini
 * 230452817
 */
@Service
public class ProductService implements IProductService {

    private final ProductRepository repository;
    private final IAuditLogService auditLogService;

    public ProductService(ProductRepository repository, IAuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @Override
    public Product create(Product product) {
        return this.repository.save(product);
    }

    @Override
    public Product read(String productId) {
        return this.repository.findById(productId).orElse(null);
    }

    @Override
    public Product update(Product product) {
        if (this.repository.existsById(product.getProductId())) {
            return this.repository.save(product);
        }
        return null;
    }

    @Override
    public boolean delete(String productId) {
        if (this.repository.existsById(productId)) {
            this.repository.deleteById(productId);
            return true;
        }
        return false;
    }

    @Override
    public List<Product> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Product> getByCategoryId(String categoryId) {
        return this.repository.findByCategory_CategoryId(categoryId);
    }

    @Override
    public Page<Product> search(String q, String categoryId, Boolean rxRequired, boolean inStockOnly, Pageable pageable) {
        return search(q, categoryId, rxRequired, inStockOnly, false, pageable);
    }

    @Override
    public Page<Product> search(String q, String categoryId, Boolean rxRequired, boolean inStockOnly, boolean activeOnly, Pageable pageable) {
        String term = (q == null) ? "" : q.trim().toLowerCase();
        String category = (categoryId == null || categoryId.isBlank()) ? null : categoryId;
        return this.repository.search(term, category, rxRequired, inStockOnly ? 1 : 0, activeOnly, pageable);
    }

    @Override
    public List<Product> getByRxRequired(boolean rxRequired) {
        return this.repository.findByRxRequired(rxRequired);
    }

    @Override
    public List<Product> getLowStock(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must be 0 or more");
        }
        return this.repository.findByStockQuantityLessThanEqualOrderByStockQuantityAsc(threshold);
    }

    @Override
    @Transactional
    public Product deductStock(String productId, int quantity) {
        requirePositive(quantity);
        int updated = this.repository.deductStock(productId, quantity);
        if (updated == 0) {
            if (!this.repository.existsById(productId)) {
                throw new NoSuchElementException("Product not found: " + productId);
            }
            throw new InsufficientStockException(productId, quantity);
        }
        return this.repository.findById(productId).orElseThrow();
    }

    @Override
    @Transactional
    public void deductStock(Map<String, Integer> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No items to deduct");
        }

        for (Map.Entry<String, Integer> item : new TreeMap<>(items).entrySet()) {
            deductStock(item.getKey(), item.getValue());
        }
    }

    @Override
    @Transactional
    public Product restoreStock(String productId, int quantity) {
        requirePositive(quantity);
        if (this.repository.restoreStock(productId, quantity) == 0) {
            throw new NoSuchElementException("Product not found: " + productId);
        }
        return this.repository.findById(productId).orElseThrow();
    }

    @Override
    @Transactional
    public Product setStock(String productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (this.repository.setStock(productId, quantity) == 0) {
            throw new NoSuchElementException("Product not found: " + productId);
        }
        this.auditLogService.record("STOCK_ADJUSTED", "Product", productId);
        return this.repository.findById(productId).orElseThrow();
    }

    @Override
    @Transactional
    public Product setActive(String productId, boolean active) {
        if (this.repository.setActive(productId, active) == 0) {
            throw new NoSuchElementException("Product not found: " + productId);
        }
        this.auditLogService.record(active ? "PRODUCT_ACTIVATED" : "PRODUCT_DEACTIVATED", "Product", productId);
        return this.repository.findById(productId).orElseThrow();
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}