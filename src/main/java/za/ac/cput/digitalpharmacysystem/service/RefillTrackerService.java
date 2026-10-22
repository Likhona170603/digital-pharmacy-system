package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.stereotype.Service;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;
import za.ac.cput.digitalpharmacysystem.domain.VerificationStatus;
import za.ac.cput.digitalpharmacysystem.repository.IRefillTrackerRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class RefillTrackerService
        implements IRefillTrackerService {

    private final IRefillTrackerRepository repository;
    private final IPrescriptionService prescriptionService;

    public RefillTrackerService(
            IRefillTrackerRepository repository,
            IPrescriptionService prescriptionService) {

        this.repository = repository;
        this.prescriptionService = prescriptionService;
    }

    @Override
    public RefillTracker create(RefillTracker tracker) {
        return repository.save(tracker);
    }

    @Override
    public RefillTracker read(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public RefillTracker update(RefillTracker tracker) {
        return repository.save(tracker);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
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

        return repository.save(tracker);
    }
}