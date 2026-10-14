package za.ac.cput.digitalpharmacysystem.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.factory.CategoryFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Asenathi Nini
 * 230452817
 */
@AutoConfigureTestRestTemplate
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseURL = "/digitalpharmacysystem/category";
    private static Category category;

    @BeforeAll
    static void setUp() {
        category = CategoryFactory.createCategory(
                "C-CTRL-001",
                "Painkillers",
                "Tablet",
                "Schedule 2"
        );
    }

    @Test
    @Order(1)
    void a_create() {
        String url = baseURL + "/create";
        ResponseEntity<Category> postResponse = restTemplate.postForEntity(url, category, Category.class);

        assertNotNull(postResponse);
        assertNotNull(postResponse.getBody());
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertEquals(category.getCategoryId(), postResponse.getBody().getCategoryId());
    }

    @Test
    @Order(2)
    void b_createDuplicateReturnsConflict() {
        ResponseEntity<Category> response = restTemplate.postForEntity(baseURL + "/create", category, Category.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(3)
    void c_createInvalidReturnsBadRequest() {
        Map<String, String> invalid = Map.of("categoryId", "C-CTRL-BAD", "categoryName", "");
        ResponseEntity<Category> response = restTemplate.postForEntity(baseURL + "/create", invalid, Category.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(4)
    void d_read() {
        String url = baseURL + "/read/" + category.getCategoryId();
        ResponseEntity<Category> response = restTemplate.getForEntity(url, Category.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(category.getCategoryId(), response.getBody().getCategoryId());
    }

    @Test
    @Order(5)
    void e_update() {
        Category updatedCategory = CategoryFactory.createCategory(
                category.getCategoryId(),
                "Pain Relief",
                "Capsule",
                "Schedule 2"
        );

        String url = baseURL + "/update";
        ResponseEntity<Category> response = restTemplate.postForEntity(url, updatedCategory, Category.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Pain Relief", response.getBody().getCategoryName());
    }

    @Test
    @Order(6)
    void f_updateNonExistingReturnsNotFound() {
        Category ghost = CategoryFactory.createCategory("C-CTRL-GHOST", "Ghost", "Tablet", "Schedule 0");
        ResponseEntity<Category> response = restTemplate.postForEntity(baseURL + "/update", ghost, Category.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(7)
    void g_getAll() {
        String url = baseURL + "/getall";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Category[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Category[].class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    @Order(8)
    void h_delete() {
        String url = baseURL + "/delete/" + category.getCategoryId();
        restTemplate.delete(url);

        String readUrl = baseURL + "/read/" + category.getCategoryId();
        ResponseEntity<Category> response = restTemplate.getForEntity(readUrl, Category.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
