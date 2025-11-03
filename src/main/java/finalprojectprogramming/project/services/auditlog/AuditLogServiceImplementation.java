package finalprojectprogramming.project.services.auditlog;

import com.fasterxml.jackson.databind.JsonNode;
import finalprojectprogramming.project.dtos.AuditLogDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.AuditLog;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.repositories.AuditLogRepository;
import finalprojectprogramming.project.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditLogServiceImplementation implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public AuditLogServiceImplementation(AuditLogRepository auditLogRepository, UserRepository userRepository,
            ModelMapper modelMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public AuditLogDTO create(AuditLogDTO auditLogDTO) {
        AuditLog auditLog = new AuditLog();
        if (auditLogDTO.getUserId() != null) {
            try {
                auditLog.setUser(getUser(auditLogDTO.getUserId()));
            } catch (Exception e) {
                // Usuario no encontrado - continuar sin usuario
                auditLog.setUser(null);
            }
        }
        auditLog.setAction(auditLogDTO.getAction());
        auditLog.setEntityId(auditLogDTO.getEntityId());
        auditLog.setDetails(auditLogDTO.getDetails());
        auditLog.setTimestamp(auditLogDTO.getTimestamp() != null ? auditLogDTO.getTimestamp() : LocalDateTime.now());

        AuditLog saved = auditLogRepository.save(auditLog);
        return toDto(saved);
    }

    @Override
    public void logEvent(Long userId, String action, String entityId, JsonNode details) {
        try {
            System.out.println("✅ Guardando audit log: action=" + action + ", userId=" + userId + ", entityId=" + entityId);
            AuditLogDTO dto = AuditLogDTO.builder()
                    .userId(userId)
                    .action(action)
                    .entityId(entityId)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();
            AuditLogDTO saved = create(dto);
            System.out.println("✅ Audit log guardado exitosamente con ID: " + saved.getId());
        } catch (Exception e) {
            // ❌ Error al guardar audit log - no romper la transacción principal
            System.err.println("❌ Error al guardar audit log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogDTO findById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log with id " + id + " not found"));
        return toDto(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> findAll() {
        return auditLogRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> findByUser(Long userId) {
        return auditLogRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log with id " + id + " not found"));
        auditLogRepository.delete(auditLog);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
    }

    private AuditLogDTO toDto(AuditLog auditLog) {
        AuditLogDTO dto = modelMapper.map(auditLog, AuditLogDTO.class);
        dto.setUserId(auditLog.getUser() != null ? auditLog.getUser().getId() : null);
        return dto;
    }
}