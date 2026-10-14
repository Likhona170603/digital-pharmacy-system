package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.factory.CategoryFactory;
import za.ac.cput.digitalpharmacysystem.factory.ProductFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Asenathi Nini
 * 230452817
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    @Autowired
    private IProductService service;

    @Autowired
    private ICategoryService categoryService;

    private static final Category category = CategoryFactory.createCategory(
            "C-SVC-001", "Antibiotics", "Capsule", "Schedule 4");

    private static final Product product = ProductFactory.createProduct(
            "P-SVC-001", "Amoxicillin", "250mg capsules", 120.00, "SKU-AMX-250-SVC", 50, true, category);

    @Test
    @Order(1)
    void create() {
        categoryService.create(category);

        Product created = service.create(product);
        assertNotNull(created);
        assertEquals(product.getProductId(), created.getProductId());
        assertTrue(created.isRxRequired());
    }

    @Test
    @Order(2)
    void read() {
        Product read = service.read(product.getProductId());
        assertNotNull(read);
        assertEquals(product.getProductId(), read.getProductId());
        assertTrue(read.isRxRequired());
        assertEquals(category.getCategoryId(), read.getCategory().getCategoryId());
    }

    @Test
    @Order(3)
    void update() {
        Product updatedProduct = ProductFactory.createProduct(
                product.getProductId(), "Amoxicillin", "250mg capsules - Updated", 135.00,
                "SKU-AMX-250-SVC", 40, true, category);

        Product updated = service.update(updatedProduct);

        assertNotNull(updated);
        assertEquals(135.00, updated.getPrice());
        assertEquals(40, updated.getStockQuantity());
    }

    @Test
    @Order(4)
    void updateNonExistingReturnsNull() {
        Product ghost = ProductFactory.createProduct(
                "P-DOES-NOT-EXIST", "Ghost", "n/a", 1.00, "SKU-GHOST", 1, false, category);
        assertNull(service.update(ghost));
    }

    @Test
    @Order(5)
    void getAll() {
        List<Product> allProducts = service.getAll();
        assertNotNull(allProducts);
        assertFalse(allProducts.isEmpty());
    }

    @Test
    @Order(6)
    void getByCategoryId() {
        List<Product> products = service.getByCategoryId(category.getCategoryId());
        assertEquals(1, products.size());
        assertEquals(product.getProductId(), products.get(0).getProductId());
        assertTrue(service.getByCategoryId("C-NO-SUCH-CATEGORY").isEmpty());
    }

    @Test
    @Order(7)
    void delete() {
        assertTrue(service.delete(product.getProductId()));
        assertNull(service.read(product.getProductId()));

        categoryService.delete(category.getCategoryId());
    }
}