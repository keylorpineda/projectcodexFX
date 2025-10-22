package finalprojectprogramming.project.services.auditlog;

import finalprojectprogramming.project.dtos.AuditLogDTO;
import java.util.List;

public interface AuditLogService {

    AuditLogDTO create(AuditLogDTO auditLogDTO);

    AuditLogDTO findById(Long id);

    List<AuditLogDTO> findAll();

    List<AuditLogDTO> findByUser(Long userId);

    void delete(Long id);
}