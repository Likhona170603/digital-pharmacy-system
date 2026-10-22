package za.ac.cput.digitalpharmacysystem.factory;

import za.ac.cput.digitalpharmacysystem.domain.Delivery;
import za.ac.cput.digitalpharmacysystem.domain.DeliveryStatus;
import za.ac.cput.digitalpharmacysystem.util.Helper;
/*
 * Sesona Ntshatsha
 * 240773365

 */

public class DeliveryFactory {

    public static Delivery createDelivery(String id, String trackingNo, String address, DeliveryStatus status) {
        if (Helper.isNullOrEmpty(id) || Helper.isNullOrEmpty(trackingNo) || Helper.isNullOrEmpty(address) || status == null) {
            return null;
        }
        return new Delivery.Builder()
                .setId(id)
                .setTrackingNo(trackingNo)
                .setAddress(address)
                .setStatus(status)
                .build();


    }
}
