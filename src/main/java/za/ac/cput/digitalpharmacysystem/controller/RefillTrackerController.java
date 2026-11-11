package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;
import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.service.IPrescriptionService;
import za.ac.cput.digitalpharmacysystem.service.IRefillTrackerService;

import java.util.List;

@RestController
@RequestMapping("/digitalpharmacysystem/refills")
public class RefillTrackerController {

    private final IRefillTrackerService service;
    private final IPrescriptionService prescriptionService;

    public RefillTrackerController(
            IRefillTrackerService service,
            IPrescriptionService prescriptionService) {

        this.service = service;
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<RefillTracker> create(
            @RequestBody RefillTracker tracker) {

        RefillTracker created =
                service.create(tracker);

        return new ResponseEntity<>(
                created,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefillTracker> read(
            @PathVariable String id, Authentication authentication) {

        RefillTracker tracker =
                service.read(id);

        if (tracker == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canAccessPrescription(authentication, tracker.getPrescriptionId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(tracker);
    }

    @GetMapping
    public ResponseEntity<List<RefillTracker>> getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }

    @GetMapping("/prescription/{prescriptionId}")
    public ResponseEntity<RefillTracker>
    getByPrescriptionId(
            @PathVariable String prescriptionId, Authentication authentication) {
        if (!canAccessPrescription(authentication, prescriptionId)) {
            return ResponseEntity.status(403).build();
        }

        RefillTracker tracker =
                service.getByPrescriptionId(
                        prescriptionId
                );

        if (tracker == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tracker);
    }

    @PutMapping
    public ResponseEntity<RefillTracker> update(
            @RequestBody RefillTracker tracker) {

        RefillTracker updated = service.update(tracker);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/eligible/{prescriptionId}")
    public ResponseEntity<Boolean> checkEligibility(
            @PathVariable String prescriptionId, Authentication authentication) {
        if (!canAccessPrescription(authentication, prescriptionId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                service.isRefillEligible(
                        prescriptionId
                )
        );
    }

    @PostMapping("/process/{prescriptionId}")
    public ResponseEntity<RefillTracker> processRefill(
            @PathVariable String prescriptionId, Authentication authentication) {
        if (!canAccessPrescription(authentication, prescriptionId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                service.processRefill(
                        prescriptionId
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private boolean canAccessPrescription(Authentication authentication, String prescriptionId) {
        User user = authentication != null && authentication.getPrincipal() instanceof User currentUser
                ? currentUser : null;
        if (user == null) {
            return false;
        }
        if (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.PHARMACIST) {
            return true;
        }
        Prescription prescription = prescriptionService.read(prescriptionId);
        return user.getRole() == User.UserRole.CUSTOMER && prescription != null
                && String.valueOf(user.getId()).equals(prescription.getPatientId());
    }
}