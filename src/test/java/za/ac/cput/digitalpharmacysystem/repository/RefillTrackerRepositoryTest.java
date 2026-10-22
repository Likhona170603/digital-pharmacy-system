package za.ac.cput.digitalpharmacysystem.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;
import za.ac.cput.digitalpharmacysystem.factory.RefillTrackerFactory;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefillTrackerRepositoryTest {

    @Autowired
    private IRefillTrackerRepository repository;

    @Test
    void save() {

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        "RX001",
                        6,
                        LocalDate.now()
                );

        RefillTracker saved =
                repository.save(tracker);

        assertNotNull(saved);
        assertNotNull(
                saved.getRefillTrackerId()
        );

        assertEquals(
                "RX001",
                saved.getPrescriptionId()
        );
    }

    @Test
    void findByPrescriptionId() {

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        "RX002",
                        6,
                        LocalDate.now()
                );

        repository.save(tracker);

        Optional<RefillTracker> result =
                repository.findByPrescriptionId(
                        "RX002"
                );

        assertTrue(result.isPresent());

        assertEquals(
                "RX002",
                result.get().getPrescriptionId()
        );
    }
}