package za.ac.cput.digitalpharmacysystem.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.VerificationStatus;
import za.ac.cput.digitalpharmacysystem.factory.PrescriptionFactory;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PrescriptionRepositoryTest {

    @Autowired
    private IPrescriptionRepository repository;

    @Test
    void save() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT001",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        Prescription saved =
                repository.save(prescription);

        assertNotNull(saved);
        assertNotNull(
                saved.getPrescriptionId()
        );

        assertEquals(
                "PAT001",
                saved.getPatientId()
        );
    }

    @Test
    void findByPatientId() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT002",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        repository.save(prescription);

        List<Prescription> result =
                repository.findByPatientId("PAT002");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void findByVerificationStatus() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT003",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        repository.save(prescription);

        List<Prescription> result =
                repository.findByVerificationStatus(
                        VerificationStatus.PENDING
                );

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}