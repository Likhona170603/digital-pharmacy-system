package za.ac.cput.digitalpharmacysystem.controller;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import za.ac.cput.digitalpharmacysystem.domain.AuditLog;
import za.ac.cput.digitalpharmacysystem.factory.AuditLogFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuditLogControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseURL = "/digitalpharmacysystem/auditlog";
    private static AuditLog auditLog;

    @BeforeAll
    static void setUp() {
        auditLog = AuditLogFactory.createAuditLog(
                "LOG-200",
                "LOGIN_SUCCESS",
                "User ID #104",
                LocalDateTime.now()
        );
    }

    @Test
    @Order(1)
    void a_create() {
        String url = baseURL + "/create";
        ResponseEntity<AuditLog> postResponse = restTemplate.postForEntity(url, auditLog, AuditLog.class);

        assertNotNull(postResponse);
        assertNotNull(postResponse.getBody());
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertEquals(auditLog.getId(), postResponse.getBody().getId());
    }

    @Test
    @Order(2)
    void b_read() {
        String url = baseURL + "/read/" + auditLog.getId();
        ResponseEntity<AuditLog> response = restTemplate.getForEntity(url, AuditLog.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(auditLog.getId(), response.getBody().getId());
    }

    @Test
    @Order(3)
    void c_update() {

        AuditLog updatedLog = AuditLogFactory.createAuditLog(
                auditLog.getId(),
                "LOGOUT_SUCCESS",
                "User ID #104 logged out",
                auditLog.getTimeStamp()
        );

        String url = baseURL + "/update";
        ResponseEntity<AuditLog> response = restTemplate.postForEntity(url, updatedLog, AuditLog.class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LOGOUT_SUCCESS", response.getBody().getAction());
    }

    @Test
    @Order(4)
    void d_getAll() {
        String url = baseURL + "/getall";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<AuditLog[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, AuditLog[].class);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    @Order(5)
    void e_delete() {
        String url = baseURL + "/delete/" + auditLog.getId();
        restTemplate.delete(url);

        String readUrl = baseURL + "/read/" + auditLog.getId();
        ResponseEntity<AuditLog> response = restTemplate.getForEntity(readUrl, AuditLog.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
