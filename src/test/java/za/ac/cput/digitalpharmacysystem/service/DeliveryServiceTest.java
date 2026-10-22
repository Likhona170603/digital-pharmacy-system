package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import za.ac.cput.digitalpharmacysystem.domain.Delivery;
import za.ac.cput.digitalpharmacysystem.domain.DeliveryStatus;
import za.ac.cput.digitalpharmacysystem.factory.DeliveryFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeliveryServiceTest {

    @Autowired
    private IDeliveryService service;

    private static Delivery delivery = DeliveryFactory.createDelivery(
            "DEL-101",
            "TRK-00192",
            "45 Long Street, Cape Town",
            DeliveryStatus.PENDING
    );

    @Test
    @Order(1)
    void create() {
        Delivery created = service.create(delivery);
        assertNotNull(created);
        assertEquals(delivery.getId(), created.getId());
    }

    @Test
    @Order(2)
    void read() {
        Delivery read = service.read(delivery.getId());
        assertNotNull(read);
        assertEquals(delivery.getId(), read.getId());
    }

    @Test
    @Order(3)
    void update() {
        Delivery updatedDelivery = DeliveryFactory.createDelivery(
                delivery.getId(),
                "TRK-00192",
                "45 Long Street, Cape Town",
                DeliveryStatus.DISPATCHED
        );

        Delivery updated = service.update(updatedDelivery);

        assertNotNull(updated);
        assertEquals(DeliveryStatus.DISPATCHED, updated.getStatus());
    }

    @Test
    @Order(4)
    void getAll() {
        List<Delivery> allDeliveries = service.getAll();
        assertNotNull(allDeliveries);
        assertFalse(allDeliveries.isEmpty());
    }

    @Test
    @Order(5)
    void delete() {
        boolean success = service.delete(delivery.getId());
        assertTrue(success);

        Delivery read = service.read(delivery.getId());
        assertNull(read);
    }
}
