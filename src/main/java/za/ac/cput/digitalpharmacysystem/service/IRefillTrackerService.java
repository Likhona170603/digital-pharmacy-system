package za.ac.cput.digitalpharmacysystem.service;

import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;

import java.util.List;

public interface IRefillTrackerService {

    RefillTracker create(RefillTracker tracker);

    RefillTracker read(String id);

    RefillTracker update(RefillTracker tracker);

    void delete(String id);

    List<RefillTracker> getAll();

    RefillTracker getByPrescriptionId(
            String prescriptionId
    );

    boolean isRefillEligible(
            String prescriptionId
    );

    RefillTracker processRefill(
            String prescriptionId
    );
}