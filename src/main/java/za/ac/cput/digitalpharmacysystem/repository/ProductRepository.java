package za.ac.cput.digitalpharmacysystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.cput.digitalpharmacysystem.domain.Product;

import java.util.List;

/*
 * Asenathi Nini
 * 230452817
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByCategory_CategoryId(String categoryId);
}