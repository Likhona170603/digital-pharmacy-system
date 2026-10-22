package za.ac.cput.digitalpharmacysystem.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.digitalpharmacysystem.domain.AuditLog;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
/*
 * Sesona Ntshatsha
 * 240773365

 */

public class AuditLogFactoryTest {
    @Test
    void testCreateAuditLogSuccess() {
        LocalDateTime now = LocalDateTime.now();
        AuditLog auditLog = AuditLogFactory.createAuditLog(
                "AL-1001",
                "CREATE_PRESCRIPTION",
                "PrescriptionEntity",
                now
        );

        assertNotNull(auditLog);
        assertEquals("AL-1001", auditLog.getId());
        assertEquals("CREATE_PRESCRIPTION", auditLog.getAction());
        assertEquals("PrescriptionEntity", auditLog.getEntityTarget());
        assertEquals(now, auditLog.getTimeStamp());
        System.out.println("Success Test Passed: " + auditLog);
    }

    @Test
    void testCreateAuditLogWithNullAction() {
        AuditLog auditLog = AuditLogFactory.createAuditLog(
                "AL-1001",
                null,
                "PrescriptionEntity",
                LocalDateTime.now()
        );

        assertNull(auditLog);
    }

    @Test
    void testCreateAuditLogWithNullTimestamp() {
        AuditLog auditLog = AuditLogFactory.createAuditLog(
                "AL-1001",
                "DELETE_USER",
                "UserEntity",
                null
        );

        assertNull(auditLog);
    }
}

