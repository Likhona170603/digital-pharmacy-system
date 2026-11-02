package za.ac.cput.digitalpharmacysystem.domain;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,

    // Appended last on purpose: Order stores this enum by position (ordinal),
    // so existing values must keep their place. Add new values below, never above.
    PACKED,
    READY_FOR_COLLECTION,
    COLLECTED
}

