package za.ac.cput.digitalpharmacysystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.cput.digitalpharmacysystem.domain.Category;

/*
 * Asenathi Nini
 * 230452817
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
}
