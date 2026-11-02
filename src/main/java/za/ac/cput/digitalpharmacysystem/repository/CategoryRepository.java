package za.ac.cput.digitalpharmacysystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import za.ac.cput.digitalpharmacysystem.domain.Category;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByDosageFormIgnoreCase(String dosageForm);

    List<Category> findByScheduleClassificationIgnoreCase(String scheduleClassification);

    List<Category> findByDosageFormIgnoreCaseAndScheduleClassificationIgnoreCase(String dosageForm, String scheduleClassification);

    @Query("SELECT DISTINCT c.dosageForm FROM Category c WHERE c.dosageForm IS NOT NULL ORDER BY c.dosageForm")
    List<String> findDistinctDosageForms();
}

