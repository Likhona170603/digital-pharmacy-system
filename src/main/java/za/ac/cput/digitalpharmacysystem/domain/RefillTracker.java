package za.ac.cput.digitalpharmacysystem.domain;

/*
 * Likhona Noroyita
 * 230154239
 */

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@Entity
public class RefillTracker {

    @Id
    private String refillTrackerId;

    private String prescriptionId;

    private int totalRefillsAllowed;

    private int refillsUsed;

    private LocalDate nextEligibleDate;

    protected RefillTracker() {
        // Required by JPA
    }

    private RefillTracker(Builder builder) {
        this.refillTrackerId = builder.refillTrackerId;
        this.prescriptionId = builder.prescriptionId;
        this.totalRefillsAllowed = builder.totalRefillsAllowed;
        this.refillsUsed = builder.refillsUsed;
        this.nextEligibleDate = builder.nextEligibleDate;
    }

    public String getRefillTrackerId() {
        return refillTrackerId;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public int getTotalRefillsAllowed() {
        return totalRefillsAllowed;
    }

    public int getRefillsUsed() {
        return refillsUsed;
    }

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    /** Worked out from the other fields, not stored. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public RefillStatus getRefillStatus() {
        if (refillsUsed >= totalRefillsAllowed) {
            return RefillStatus.COMPLETED;
        }
        if (nextEligibleDate != null && nextEligibleDate.isAfter(LocalDate.now())) {
            return RefillStatus.NOT_ELIGIBLE;
        }
        return RefillStatus.AVAILABLE;
    }

    public void setRefillsUsed(int refillsUsed) {
        this.refillsUsed = refillsUsed;
    }

    public void setNextEligibleDate(LocalDate nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }

    public static class Builder {

        private String refillTrackerId;
        private String prescriptionId;
        private int totalRefillsAllowed;
        private int refillsUsed;
        private LocalDate nextEligibleDate;

        public Builder setRefillTrackerId(String refillTrackerId) {
            this.refillTrackerId = refillTrackerId;
            return this;
        }

        public Builder setPrescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public Builder setTotalRefillsAllowed(int totalRefillsAllowed) {
            this.totalRefillsAllowed = totalRefillsAllowed;
            return this;
        }

        public Builder setRefillsUsed(int refillsUsed) {
            this.refillsUsed = refillsUsed;
            return this;
        }

        public Builder setNextEligibleDate(LocalDate nextEligibleDate) {
            this.nextEligibleDate = nextEligibleDate;
            return this;
        }

        public RefillTracker build() {
            return new RefillTracker(this);
        }
    }
}