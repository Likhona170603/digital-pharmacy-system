package za.ac.cput.digitalpharmacysystem.service;

/*
 * Likhona Noroyita
 * 230154239
 */

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.domain.VerificationStatus;
import za.ac.cput.digitalpharmacysystem.factory.RefillTrackerFactory;
import za.ac.cput.digitalpharmacysystem.repository.IPrescriptionRepository;
import za.ac.cput.digitalpharmacysystem.repository.IRefillTrackerRepository;
import za.ac.cput.digitalpharmacysystem.util.ActorContext;
import za.ac.cput.digitalpharmacysystem.util.Helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class PrescriptionService
        implements IPrescriptionService {

    private final IPrescriptionRepository repository;
    // The repository (not IRefillTrackerService) is used here because
    // RefillTrackerService already depends on this service.
    private final IRefillTrackerRepository refillRepository;
    private final IAuditLogService auditLogService;


    public PrescriptionService(
            IPrescriptionRepository repository,
            IRefillTrackerRepository refillRepository,
            IAuditLogService auditLogService) {
        this.repository = repository;
        this.refillRepository = refillRepository;
        this.auditLogService = auditLogService;
    }
    @Override
    public List<Prescription> getPrescriptionsForCurrentUser() {
        User currentUser = ActorContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User is not authenticated.");
        }

        String role = currentUser.getRole() != null ? currentUser.getRole().name() : "";

        // Admin and Pharmacist can see all prescriptions
        if ("ADMIN".equalsIgnoreCase(role) || "PHARMACIST".equalsIgnoreCase(role)) {
            return repository.findAll();
        }

        // Customers can only see their own prescriptions by patientId
        return repository.findByPatientId(currentUser.getId().toString());
    }


    @Override
    public Prescription create(Prescription prescription) {
        Prescription saved = repository.save(prescription);
        auditLogService.record("PRESCRIPTION_UPLOADED", "Prescription", saved.getPrescriptionId());
        return saved;
    }

    @Override
    public Prescription read(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Prescription update(Prescription prescription) {
        if (repository.existsById(prescription.getPrescriptionId())) {
            return repository.save(prescription);
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
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
    @Transactional
    public Prescription approvePrescription(
            String id,
            int refillsAllowed,
            String verifiedBy) {

        Prescription prescription = findOrThrow(id);

        requireDecidable(prescription, "approved");

        prescription.setVerificationStatus(
                VerificationStatus.APPROVED
        );

        prescription.setRejectionReason(null);

        stamp(prescription, verifiedBy);

        Prescription saved = repository.save(prescription);

        // The pharmacist decides how many times it can be dispensed (at least once).
        if (refillRepository.findByPrescriptionId(id).isEmpty()) {
            RefillTracker tracker = RefillTrackerFactory.createRefillTracker(
                    id,
                    Math.max(1, refillsAllowed),
                    LocalDate.now()
            );
            refillRepository.save(tracker);
        }

        auditLogService.record("PRESCRIPTION_APPROVED", "Prescription", id);

        return saved;
    }

    @Override
    @Transactional
    public Prescription rejectPrescription(
            String id,
            String rejectionReason,
            String verifiedBy) {

        return decide(
                id,
                rejectionReason,
                verifiedBy,
                VerificationStatus.REJECTED,
                "rejected",
                "PRESCRIPTION_REJECTED"
        );
    }

    @Override
    @Transactional
    public Prescription requestClarification(
            String id,
            String reason,
            String verifiedBy) {

        return decide(
                id,
                reason,
                verifiedBy,
                VerificationStatus.NEEDS_CLARIFICATION,
                "sent back for clarification",
                "PRESCRIPTION_NEEDS_CLARIFICATION"
        );
    }

    private Prescription decide(
            String id,
            String reason,
            String verifiedBy,
            VerificationStatus status,
            String verb,
            String auditAction) {

        if (reason == null || reason.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "A reason is required"
            );
        }

        Prescription prescription = findOrThrow(id);

        requireDecidable(prescription, verb);

        prescription.setVerificationStatus(status);

        prescription.setRejectionReason(reason);

        stamp(prescription, verifiedBy);

        Prescription saved = repository.save(prescription);

        auditLogService.record(auditAction, "Prescription", id);

        return saved;
    }

    private void requireDecidable(Prescription prescription, String verb) {
        VerificationStatus status = prescription.getVerificationStatus();

        if (status != VerificationStatus.PENDING
                && status != VerificationStatus.NEEDS_CLARIFICATION) {

            throw new IllegalStateException(
                    "Only pending prescriptions can be " + verb
                            + " (this one is " + status + ")"
            );
        }
    }

    private void stamp(Prescription prescription, String verifiedBy) {
        String by = verifiedBy;

        if (Helper.isNullOrEmpty(by) && ActorContext.getCurrentUser() != null) {
            by = String.valueOf(ActorContext.getCurrentUser().getId());
        }

        prescription.setVerifiedBy(by);
        prescription.setVerifiedAt(LocalDateTime.now());
    }

    private Prescription findOrThrow(String id){
            Prescription prescription = read(id);
            if (prescription == null) {
                throw new NoSuchElementException(
                        "Prescription not found: " + id
                );
            }
            return prescription;
        }
    }
