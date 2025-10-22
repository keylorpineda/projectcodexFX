package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.AuditLogDTO;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@Validated
@Tag(name = "Audit Logs", description = "Operations related to audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    @Operation(summary = "Create a new audit log entry")
    public ResponseEntity<AuditLogDTO> createAuditLog(@Valid @RequestBody AuditLogDTO auditLogDTO) {
        AuditLogDTO created = auditLogService.create(auditLogDTO);
        return ResponseEntity.created(URI.create("/api/audit-logs/" + created.getId())).body(created);
    }

    @GetMapping
    @Operation(summary = "Retrieve all audit logs")
    public ResponseEntity<List<AuditLogDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve an audit log by id")
    public ResponseEntity<AuditLogDTO> getAuditLogById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.findById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Retrieve audit logs for a user")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.findByUser(userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an audit log entry")
    public ResponseEntity<Void> deleteAuditLog(@PathVariable Long id) {
        auditLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}