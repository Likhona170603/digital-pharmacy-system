package za.ac.cput.digitalpharmacysystem.repository;

/*
 * Likhona Noroyita
 * 230154239
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.VerificationStatus;

import java.util.List;

@Repository
public interface IPrescriptionRepository
        extends JpaRepository<Prescription, String> {

    List<Prescription> findByPatientId(String patientId);

    List<Prescription> findByVerificationStatus(
            VerificationStatus verificationStatus
    );
}