package za.ac.cput.digitalpharmacysystem.service;

/*
 * Likhona Noroyita
 * 230154239
 */

import org.springframework.stereotype.Service;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;
import za.ac.cput.digitalpharmacysystem.domain.VerificationStatus;
import za.ac.cput.digitalpharmacysystem.repository.IRefillTrackerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RefillTrackerService
        implements IRefillTrackerService {

    private final IRefillTrackerRepository repository;
    private final IPrescriptionService prescriptionService;
    private final IAuditLogService auditLogService;

    public RefillTrackerService(
            IRefillTrackerRepository repository,
            IPrescriptionService prescriptionService,
            IAuditLogService auditLogService) {

        this.repository = repository;
        this.prescriptionService = prescriptionService;
        this.auditLogService = auditLogService;
    }

    @Override
    public RefillTracker create(RefillTracker tracker) {
        // One tracker per prescription: creating a second one for the same prescription
        // updates the existing tracker instead of adding a duplicate.
        if (tracker.getPrescriptionId() != null) {
            Optional<RefillTracker> existing =
                    repository.findByPrescriptionId(tracker.getPrescriptionId());

            if (existing.isPresent() && !existing.get().getRefillTrackerId()
                    .equals(tracker.getRefillTrackerId())) {

                tracker = new RefillTracker.Builder()
                        .setRefillTrackerId(existing.get().getRefillTrackerId())
                        .setPrescriptionId(tracker.getPrescriptionId())
                        .setTotalRefillsAllowed(tracker.getTotalRefillsAllowed())
                        .setRefillsUsed(tracker.getRefillsUsed())
                        .setNextEligibleDate(tracker.getNextEligibleDate())
                        .build();
            }
        }
        return repository.save(tracker);
    }

    @Override
    public RefillTracker read(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public RefillTracker update(RefillTracker tracker) {
        if (repository.existsById(tracker.getRefillTrackerId())) {
            return repository.save(tracker);
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
    public List<RefillTracker> getAll() {
        return repository.findAll();
    }

    @Override
    public RefillTracker getByPrescriptionId(
            String prescriptionId) {

        return repository
                .findByPrescriptionId(prescriptionId)
                .orElse(null);
    }

    @Override
    public boolean isRefillEligible(
            String prescriptionId) {

        Prescription prescription =
                prescriptionService.read(prescriptionId);

        if (prescription == null) {
            return false;
        }

        if (prescription.getVerificationStatus()
                != VerificationStatus.APPROVED) {

            return false;
        }

        RefillTracker tracker =
                getByPrescriptionId(prescriptionId);

        if (tracker == null) {
            return false;
        }

        if (tracker.getRefillsUsed()
                >= tracker.getTotalRefillsAllowed()) {

            return false;
        }

        return !LocalDate.now()
                .isBefore(tracker.getNextEligibleDate());
    }

    @Override
    public RefillTracker processRefill(
            String prescriptionId) {

        if (!isRefillEligible(prescriptionId)) {

            throw new IllegalStateException(
                    "Refill is not eligible"
            );
        }

        RefillTracker tracker =
                getByPrescriptionId(prescriptionId);

        tracker.setRefillsUsed(
                tracker.getRefillsUsed() + 1
        );

        tracker.setNextEligibleDate(
                LocalDate.now().plusDays(30)
        );

        RefillTracker saved = repository.save(tracker);

        auditLogService.record("REFILL_USED", "RefillTracker", saved.getRefillTrackerId());

        return saved;
    }

    @Override
    public RefillTracker restoreRefill(String prescriptionId) {

        RefillTracker tracker = getByPrescriptionId(prescriptionId);

        if (tracker == null) {
            return null;
        }

        tracker.setRefillsUsed(
                Math.max(0, tracker.getRefillsUsed() - 1)
        );

        // The refill is available again straight away.
        tracker.setNextEligibleDate(LocalDate.now());

        RefillTracker saved = repository.save(tracker);

        auditLogService.record("REFILL_RESTORED", "RefillTracker", saved.getRefillTrackerId());

        return saved;
    }
}
