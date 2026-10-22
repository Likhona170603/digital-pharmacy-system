package za.ac.cput.digitalpharmacysystem.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.digitalpharmacysystem.domain.Category;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Asenathi Nini
 * 230452817
 */
class CategoryFactoryTest {

    @Test
    void testCreateCategorySuccess() {
        Category category = CategoryFactory.createCategory("C001", "Painkillers", "Tablet", "Schedule 2");
        assertNotNull(category);
        assertEquals("C001", category.getCategoryId());
        assertEquals("Painkillers", category.getCategoryName());
        System.out.println("Success Test Passed: " + category);
    }

    @Test
    void testCreateCategoryFailure() {
        Category category = CategoryFactory.createCategory("C001", "", "Tablet", "Schedule 2");
        assertNull(category);
    }
}
