package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;
import za.ac.cput.digitalpharmacysystem.factory.PrescriptionFactory;
import za.ac.cput.digitalpharmacysystem.factory.RefillTrackerFactory;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefillTrackerServiceTest {

    @Autowired
    private IRefillTrackerService service;

    @Autowired
    private IPrescriptionService prescriptionService;

    @Test
    void create() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT201",
                        "prescriptions/chronic.pdf",
                        LocalDate.now()
                );

        Prescription savedPrescription =
                prescriptionService.create(
                        prescription
                );

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        savedPrescription.getPrescriptionId(),
                        6,
                        LocalDate.now()
                );

        RefillTracker saved =
                service.create(tracker);

        assertNotNull(saved);

        assertEquals(
                savedPrescription.getPrescriptionId(),
                saved.getPrescriptionId()
        );

        assertEquals(
                6,
                saved.getTotalRefillsAllowed()
        );

        assertEquals(
                0,
                saved.getRefillsUsed()
        );
    }

    @Test
    void read() {

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        "RX201",
                        6,
                        LocalDate.now()
                );

        RefillTracker saved =
                service.create(tracker);

        RefillTracker found =
                service.read(
                        saved.getRefillTrackerId()
                );

        assertNotNull(found);

        assertEquals(
                saved.getRefillTrackerId(),
                found.getRefillTrackerId()
        );
    }

    @Test
    void getByPrescriptionId() {

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        "RX202",
                        6,
                        LocalDate.now()
                );

        service.create(tracker);

        RefillTracker found =
                service.getByPrescriptionId(
                        "RX202"
                );

        assertNotNull(found);

        assertEquals(
                "RX202",
                found.getPrescriptionId()
        );
    }

    @Test
    void refillNotEligibleWhenPrescriptionIsPending() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT203",
                        "prescriptions/chronic.pdf",
                        LocalDate.now()
                );

        Prescription savedPrescription =
                prescriptionService.create(
                        prescription
                );

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        savedPrescription.getPrescriptionId(),
                        6,
                        LocalDate.now()
                );

        service.create(tracker);

        boolean eligible =
                service.isRefillEligible(
                        savedPrescription.getPrescriptionId()
                );

        assertFalse(eligible);
    }

    @Test
    void refillEligibleWhenPrescriptionIsApproved() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT204",
                        "prescriptions/chronic.pdf",
                        LocalDate.now()
                );

        Prescription savedPrescription =
                prescriptionService.create(
                        prescription
                );

        prescriptionService.approvePrescription(
                savedPrescription.getPrescriptionId()
        );

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        savedPrescription.getPrescriptionId(),
                        6,
                        LocalDate.now()
                );

        service.create(tracker);

        boolean eligible =
                service.isRefillEligible(
                        savedPrescription.getPrescriptionId()
                );

        assertTrue(eligible);
    }

    @Test
    void processRefill() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT205",
                        "prescriptions/chronic.pdf",
                        LocalDate.now()
                );

        Prescription savedPrescription =
                prescriptionService.create(
                        prescription
                );

        prescriptionService.approvePrescription(
                savedPrescription.getPrescriptionId()
        );

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        savedPrescription.getPrescriptionId(),
                        6,
                        LocalDate.now()
                );

        service.create(tracker);

        RefillTracker processed =
                service.processRefill(
                        savedPrescription.getPrescriptionId()
                );

        assertNotNull(processed);

        assertEquals(
                1,
                processed.getRefillsUsed()
        );

        assertEquals(
                LocalDate.now().plusDays(30),
                processed.getNextEligibleDate()
        );
    }
}