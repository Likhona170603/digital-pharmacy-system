package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.dto.StockRequest;
import za.ac.cput.digitalpharmacysystem.exception.InsufficientStockException;
import za.ac.cput.digitalpharmacysystem.factory.ProductFactory;
import za.ac.cput.digitalpharmacysystem.service.ICategoryService;
import za.ac.cput.digitalpharmacysystem.service.IProductService;
import za.ac.cput.digitalpharmacysystem.util.Helper;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/*
 * Asenathi Nini
 * 230452817
 */
@RestController
@RequestMapping("/digitalpharmacysystem/product")
public class ProductController {

    private final IProductService service;
    private final ICategoryService categoryService;

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
        Product existing = service.read(valid.getProductId());
        if (existing == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // The factory always builds an active product, so keep the stored flag.
        // Activating/deactivating is done through PATCH /{id}/active only.
        valid = new Product.Builder().copy(valid).setActive(existing.isActive()).build();
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


    @GetMapping("/search")
    public ResponseEntity<Page<Product>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Boolean rxRequired,
            @RequestParam(defaultValue = "false") boolean inStock,
            @PageableDefault(size = 20, sort = "productName") Pageable pageable) {
        return new ResponseEntity<>(service.search(q, categoryId, rxRequired, inStock, pageable), HttpStatus.OK);
    }

    /** Customer-facing list: same filters as /search but only active products. */
    @GetMapping("/catalogue")
    public ResponseEntity<Page<Product>> catalogue(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Boolean rxRequired,
            @RequestParam(defaultValue = "false") boolean inStock,
            @PageableDefault(size = 20, sort = "productName") Pageable pageable) {
        return new ResponseEntity<>(service.search(q, categoryId, rxRequired, inStock, true, pageable), HttpStatus.OK);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Product> setActive(@PathVariable String id, @RequestParam boolean value) {
        return new ResponseEntity<>(service.setActive(id, value), HttpStatus.OK);
    }

    @GetMapping("/rx")
    public ResponseEntity<List<Product>> getRx() {
        return new ResponseEntity<>(service.getByRxRequired(true), HttpStatus.OK);
    }

    @GetMapping("/otc")
    public ResponseEntity<List<Product>> getOtc() {
        return new ResponseEntity<>(service.getByRxRequired(false), HttpStatus.OK);
    }


    @GetMapping("/lowstock")
    public ResponseEntity<List<Product>> getLowStock(@RequestParam(defaultValue = "10") int threshold) {
        return new ResponseEntity<>(service.getLowStock(threshold), HttpStatus.OK);
    }

    @GetMapping("/outofstock")
    public ResponseEntity<List<Product>> getOutOfStock() {
        return new ResponseEntity<>(service.getLowStock(0), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/stock", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Product> setStock(@PathVariable String id, @RequestBody StockRequest request) {
        return new ResponseEntity<>(service.setStock(id, request.getQuantity()), HttpStatus.OK);
    }

    @PostMapping("/{id}/deduct")
    public ResponseEntity<Product> deduct(@PathVariable String id, @RequestBody StockRequest request) {
        return new ResponseEntity<>(service.deductStock(id, request.getQuantity()), HttpStatus.OK);
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Product> restore(@PathVariable String id, @RequestBody StockRequest request) {
        return new ResponseEntity<>(service.restoreStock(id, request.getQuantity()), HttpStatus.OK);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientStock(InsufficientStockException e) {
        return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}