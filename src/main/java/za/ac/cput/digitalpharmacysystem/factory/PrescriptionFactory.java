package za.ac.cput.digitalpharmacysystem.factory;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.VerificationStatus;

import java.time.LocalDate;
import java.util.UUID;

public class PrescriptionFactory {

    public static Prescription createPrescription(
            String patientId,
            String filePath,
            LocalDate issuedDate) {

        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Patient ID is required"
            );
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "File path is required"
            );
        }

        if (issuedDate == null) {
            throw new IllegalArgumentException(
                    "Issued date is required"
            );
        }

        return new Prescription.Builder()
                .setPrescriptionId(UUID.randomUUID().toString())
                .setPatientId(patientId)
                .setFilePath(filePath)
                .setIssuedDate(issuedDate)
                .setVerificationStatus(
                        VerificationStatus.PENDING
                )
                .setRejectionReason(null)
                .build();
    }
}