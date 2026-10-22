package za.ac.cput.digitalpharmacysystem.factory;

import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;

import java.time.LocalDate;
import java.util.UUID;

public class RefillTrackerFactory {

    public static RefillTracker createRefillTracker(
            String prescriptionId,
            int totalRefillsAllowed,
            LocalDate nextEligibleDate) {

        if (prescriptionId == null ||
                prescriptionId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Prescription ID is required"
            );
        }

        if (totalRefillsAllowed < 0) {
            throw new IllegalArgumentException(
                    "Total refills cannot be negative"
            );
        }

        if (nextEligibleDate == null) {
            throw new IllegalArgumentException(
                    "Next eligible date is required"
            );
        }

        return new RefillTracker.Builder()
                .setRefillTrackerId(
                        UUID.randomUUID().toString()
                )
                .setPrescriptionId(prescriptionId)
                .setTotalRefillsAllowed(totalRefillsAllowed)
                .setRefillsUsed(0)
                .setNextEligibleDate(nextEligibleDate)
                .build();
    }
}