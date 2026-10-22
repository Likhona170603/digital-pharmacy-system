package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.service.IPrescriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final IPrescriptionService service;

    public PrescriptionController(
            IPrescriptionService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Prescription> create(
            @RequestBody Prescription prescription) {

        Prescription created =
                service.create(prescription);

        return new ResponseEntity<>(
                created,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> read(
            @PathVariable String id) {

        Prescription prescription =
                service.read(id);

        if (prescription == null) {
            return ResponseEntity.notFound().build();
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
            @PathVariable String patientId) {

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

        return ResponseEntity.ok(
                service.update(prescription)
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Prescription> approve(
            @PathVariable String id) {

        return ResponseEntity.ok(
                service.approvePrescription(id)
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Prescription> reject(
            @PathVariable String id,
            @RequestParam String reason) {

        return ResponseEntity.ok(
                service.rejectPrescription(
                        id,
                        reason
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}