package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.digitalpharmacysystem.domain.Category;
import za.ac.cput.digitalpharmacysystem.factory.CategoryFactory;
import za.ac.cput.digitalpharmacysystem.repository.CategoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Asenathi Nini
 * 230452817
 */
@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    private final Category syrup = CategoryFactory.createCategory("C-Q-1", "Cough", "Syrup", "Schedule 2");

    @Test
    void search_noFilters_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(syrup));
        assertEquals(1, service.search(null, " ").size());
    }

    @Test
    void search_byDosageFormOnly() {
        when(repository.findByDosageFormIgnoreCase("syrup")).thenReturn(List.of(syrup));
        assertEquals(1, service.search(" syrup ", null).size());
    }

    @Test
    void search_byScheduleOnly() {
        when(repository.findByScheduleClassificationIgnoreCase("Schedule 2")).thenReturn(List.of(syrup));
        assertEquals(1, service.search(null, "Schedule 2").size());
    }

    @Test
    void search_byBoth() {
        when(repository.findByDosageFormIgnoreCaseAndScheduleClassificationIgnoreCase("Syrup", "Schedule 2"))
                .thenReturn(List.of(syrup));
        assertEquals(1, service.search("Syrup", "Schedule 2").size());
    }

    @Test
    void getDosageForms_delegates() {
        when(repository.findDistinctDosageForms()).thenReturn(List.of("Syrup", "Tablet"));
        assertEquals(List.of("Syrup", "Tablet"), service.getDosageForms());
        verify(repository).findDistinctDosageForms();
    }
}
