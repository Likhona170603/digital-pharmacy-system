package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.ac.cput.digitalpharmacysystem.domain.AuditLog;
import za.ac.cput.digitalpharmacysystem.repository.AuditLogRepository;

import za.ac.cput.digitalpharmacysystem.service.IAuditLogService;
import za.ac.cput.digitalpharmacysystem.util.ActorContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@Service
public class AuditLogService implements IAuditLogService {

    private final AuditLogRepository repository;
    private final ActorContext actorContext;

    @Autowired
    public AuditLogService(AuditLogRepository repository, ActorContext actorContext) {
        this.repository = repository;
        this.actorContext = actorContext;
    }

    @Override
    public AuditLog record(String action, String entityTarget, String entityId) {
        AuditLog entry = new AuditLog.Builder()
                .setId(UUID.randomUUID().toString())
                .setAction(action)
                .setEntityTarget(entityTarget)
                .setEntityId(entityId)
                .setActorId(actorContext.getActorId())
                .setActorRole(actorContext.getActorRole())
                .setTimeStamp(LocalDateTime.now())
                .build();
        return this.repository.save(entry);
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



