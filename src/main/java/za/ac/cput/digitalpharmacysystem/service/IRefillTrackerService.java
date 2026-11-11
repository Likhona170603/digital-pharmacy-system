package za.ac.cput.digitalpharmacysystem.service;

/*
 * Likhona Noroyita
 * 230154239
 */

import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;

public interface IRefillTrackerService extends IService<RefillTracker, String> {

    RefillTracker getByPrescriptionId(
            String prescriptionId
    );

    boolean isRefillEligible(
            String prescriptionId
    );

    RefillTracker processRefill(
            String prescriptionId
    );

    /** Gives back one refill (used when an order that consumed it is cancelled). */
    RefillTracker restoreRefill(
            String prescriptionId
    );
}