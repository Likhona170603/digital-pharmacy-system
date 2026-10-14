package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.factory.CategoryFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Asenathi Nini
 * 230452817
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryServiceTest {

    @Autowired
    private ICategoryService service;

    private static final Category category = CategoryFactory.createCategory(
            "C-CSVC-001", "Vitamins", "Tablet", "Schedule 0");

    @Test
    @Order(1)
    void create() {
        Category created = service.create(category);
        assertNotNull(created);
        assertEquals(category.getCategoryId(), created.getCategoryId());
    }

    @Test
    @Order(2)
    void read() {
        Category read = service.read(category.getCategoryId());
        assertNotNull(read);
        assertEquals("Vitamins", read.getCategoryName());
    }

    @Test
    @Order(3)
    void update() {
        Category changed = new Category.Builder().copy(category).setCategoryName("Supplements").build();
        Category updated = service.update(changed);
        assertNotNull(updated);
        assertEquals("Supplements", updated.getCategoryName());
    }

    @Test
    @Order(4)
    void updateNonExistingReturnsNull() {
        Category ghost = CategoryFactory.createCategory("C-DOES-NOT-EXIST", "Ghost", "Tablet", "Schedule 0");
        assertNull(service.update(ghost));
    }

    @Test
    @Order(5)
    void getAll() {
        List<Category> all = service.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    @Test
    @Order(6)
    void delete() {
        assertTrue(service.delete(category.getCategoryId()));
        assertNull(service.read(category.getCategoryId()));
    }

    @Test
    @Order(7)
    void deleteNonExistingReturnsFalse() {
        assertFalse(service.delete(category.getCategoryId()));
    }
}