package za.ac.cput.digitalpharmacysystem.service;

import za.ac.cput.digitalpharmacysystem.domain.Delivery;
import za.ac.cput.digitalpharmacysystem.domain.DeliveryStatus;

import java.util.List;

/*
 * Sesona Ntshatsha
 * 240773365

 */

public interface IDeliveryService extends IService<Delivery, String> {
    Delivery findByOrderNumber(String orderNumber);
    List<Delivery> getCourierManifest(Long courierId);
    List<Delivery> getDeliveriesForCurrentUser();
    /**
     * Moves a delivery forward: PACKED -> DISPATCHED -> (IN_TRANSIT) -> DELIVERED.
     * Dispatching needs an assigned courier; DELIVERED needs the recipient's name.
     * The order's status follows (SHIPPED, DELIVERED) and the change is audited.
     */
    Delivery updateStatus(String id, DeliveryStatus newStatus, String recipientName);

    Delivery assignCourier(String id, Long courierId);
}
