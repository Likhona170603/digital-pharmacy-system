package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.cput.digitalpharmacysystem.domain.AuditLog;
import za.ac.cput.digitalpharmacysystem.repository.AuditLogRepository;

import java.util.List;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@Service
public class AuditLogService implements IAuditLogService {

    private final AuditLogRepository repository;

    @Autowired
    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }
    @Override
    public AuditLog create(AuditLog auditLog) {
        return this.repository.save(auditLog);
    }

    @Override
    public AuditLog read(String id) {
        return this.repository.findById(id).orElse(null);
    }

    @Override
    public AuditLog update(AuditLog auditLog) {
        if (this.repository.existsById(auditLog.getId())) {
            return this.repository.save(auditLog);
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        if (this.repository.existsById(id)) {
            this.repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<AuditLog> getAll() {
        return this.repository.findAll();
    }
}



