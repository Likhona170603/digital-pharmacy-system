package za.ac.cput.digitalpharmacysystem.service;

import za.ac.cput.digitalpharmacysystem.domain.AuditLog;
/*
 * Sesona Ntshatsha
 * 240773365

 */

public interface IAuditLogService extends IService<AuditLog, String> {

    /** Writes an audit entry stamped with the current time and the acting user (if known). */
    AuditLog record(String action, String entityTarget, String entityId);
}
