package finalprojectprogramming.project.services.auditlog;

import com.fasterxml.jackson.databind.JsonNode;
import finalprojectprogramming.project.dtos.AuditLogDTO;
import java.util.List;

public interface AuditLogService {

    AuditLogDTO create(AuditLogDTO auditLogDTO);

    void logEvent(Long userId, String action, String entityId, JsonNode details);

    AuditLogDTO findById(Long id);

    List<AuditLogDTO> findAll();

    List<AuditLogDTO> findByUser(Long userId);

    void delete(Long id);
}