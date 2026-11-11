package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.domain.VerificationStatus;
import za.ac.cput.digitalpharmacysystem.service.IPrescriptionService;

import java.util.List;

@RestController
@RequestMapping({"/digitalpharmacysystem/prescription", "/api/prescriptions"})
public class PrescriptionController {

    private final IPrescriptionService service;

    public PrescriptionController(
            IPrescriptionService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Prescription> create(
            @RequestBody Prescription prescription,
            Authentication authentication) {

        User user = currentUser(authentication);
        if (user == null || user.getRole() != User.UserRole.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Prescription ownedPrescription = copyForPatient(prescription, String.valueOf(user.getId()));
        Prescription created = service.create(ownedPrescription);

        return new ResponseEntity<>(
                created,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> read(
            @PathVariable String id, Authentication authentication) {

        Prescription prescription =
                service.read(id);

        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canReviewAll(authentication) && !owns(authentication, prescription)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(prescription);
    }

    @GetMapping
    public ResponseEntity<List<Prescription>> getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Prescription>> getByPatientId(
            @PathVariable String patientId, Authentication authentication) {
        if (!canReviewAll(authentication) && !ownsPatient(authentication, patientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                service.getByPatientId(patientId)
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Prescription>>
    getPendingPrescriptions() {

        return ResponseEntity.ok(
                service.getPendingPrescriptions()
        );
    }

    @PutMapping
    public ResponseEntity<Prescription> update(
            @RequestBody Prescription prescription) {

        Prescription updated = service.update(prescription);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Prescription> approve(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int refills,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.approvePrescription(
                        id,
                        refills,
                        ((User) authentication.getPrincipal()).getFullName()
                )
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Prescription> reject(
            @PathVariable String id,
            @RequestParam String reason,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.rejectPrescription(
                        id,
                        reason,
                        ((User) authentication.getPrincipal()).getFullName()
                )
        );
    }

    @PutMapping("/{id}/clarify")
    public ResponseEntity<Prescription> clarify(
            @PathVariable String id,
            @RequestParam String reason,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.requestClarification(
                        id,
                        reason,
                        ((User) authentication.getPrincipal()).getFullName()
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

    private User currentUser(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof User user ? user : null;
    }

    private boolean canReviewAll(Authentication authentication) {
        User user = currentUser(authentication);
        return user != null && (user.getRole() == User.UserRole.ADMIN
                || user.getRole() == User.UserRole.PHARMACIST);
    }

    private boolean owns(Authentication authentication, Prescription prescription) {
        return ownsPatient(authentication, prescription.getPatientId());
    }

    private boolean ownsPatient(Authentication authentication, String patientId) {
        User user = currentUser(authentication);
        return user != null && user.getRole() == User.UserRole.CUSTOMER
                && String.valueOf(user.getId()).equals(patientId);
    }

    private Prescription copyForPatient(Prescription prescription, String patientId) {
        return new Prescription.Builder()
                .setPrescriptionId(prescription.getPrescriptionId())
                .setPatientId(patientId)
                .setFilePath(prescription.getFilePath())
                .setIssuedDate(prescription.getIssuedDate())
                .setVerificationStatus(VerificationStatus.PENDING)
                .setRejectionReason(null)
                .setProductId(prescription.getProductId())
                .setVerifiedBy(null)
                .setVerifiedAt(null)
                .build();
    }
}