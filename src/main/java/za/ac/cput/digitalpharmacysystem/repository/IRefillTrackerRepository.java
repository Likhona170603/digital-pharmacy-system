package za.ac.cput.digitalpharmacysystem.repository;

/*
 * Likhona Noroyita
 * 230154239
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;

import java.util.Optional;

@Repository
public interface IRefillTrackerRepository
        extends JpaRepository<RefillTracker, String> {

    Optional<RefillTracker> findByPrescriptionId(
            String prescriptionId
    );
}