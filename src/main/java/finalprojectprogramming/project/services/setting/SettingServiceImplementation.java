package finalprojectprogramming.project.services.setting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.dtos.SettingDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Setting;
import finalprojectprogramming.project.repositories.SettingRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SettingServiceImplementation implements SettingService {

    private final SettingRepository settingRepository;
    private final ModelMapper modelMapper;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public SettingServiceImplementation(SettingRepository settingRepository, ModelMapper modelMapper,
            AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.settingRepository = settingRepository;
        this.modelMapper = modelMapper;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public SettingDTO create(SettingDTO settingDTO) {
        validateUniqueKey(settingDTO.getKey(), null);

        Setting setting = new Setting();
        setting.setKey(settingDTO.getKey());
        setting.setValue(settingDTO.getValue());
        setting.setDescription(settingDTO.getDescription());
        setting.setUpdatedAt(LocalDateTime.now());

        Setting saved = settingRepository.save(setting);
        
        // Auditoría: Configuración creada
        recordAudit("SETTING_CREATED", saved, details -> {
            details.put("key", saved.getKey());
            details.put("value", saved.getValue());
        });
        
        return toDto(saved);
    }

    @Override
    public SettingDTO update(Long id, SettingDTO settingDTO) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with id " + id + " not found"));

        if (settingDTO.getKey() != null && !Objects.equals(settingDTO.getKey(), setting.getKey())) {
            validateUniqueKey(settingDTO.getKey(), id);
            setting.setKey(settingDTO.getKey());
        }
        if (settingDTO.getValue() != null) {
            setting.setValue(settingDTO.getValue());
        }
        if (settingDTO.getDescription() != null) {
            setting.setDescription(settingDTO.getDescription());
        }
        setting.setUpdatedAt(LocalDateTime.now());

        Setting saved = settingRepository.save(setting);
        
        // Auditoría: Configuración actualizada
        recordAudit("SETTING_UPDATED", saved, details -> {
            if (settingDTO.getKey() != null) {
                details.put("keyChanged", true);
                details.put("key", saved.getKey());
            }
            if (settingDTO.getValue() != null) {
                details.put("valueChanged", true);
                details.put("newValue", saved.getValue());
            }
        });
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SettingDTO findById(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with id " + id + " not found"));
        return toDto(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettingDTO> findAll() {
        return settingRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SettingDTO findByKey(String key) {
        Setting setting = settingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with key " + key + " not found"));
        return toDto(setting);
    }

    @Override
    public void delete(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting with id " + id + " not found"));
        
        // Auditoría: Configuración eliminada
        recordAudit("SETTING_DELETED", setting, details -> {
            details.put("key", setting.getKey());
            details.put("value", setting.getValue());
        });
        
        settingRepository.delete(setting);
    }

    private void validateUniqueKey(String key, Long currentId) {
        if (key == null || key.isBlank()) {
            throw new BusinessRuleException("Setting key cannot be blank");
        }
        settingRepository.findByKey(key)
                .filter(existing -> !Objects.equals(existing.getId(), currentId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("A setting with the provided key already exists");
                });
    }

    private SettingDTO toDto(Setting setting) {
        return modelMapper.map(setting, SettingDTO.class);
    }
    
    /**
     * Registra un evento de auditoría para acciones de configuración
     */
    private void recordAudit(String action, Setting setting, Consumer<ObjectNode> detailsCustomizer) {
        Long actorId = null;
        try {
            actorId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
            actorId = null;
        }
        
        ObjectNode details = objectMapper.createObjectNode();
        details.put("settingId", setting.getId());
        if (setting.getDescription() != null) {
            details.put("description", setting.getDescription());
        }
        
        if (detailsCustomizer != null) {
            detailsCustomizer.accept(details);
        }
        
        String entityId = setting.getId() != null ? setting.getId().toString() : null;
        auditLogService.logEvent(actorId, action, entityId, details);
    }
}