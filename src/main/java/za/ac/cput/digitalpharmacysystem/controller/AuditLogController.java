package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.digitalpharmacysystem.domain.AuditLog;
import za.ac.cput.digitalpharmacysystem.service.IAuditLogService;

import java.util.List;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@RestController
@RequestMapping("/digitalpharmacysystem/auditlog")
public class AuditLogController {
    private final IAuditLogService service;

    @Autowired
    public AuditLogController(IAuditLogService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<AuditLog> create(@RequestBody AuditLog auditLog) {
        AuditLog created = service.create(auditLog);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<AuditLog> read(@PathVariable String id) {
        AuditLog auditLog = service.read(id);
        if (auditLog == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(auditLog, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<AuditLog> update(@RequestBody AuditLog auditLog) {
        AuditLog updated = service.update(auditLog);
        if (updated == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<AuditLog>> getAll() {
        List<AuditLog> logs = service.getAll();
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }
}
