package za.ac.cput.digitalpharmacysystem.service;

import za.ac.cput.digitalpharmacysystem.domain.Delivery;
/*
 * Likhona Noroyita
 * 230154239
 */

import za.ac.cput.digitalpharmacysystem.domain.Prescription;

import java.util.List;

public interface IPrescriptionService extends IService<Prescription, String> {

    List<Prescription> getByPatientId(String patientId);
    List<Prescription> getPrescriptionsForCurrentUser();
    List<Prescription> getPendingPrescriptions();

    /**
     * Approves a pending (or clarification-needed) prescription and creates its refill tracker
     * allowing the given number of dispenses (at least 1).
     */
    Prescription approvePrescription(String id, int refillsAllowed, String verifiedBy);

    Prescription rejectPrescription(String id, String rejectionReason, String verifiedBy);

    Prescription requestClarification(String id, String reason, String verifiedBy);

    default Prescription approvePrescription(String id) {
        return approvePrescription(id, 1, null);
    }

    default Prescription rejectPrescription(String id, String rejectionReason) {
        return rejectPrescription(id, rejectionReason, null);
    }
}
