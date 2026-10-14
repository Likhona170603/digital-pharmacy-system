package za.ac.cput.digitalpharmacysystem.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.domain.Product;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Asenathi Nini
 * 230452817
 */
class ProductFactoryTest {

    private final Category category = CategoryFactory.createCategory("C001", "Painkillers", "Tablet", "Schedule 2");

    private Product build(String id, String name, double price, String sku, int stock, Category cat) {
        return ProductFactory.createProduct(id, name, "500mg tablets", price, sku, stock, true, cat);
    }

    @Test
    void testCreateProductSuccess() {
        Product product = build("P001", "Panado", 45.00, "SKU-PAN-500", 100, category);

        assertNotNull(product);
        assertEquals("P001", product.getProductId());
        assertEquals("Panado", product.getProductName());
        assertEquals(45.00, product.getPrice());
        assertEquals("SKU-PAN-500", product.getSku());
        assertEquals(100, product.getStockQuantity());
        assertTrue(product.isRxRequired());
        assertNotNull(product.getCategory());
        System.out.println("Success Test Passed: " + product);
    }

    @Test
    void testCreateProductNegativePriceFailure() {
        assertNull(build("P001", "Panado", -10.00, "SKU-PAN-500", 100, category));
    }

    @Test
    void testCreateProductNegativeStockFailure() {
        assertNull(build("P001", "Panado", 45.00, "SKU-PAN-500", -1, category));
    }

    @Test
    void testCreateProductNullCategoryFailure() {
        assertNull(build("P001", "Panado", 45.00, "SKU-PAN-500", 100, null));
    }

    @Test
    void testCreateProductEmptyIdFailure() {
        assertNull(build("", "Panado", 45.00, "SKU-PAN-500", 100, category));
    }

    @Test
    void testCreateProductNullIdFailure() {
        assertNull(build(null, "Panado", 45.00, "SKU-PAN-500", 100, category));
    }

    @Test
    void testCreateProductEmptyNameFailure() {
        assertNull(build("P001", "", 45.00, "SKU-PAN-500", 100, category));
    }

    @Test
    void testCreateProductEmptySkuFailure() {
        assertNull(build("P001", "Panado", 45.00, " ", 100, category));
    }

    @Test
    void testZeroPriceAndZeroStockAreAllowed() {
        assertNotNull(build("P001", "Free sample", 0.0, "SKU-FREE", 0, category));
    }
}