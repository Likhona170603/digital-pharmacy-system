package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.stereotype.Service;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.VerificationStatus;
import za.ac.cput.digitalpharmacysystem.repository.IPrescriptionRepository;

import java.util.List;

@Service
public class PrescriptionService
        implements IPrescriptionService {

    private final IPrescriptionRepository repository;

    public PrescriptionService(
            IPrescriptionRepository repository) {

        this.repository = repository;
    }

    @Override
    public Prescription create(Prescription prescription) {
        return repository.save(prescription);
    }

    @Override
    public Prescription read(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Prescription update(Prescription prescription) {
        return repository.save(prescription);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Prescription> getAll() {
        return repository.findAll();
    }

    @Override
    public List<Prescription> getByPatientId(
            String patientId) {

        return repository.findByPatientId(patientId);
    }

    @Override
    public List<Prescription> getPendingPrescriptions() {

        return repository.findByVerificationStatus(
                VerificationStatus.PENDING
        );
    }

    @Override
    public Prescription approvePrescription(String id) {

        Prescription prescription = read(id);

        if (prescription == null) {
            throw new IllegalArgumentException(
                    "Prescription not found"
            );
        }

        if (prescription.getVerificationStatus()
                != VerificationStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending prescriptions can be approved"
            );
        }

        prescription.setVerificationStatus(
                VerificationStatus.APPROVED
        );

        prescription.setRejectionReason(null);

        return repository.save(prescription);
    }

    @Override
    public Prescription rejectPrescription(
            String id,
            String rejectionReason) {

        if (rejectionReason == null ||
                rejectionReason.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Rejection reason is required"
            );
        }

        Prescription prescription = read(id);

        if (prescription == null) {
            throw new IllegalArgumentException(
                    "Prescription not found"
            );
        }

        if (prescription.getVerificationStatus()
                != VerificationStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending prescriptions can be rejected"
            );
        }

        prescription.setVerificationStatus(
                VerificationStatus.REJECTED
        );

        prescription.setRejectionReason(
                rejectionReason
        );

        return repository.save(prescription);
    }
}