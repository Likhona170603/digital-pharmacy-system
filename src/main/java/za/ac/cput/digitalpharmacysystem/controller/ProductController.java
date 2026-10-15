package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.factory.ProductFactory;
import za.ac.cput.digitalpharmacysystem.service.ICategoryService;
import za.ac.cput.digitalpharmacysystem.service.IProductService;
import za.ac.cput.digitalpharmacysystem.util.Helper;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
@RestController
@RequestMapping("/digitalpharmacysystem/product")
public class ProductController {

    private final IProductService service;
    private final ICategoryService categoryService;

    @Autowired
    public ProductController(IProductService service, ICategoryService categoryService) {
        this.service = service;
        this.categoryService = categoryService;
    }

    private Product validated(Product p) {
        if (p == null || p.getCategory() == null || Helper.isNullOrEmpty(p.getCategory().getCategoryId())) {
            return null;
        }
        Category category = categoryService.read(p.getCategory().getCategoryId());
        if (category == null) {
            return null;
        }
        return ProductFactory.createProduct(
                p.getProductId(), p.getProductName(), p.getDescription(), p.getPrice(),
                p.getSku(), p.getStockQuantity(), p.isRxRequired(), category);
    }

    @PostMapping("/create")
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product valid = validated(product);
        if (valid == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (service.read(valid.getProductId()) != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        try {
            return new ResponseEntity<>(service.create(valid), HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) { // e.g. duplicate SKU
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Product> read(@PathVariable String id) {
        Product product = service.read(id);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<Product> update(@RequestBody Product product) {
        Product valid = validated(product);
        if (valid == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Product updated = service.update(valid);
            if (updated == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (DataIntegrityViolationException e) { // e.g. SKU already used by another product
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Product>> getAll() {
        List<Product> products = service.getAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/bycategory/{categoryId}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String categoryId) {
        return new ResponseEntity<>(service.getByCategoryId(categoryId), HttpStatus.OK);
    }
}
