package za.ac.cput.digitalpharmacysystem.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import za.ac.cput.digitalpharmacysystem.domain.Delivery;
import za.ac.cput.digitalpharmacysystem.domain.DeliveryStatus;
import za.ac.cput.digitalpharmacysystem.factory.DeliveryFactory;


import static org.junit.jupiter.api.Assertions.*;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class DeliveryControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseURL = "/digitalpharmacysystem/delivery";
    private static Delivery delivery;

    @BeforeAll
    static void setUp() {
        delivery = DeliveryFactory.createDelivery(
                "DEL-300",
                "TRK-88012",
                "12 Main Road, Cape Town",
                DeliveryStatus.PENDING
        );
    }

    @Test
    @Order(1)
    void a_create() {
        String url = baseURL + "/create";
        ResponseEntity<Delivery> postResponse = restTemplate.postForEntity(url, delivery, Delivery.class);

        assertNotNull(postResponse);
        assertNotNull(postResponse.getBody());
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertEquals(delivery.getId(), postResponse.getBody().getId());
    }

    @Test
    @Order(2)
    void b_read() {
        String url = baseURL + "/read/" + delivery.getId();
        ResponseEntity<Delivery> response = restTemplate.getForEntity(url, Delivery.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(delivery.getId(), response.getBody().getId());
    }

    @Test
    @Order(3)
    void c_update() {

        Delivery updatedDelivery = DeliveryFactory.createDelivery(
                delivery.getId(),
                "TRK-88012",
                "12 Main Road, Cape Town",
                DeliveryStatus.DISPATCHED
        );

        String url = baseURL + "/update";
        ResponseEntity<Delivery> response = restTemplate.postForEntity(url, updatedDelivery, Delivery.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(DeliveryStatus.DISPATCHED, response.getBody().getStatus());
    }

    @Test
    @Order(4)
    void d_getAll() {
        String url = baseURL + "/getall";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Delivery[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Delivery[].class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    @Order(5)
    void e_delete() {
        String url = baseURL + "/delete/" + delivery.getId();
        restTemplate.delete(url);

        String readUrl = baseURL + "/read/" + delivery.getId();
        ResponseEntity<Delivery> response = restTemplate.getForEntity(readUrl, Delivery.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
