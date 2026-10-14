package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.repository.ProductRepository;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
@Service
public class ProductService implements IProductService {

    private final ProductRepository repository;

    @Autowired
    public ProductService(ProductRepository repository) {
        this.repository = repository;
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
}