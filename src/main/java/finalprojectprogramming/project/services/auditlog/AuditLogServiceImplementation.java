package finalprojectprogramming.project.services.auditlog;

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
            auditLog.setUser(getUser(auditLogDTO.getUserId()));
        }
        auditLog.setAction(auditLogDTO.getAction());
        auditLog.setEntityId(auditLogDTO.getEntityId());
        auditLog.setDetails(auditLogDTO.getDetails());
        auditLog.setTimestamp(auditLogDTO.getTimestamp() != null ? auditLogDTO.getTimestamp() : LocalDateTime.now());

        AuditLog saved = auditLogRepository.save(auditLog);
        return toDto(saved);
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