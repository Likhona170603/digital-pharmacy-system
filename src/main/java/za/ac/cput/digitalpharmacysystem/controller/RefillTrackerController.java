package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;
import za.ac.cput.digitalpharmacysystem.service.IRefillTrackerService;

import java.util.List;

@RestController
@RequestMapping("/api/refills")
public class RefillTrackerController {

    private final IRefillTrackerService service;

    public RefillTrackerController(
            IRefillTrackerService service) {

        this.service = service;
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
            @PathVariable String id) {

        RefillTracker tracker =
                service.read(id);

        if (tracker == null) {
            return ResponseEntity.notFound().build();
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
            @PathVariable String prescriptionId) {

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

        return ResponseEntity.ok(
                service.update(tracker)
        );
    }

    @GetMapping("/eligible/{prescriptionId}")
    public ResponseEntity<Boolean> checkEligibility(
            @PathVariable String prescriptionId) {

        return ResponseEntity.ok(
                service.isRefillEligible(
                        prescriptionId
                )
        );
    }

    @PostMapping("/process/{prescriptionId}")
    public ResponseEntity<RefillTracker> processRefill(
            @PathVariable String prescriptionId) {

        return ResponseEntity.ok(
                service.processRefill(
                        prescriptionId
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