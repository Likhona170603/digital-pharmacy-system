package za.ac.cput.digitalpharmacysystem.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.digitalpharmacysystem.domain.Prescription;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionFactoryTest {

    @Test
    void createPrescription() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT001",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        assertNotNull(prescription);
        assertNotNull(prescription.getPrescriptionId());

        assertEquals(
                "PAT001",
                prescription.getPatientId()
        );

        assertEquals(
                "prescriptions/test.pdf",
                prescription.getFilePath()
        );

        assertEquals(
                LocalDate.now(),
                prescription.getIssuedDate()
        );

        assertEquals(
                "PENDING",
                prescription.getVerificationStatus().name()
        );
    }
}