package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import za.ac.cput.digitalpharmacysystem.domain.AuditLog;
import za.ac.cput.digitalpharmacysystem.factory.AuditLogFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuditLogServiceTest {
    @Autowired
    private IAuditLogService service;

    private static AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = AuditLogFactory.createAuditLog(
                "LOG-101",
                "CREATE_ORDER",
                "Order #5021",
                LocalDateTime.now()
        );
    }

    @Test
    @Order(1)
    void create() {
        AuditLog created = service.create(auditLog);
        assertNotNull(created);
        assertEquals(auditLog.getId(), created.getId());
    }

    @Test
    @Order(2)
    void read() {
        AuditLog read = service.read("LOG-101");
        assertNotNull(read);
        assertEquals("LOG-101", read.getId());
    }

    @Test
    @Order(3)
    void update() {

        AuditLog updatedLog = AuditLogFactory.createAuditLog(
                auditLog.getId(),
                "UPDATE_ORDER",
                "Order #5021 - Status set to COMPLETED",
                auditLog.getTimeStamp()
        );

        AuditLog updated = service.update(updatedLog);
        assertNotNull(updated);
        assertEquals("UPDATE_ORDER", updated.getAction());
        assertEquals("Order #5021 - Status set to COMPLETED", updated.getEntityTarget());
    }

    @Test
    @Order(4)
    void getAll() {
        List<AuditLog> allLogs = service.getAll();
        assertNotNull(allLogs);
        assertFalse(allLogs.isEmpty());
    }

    @Test
    @Order(5)
    void delete() {
        boolean success = service.delete("LOG-101");
        assertTrue(success);

        AuditLog read = service.read("LOG-101");
        assertNull(read);
    }
}