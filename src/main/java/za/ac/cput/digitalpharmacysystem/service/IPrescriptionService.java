package za.ac.cput.digitalpharmacysystem.service;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;

import java.util.List;

public interface IPrescriptionService {

    Prescription create(Prescription prescription);

    Prescription read(String id);

    Prescription update(Prescription prescription);

    void delete(String id);

    List<Prescription> getAll();

    List<Prescription> getByPatientId(String patientId);

    List<Prescription> getPendingPrescriptions();

    Prescription approvePrescription(String id);

    Prescription rejectPrescription(
            String id,
            String rejectionReason
    );
}