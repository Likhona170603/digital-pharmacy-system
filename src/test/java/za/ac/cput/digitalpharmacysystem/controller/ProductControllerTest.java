package za.ac.cput.digitalpharmacysystem.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.factory.CategoryFactory;
import za.ac.cput.digitalpharmacysystem.factory.ProductFactory;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Asenathi Nini
 * 230452817
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseURL = "/digitalpharmacysystem/product";
    private final String categoryURL = "/digitalpharmacysystem/category";
    private static Product product;
    private static Category category;

    @BeforeAll
    static void setUp() {
        category = CategoryFactory.createCategory(
                "C-PCTRL-001",
                "Antibiotics",
                "Capsule",
                "Schedule 4"
        );

        product = ProductFactory.createProduct(
                "P-PCTRL-001",
                "Amoxicillin",
                "250mg capsules",
                120.00,
                "SKU-AMX-250-CTRL",
                50,
                true,
                category
        );
    }

    @Test
    @Order(1)
    void a_createCategoryFirst() {

        ResponseEntity<Category> response =
                restTemplate.postForEntity(categoryURL + "/create", category, Category.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Order(2)
    void b_create() {
        String url = baseURL + "/create";
        ResponseEntity<Product> postResponse = restTemplate.postForEntity(url, product, Product.class);

        assertNotNull(postResponse);
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        assertEquals(product.getProductId(), postResponse.getBody().getProductId());
        // Regression check: the prescription flag must survive the JSON round trip.
        assertTrue(postResponse.getBody().isRxRequired());
    }

    @Test
    @Order(3)
    void c_createDuplicateReturnsConflict() {
        ResponseEntity<Product> response = restTemplate.postForEntity(baseURL + "/create", product, Product.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(4)
    void d_createDuplicateSkuReturnsConflict() {
        Product sameSku = ProductFactory.createProduct(
                "P-PCTRL-002", "Other product", "n/a", 10.00, product.getSku(), 5, false, category);
        ResponseEntity<Product> response = restTemplate.postForEntity(baseURL + "/create", sameSku, Product.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(5)
    void e_createWithUnknownCategoryReturnsBadRequest() {
        Category unknown = CategoryFactory.createCategory("C-NO-SUCH-CATEGORY", "Ghost", "Tablet", "Schedule 0");
        Product orphan = ProductFactory.createProduct(
                "P-PCTRL-003", "Orphan", "n/a", 10.00, "SKU-ORPHAN", 5, false, unknown);
        ResponseEntity<Product> response = restTemplate.postForEntity(baseURL + "/create", orphan, Product.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(6)
    void f_read() {
        String url = baseURL + "/read/" + product.getProductId();
        ResponseEntity<Product> response = restTemplate.getForEntity(url, Product.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(product.getProductId(), response.getBody().getProductId());
        assertTrue(response.getBody().isRxRequired());
        assertEquals(category.getCategoryId(), response.getBody().getCategory().getCategoryId());
    }

    @Test
    @Order(7)
    void g_update() {
        Product updatedProduct = ProductFactory.createProduct(
                product.getProductId(),
                "Amoxicillin",
                "250mg capsules - Updated",
                135.00,
                product.getSku(),
                40,
                false,
                category
        );

        String url = baseURL + "/update";
        ResponseEntity<Product> response = restTemplate.postForEntity(url, updatedProduct, Product.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(135.00, response.getBody().getPrice());
        assertFalse(response.getBody().isRxRequired());
    }

    @Test
    @Order(8)
    void h_getAll() {
        String url = baseURL + "/getall";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Product[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Product[].class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    @Order(9)
    void i_getByCategory() {
        String url = baseURL + "/bycategory/" + category.getCategoryId();
        ResponseEntity<Product[]> response = restTemplate.getForEntity(url, Product[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals(product.getProductId(), response.getBody()[0].getProductId());
    }

    @Test
    @Order(10)
    void j_deleteCategoryInUseReturnsConflict() {
        ResponseEntity<Void> response = restTemplate.exchange(
                categoryURL + "/delete/" + category.getCategoryId(), HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(11)
    void k_delete() {
        String url = baseURL + "/delete/" + product.getProductId();
        restTemplate.delete(url);

        String readUrl = baseURL + "/read/" + product.getProductId();
        ResponseEntity<Product> response = restTemplate.getForEntity(readUrl, Product.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(12)
    void l_cleanUpCategory() {
        ResponseEntity<Void> response = restTemplate.exchange(
                categoryURL + "/delete/" + category.getCategoryId(), HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}