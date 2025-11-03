package finalprojectprogramming.project.services.spaceschedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.dtos.SpaceScheduleDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceSchedule;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.repositories.SpaceScheduleRepository;
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
public class SpaceScheduleServiceImplementation implements SpaceScheduleService {

    private final SpaceScheduleRepository spaceScheduleRepository;
    private final SpaceRepository spaceRepository;
    private final ModelMapper modelMapper;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public SpaceScheduleServiceImplementation(SpaceScheduleRepository spaceScheduleRepository,
            SpaceRepository spaceRepository, ModelMapper modelMapper, AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.spaceScheduleRepository = spaceScheduleRepository;
        this.spaceRepository = spaceRepository;
        this.modelMapper = modelMapper;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public SpaceScheduleDTO create(SpaceScheduleDTO spaceScheduleDTO) {
        Space space = getActiveSpace(spaceScheduleDTO.getSpaceId());
        validateTimeRange(spaceScheduleDTO.getOpenTime(), spaceScheduleDTO.getCloseTime());
        assertUniqueDay(spaceScheduleDTO.getSpaceId(), spaceScheduleDTO.getDayOfWeek(), null);

        SpaceSchedule schedule = new SpaceSchedule();
        schedule.setSpace(space);
        schedule.setDayOfWeek(spaceScheduleDTO.getDayOfWeek());
        schedule.setOpenTime(spaceScheduleDTO.getOpenTime());
        schedule.setCloseTime(spaceScheduleDTO.getCloseTime());
        schedule.setHolidayOverride(Boolean.TRUE.equals(spaceScheduleDTO.getHolidayOverride()));
        schedule.setMaintenanceNotes(spaceScheduleDTO.getMaintenanceNotes());
        LocalDateTime now = LocalDateTime.now();
        schedule.setCreatedAt(now);
        schedule.setUpdatedAt(now);

        SpaceSchedule saved = spaceScheduleRepository.save(schedule);
        
        // Auditoría: Horario de espacio creado
        recordAudit("SPACE_SCHEDULE_CREATED", saved, details -> {
            details.put("dayOfWeek", saved.getDayOfWeek().name());
            details.put("openTime", saved.getOpenTime().toString());
            details.put("closeTime", saved.getCloseTime().toString());
        });
        
        return toDto(saved);
    }

    @Override
    public SpaceScheduleDTO update(Long id, SpaceScheduleDTO spaceScheduleDTO) {
        SpaceSchedule schedule = spaceScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space schedule with id " + id + " not found"));

        if (spaceScheduleDTO.getSpaceId() != null
                && !Objects.equals(spaceScheduleDTO.getSpaceId(), schedule.getSpace().getId())) {
            schedule.setSpace(getActiveSpace(spaceScheduleDTO.getSpaceId()));
        }
        if (spaceScheduleDTO.getDayOfWeek() != null && !spaceScheduleDTO.getDayOfWeek().equals(schedule.getDayOfWeek())) {
            assertUniqueDay(schedule.getSpace().getId(), spaceScheduleDTO.getDayOfWeek(), schedule.getId());
            schedule.setDayOfWeek(spaceScheduleDTO.getDayOfWeek());
        }
        if (spaceScheduleDTO.getOpenTime() != null) {
            validateTimeRange(spaceScheduleDTO.getOpenTime(),
                    spaceScheduleDTO.getCloseTime() != null ? spaceScheduleDTO.getCloseTime() : schedule.getCloseTime());
            schedule.setOpenTime(spaceScheduleDTO.getOpenTime());
        }
        if (spaceScheduleDTO.getCloseTime() != null) {
            validateTimeRange(spaceScheduleDTO.getOpenTime() != null ? spaceScheduleDTO.getOpenTime() : schedule.getOpenTime(),
                    spaceScheduleDTO.getCloseTime());
            schedule.setCloseTime(spaceScheduleDTO.getCloseTime());
        }
        if (spaceScheduleDTO.getHolidayOverride() != null) {
            schedule.setHolidayOverride(spaceScheduleDTO.getHolidayOverride());
        }
        if (spaceScheduleDTO.getMaintenanceNotes() != null) {
            schedule.setMaintenanceNotes(spaceScheduleDTO.getMaintenanceNotes());
        }
        schedule.setUpdatedAt(LocalDateTime.now());

        SpaceSchedule saved = spaceScheduleRepository.save(schedule);
        
        // Auditoría: Horario de espacio actualizado
        recordAudit("SPACE_SCHEDULE_UPDATED", saved, details -> {
            if (spaceScheduleDTO.getDayOfWeek() != null) {
                details.put("dayChanged", true);
                details.put("dayOfWeek", saved.getDayOfWeek().name());
            }
            if (spaceScheduleDTO.getOpenTime() != null || spaceScheduleDTO.getCloseTime() != null) {
                details.put("hoursChanged", true);
                details.put("openTime", saved.getOpenTime().toString());
                details.put("closeTime", saved.getCloseTime().toString());
            }
        });
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpaceScheduleDTO findById(Long id) {
        SpaceSchedule schedule = spaceScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space schedule with id " + id + " not found"));
        return toDto(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceScheduleDTO> findAll() {
        return spaceScheduleRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceScheduleDTO> findBySpace(Long spaceId) {
        return spaceScheduleRepository.findBySpaceId(spaceId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        SpaceSchedule schedule = spaceScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space schedule with id " + id + " not found"));
        
        // Auditoría: Horario de espacio eliminado
        recordAudit("SPACE_SCHEDULE_DELETED", schedule, details -> {
            details.put("dayOfWeek", schedule.getDayOfWeek().name());
        });
        
        spaceScheduleRepository.delete(schedule);
    }

    private void validateTimeRange(java.time.LocalTime openTime, java.time.LocalTime closeTime) {
        if (openTime == null || closeTime == null) {
            return;
        }
        if (!openTime.isBefore(closeTime)) {
            throw new BusinessRuleException("Open time must be before close time");
        }
    }

    private void assertUniqueDay(Long spaceId, java.time.DayOfWeek dayOfWeek, Long currentId) {
        spaceScheduleRepository.findBySpaceIdAndDayOfWeek(spaceId, dayOfWeek)
                .filter(existing -> !Objects.equals(existing.getId(), currentId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Space already has a schedule for the selected day");
                });
    }

    private Space getActiveSpace(Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Space with id " + spaceId + " not found"));
        if (space.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Space with id " + spaceId + " not found");
        }
        return space;
    }

    private SpaceScheduleDTO toDto(SpaceSchedule schedule) {
        SpaceScheduleDTO dto = modelMapper.map(schedule, SpaceScheduleDTO.class);
        dto.setSpaceId(schedule.getSpace() != null ? schedule.getSpace().getId() : null);
        return dto;
    }
    
    /**
     * Registra un evento de auditoría para acciones de horarios de espacio
     */
    private void recordAudit(String action, SpaceSchedule schedule, Consumer<ObjectNode> detailsCustomizer) {
        Long actorId = null;
        try {
            actorId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
            actorId = null;
        }
        
        ObjectNode details = objectMapper.createObjectNode();
        details.put("scheduleId", schedule.getId());
        if (schedule.getSpace() != null) {
            details.put("spaceId", schedule.getSpace().getId());
            details.put("spaceName", schedule.getSpace().getName());
        }
        details.put("holidayOverride", schedule.getHolidayOverride());
        
        if (detailsCustomizer != null) {
            detailsCustomizer.accept(details);
        }
        
        String entityId = schedule.getId() != null ? schedule.getId().toString() : null;
        auditLogService.logEvent(actorId, action, entityId, details);
    }
}