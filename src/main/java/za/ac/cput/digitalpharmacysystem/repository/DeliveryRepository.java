package za.ac.cput.digitalpharmacysystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.cput.digitalpharmacysystem.domain.Delivery;
/*
 * Sesona Ntshatsha
 * 240773365

 */

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery,String> {
}
