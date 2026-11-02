package za.ac.cput.digitalpharmacysystem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Prescription {

    @Id
    private String prescriptionId;

    private String patientId;

    private String filePath;

    private LocalDate issuedDate;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    private String rejectionReason;

    // The medicine this prescription covers (matched against the product at checkout).
    private String productId;

    private String verifiedBy;

    private LocalDateTime verifiedAt;

    protected Prescription() {
        // Required by JPA
    }

    private Prescription(Builder builder) {
        this.prescriptionId = builder.prescriptionId;
        this.patientId = builder.patientId;
        this.filePath = builder.filePath;
        this.issuedDate = builder.issuedDate;
        this.verificationStatus = builder.verificationStatus;
        this.rejectionReason = builder.rejectionReason;
        this.productId = builder.productId;
        this.verifiedBy = builder.verifiedBy;
        this.verifiedAt = builder.verifiedAt;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getProductId() {
        return productId;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public static class Builder {

        private String prescriptionId;
        private String patientId;
        private String filePath;
        private LocalDate issuedDate;
        private VerificationStatus verificationStatus;
        private String rejectionReason;
        private String productId;
        private String verifiedBy;
        private LocalDateTime verifiedAt;

        public Builder setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public Builder setPatientId(String patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder setIssuedDate(LocalDate issuedDate) {
            this.issuedDate = issuedDate;
            return this;
        }

        public Builder setVerificationStatus(
                VerificationStatus verificationStatus) {

            this.verificationStatus = verificationStatus;
            return this;
        }

        public Builder setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public Builder setProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder setVerifiedBy(String verifiedBy) {
            this.verifiedBy = verifiedBy;
            return this;
        }

        public Builder setVerifiedAt(LocalDateTime verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Prescription build() {
            return new Prescription(this);
        }
    }
}