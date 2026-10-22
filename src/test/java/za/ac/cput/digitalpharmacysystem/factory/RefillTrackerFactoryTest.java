package za.ac.cput.digitalpharmacysystem.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RefillTrackerFactoryTest {

    @Test
    void createRefillTracker() {

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        "RX001",
                        6,
                        LocalDate.now()
                );

        assertNotNull(tracker);
        assertNotNull(
                tracker.getRefillTrackerId()
        );

        assertEquals(
                "RX001",
                tracker.getPrescriptionId()
        );

        assertEquals(
                6,
                tracker.getTotalRefillsAllowed()
        );

        assertEquals(
                0,
                tracker.getRefillsUsed()
        );

        assertEquals(
                LocalDate.now(),
                tracker.getNextEligibleDate()
        );
    }
}