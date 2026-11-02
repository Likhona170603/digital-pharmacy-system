package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.Product;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.factory.PrescriptionFactory;
import za.ac.cput.digitalpharmacysystem.service.IPrescriptionService;
import za.ac.cput.digitalpharmacysystem.service.IProductService;
import za.ac.cput.digitalpharmacysystem.util.PrescriptionFileStorage;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Customer prescription upload, plus secure access to the uploaded file.
 * Kept separate from PrescriptionController so the existing endpoints are untouched.
 */
@RestController
@RequestMapping({"/digitalpharmacysystem/prescription", "/api/prescriptions"})
public class PrescriptionUploadController {

    private final IPrescriptionService prescriptionService;
    private final IProductService productService;
    private final PrescriptionFileStorage storage;

    public PrescriptionUploadController(IPrescriptionService prescriptionService,
                                        IProductService productService,
                                        PrescriptionFileStorage storage) {
        this.prescriptionService = prescriptionService;
        this.productService = productService;
        this.storage = storage;
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Prescription> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") String productId,
            @RequestParam(value = "issuedDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issuedDate) {

        User user = currentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Product product = productService.read(productId);
        if (product == null) {
            throw new IllegalArgumentException("Unknown product");
        }
        if (!product.isRxRequired()) {
            throw new IllegalArgumentException(product.getProductName() + " does not need a prescription");
        }

        LocalDate issued = issuedDate != null ? issuedDate : LocalDate.now();
        if (issued.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The prescription date cannot be in the future");
        }

        String storedName = storage.store(file);
        Prescription prescription = PrescriptionFactory.createPrescription(
                String.valueOf(user.getId()), productId, storedName, issued);

        return new ResponseEntity<>(prescriptionService.create(prescription), HttpStatus.CREATED);
    }

    /**
     * Returns the uploaded file. Only the pharmacy staff or the patient who uploaded it may view it.
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable String id) {
        User user = currentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Prescription prescription = prescriptionService.read(id);
        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }

        String role = user.getRole().name();
        boolean staff = "PHARMACIST".equals(role) || "ADMIN".equals(role);
        boolean owner = String.valueOf(user.getId()).equals(prescription.getPatientId());
        if (!staff && !owner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Path path = storage.find(prescription.getFilePath());
        if (path == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storage.contentTypeOf(prescription.getFilePath())))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(new PathResource(path));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMyPrescriptions() {
        User user = currentUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                prescriptionService.getByPatientId(String.valueOf(user.getId()))
        );
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof User user ? user : null;
    }
}
