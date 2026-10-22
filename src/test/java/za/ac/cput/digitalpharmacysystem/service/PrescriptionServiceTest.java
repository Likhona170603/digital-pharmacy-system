package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.factory.PrescriptionFactory;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PrescriptionServiceTest {

    @Autowired
    private IPrescriptionService service;

    @Test
    void create() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT101",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        Prescription saved =
                service.create(prescription);

        assertNotNull(saved);

        assertEquals(
                "PAT101",
                saved.getPatientId()
        );
    }

    @Test
    void read() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT102",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        Prescription saved =
                service.create(prescription);

        Prescription found =
                service.read(
                        saved.getPrescriptionId()
                );

        assertNotNull(found);

        assertEquals(
                saved.getPrescriptionId(),
                found.getPrescriptionId()
        );
    }

    @Test
    void getAll() {

        List<Prescription> prescriptions =
                service.getAll();

        assertNotNull(prescriptions);
    }

    @Test
    void getByPatientId() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT103",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        service.create(prescription);

        List<Prescription> result =
                service.getByPatientId("PAT103");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getPendingPrescriptions() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT104",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        service.create(prescription);

        List<Prescription> result =
                service.getPendingPrescriptions();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void approvePrescription() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT105",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        Prescription saved =
                service.create(prescription);

        Prescription approved =
                service.approvePrescription(
                        saved.getPrescriptionId()
                );

        assertNotNull(approved);

        assertEquals(
                "APPROVED",
                approved.getVerificationStatus().name()
        );
    }

    @Test
    void rejectPrescription() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT106",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        Prescription saved =
                service.create(prescription);

        Prescription rejected =
                service.rejectPrescription(
                        saved.getPrescriptionId(),
                        "Prescription is unclear"
                );

        assertNotNull(rejected);

        assertEquals(
                "REJECTED",
                rejected.getVerificationStatus().name()
        );

        assertEquals(
                "Prescription is unclear",
                rejected.getRejectionReason()
        );
    }
}
