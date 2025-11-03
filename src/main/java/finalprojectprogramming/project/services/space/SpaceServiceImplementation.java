package finalprojectprogramming.project.services.space;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceImage;
import finalprojectprogramming.project.models.SpaceSchedule;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.repositories.RatingRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
import finalprojectprogramming.project.services.spaceimage.SpaceImageService;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SpaceServiceImplementation implements SpaceService {

    private final SpaceRepository spaceRepository;
    private final ModelMapper modelMapper;
    private final SpaceAvailabilityValidator availabilityValidator;
    private final RatingRepository ratingRepository;
    private final SpaceImageService spaceImageService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public SpaceServiceImplementation(SpaceRepository spaceRepository, ModelMapper modelMapper,
            SpaceAvailabilityValidator availabilityValidator, RatingRepository ratingRepository,
            SpaceImageService spaceImageService, AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.spaceRepository = spaceRepository;
        this.modelMapper = modelMapper;
        this.availabilityValidator = availabilityValidator;
        this.ratingRepository = ratingRepository;
        this.spaceImageService = spaceImageService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public SpaceDTO create(SpaceDTO spaceDTO) {
        SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR);
        Space space = modelMapper.map(spaceDTO, Space.class);
        LocalDateTime now = LocalDateTime.now();
        space.setId(null);
        space.setCreatedAt(now);
        space.setUpdatedAt(now);
        space.setDeletedAt(null);
        if (space.getImages() == null) {
            space.setImages(new ArrayList<>());
        }
        if (space.getSchedules() == null) {
            space.setSchedules(new ArrayList<>());
        }
        if (space.getReservations() == null) {
            space.setReservations(new ArrayList<>());
        }
        if (space.getActive() == null) {
            space.setActive(Boolean.TRUE);
        }
        if (space.getAverageRating() == null) {
            space.setAverageRating(0.0);
        }
        Space saved = spaceRepository.save(space);
        
        // Auditoría: Espacio creado
        recordAudit("SPACE_CREATED", saved, details -> {
            details.put("name", saved.getName());
            details.put("type", saved.getType() != null ? saved.getType().name() : "UNKNOWN");
            details.put("capacity", saved.getCapacity());
        });
        
        return toDto(saved);
    }

    @Override
    public SpaceDTO createWithImage(SpaceDTO spaceDTO, MultipartFile image) {
        SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR);
        SpaceDTO created = create(spaceDTO);
        
        if (image != null && !image.isEmpty()) {
            try {
                spaceImageService.upload(created.getId(), image, null, true, 1);
            } catch (Exception e) {
            }
        }
        
        return created;
    }

    @Override
    public SpaceDTO update(Long id, SpaceDTO spaceDTO) {
        SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR);
        Space space = getActiveSpaceOrThrow(id);

        if (spaceDTO.getName() != null) {
            space.setName(spaceDTO.getName());
        }
        if (spaceDTO.getType() != null) {
            space.setType(spaceDTO.getType());
        }
        if (spaceDTO.getCapacity() != null) {
            space.setCapacity(spaceDTO.getCapacity());
        }
        if (spaceDTO.getDescription() != null) {
            space.setDescription(spaceDTO.getDescription());
        }
        if (spaceDTO.getLocation() != null) {
            space.setLocation(spaceDTO.getLocation());
        }
        if (spaceDTO.getMaxReservationDuration() != null) {
            space.setMaxReservationDuration(spaceDTO.getMaxReservationDuration());
        }
        if (spaceDTO.getRequiresApproval() != null) {
            space.setRequiresApproval(spaceDTO.getRequiresApproval());
        }
        if (spaceDTO.getAverageRating() != null) {
            space.setAverageRating(spaceDTO.getAverageRating());
        }
        if (spaceDTO.getActive() != null && !Objects.equals(space.getActive(), spaceDTO.getActive())) {
            applyStatusChange(space, spaceDTO.getActive());
        }
        space.setUpdatedAt(LocalDateTime.now());
        Space saved = spaceRepository.save(space);
        
        // Auditoría: Espacio actualizado
        recordAudit("SPACE_UPDATED", saved, details -> {
            details.put("name", saved.getName());
            if (spaceDTO.getActive() != null) {
                details.put("activeChanged", spaceDTO.getActive());
            }
            if (spaceDTO.getCapacity() != null) {
                details.put("capacityChanged", spaceDTO.getCapacity());
            }
        });
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpaceDTO findById(Long id) {
        SecurityUtils.requireAny(UserRole.USER, UserRole.SUPERVISOR, UserRole.ADMIN);
        Space space = getActiveSpaceOrThrow(id);
        return toDto(space);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceDTO> findAll() {
        SecurityUtils.requireAny(UserRole.USER, UserRole.SUPERVISOR, UserRole.ADMIN);
        return spaceRepository.findAll().stream()
                .filter(space -> space.getDeletedAt() == null)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR);
        Space space = getActiveSpaceOrThrow(id);
        // Soft delete: marcar como eliminado Y desactivar
        space.setActive(false);
        space.setDeletedAt(LocalDateTime.now());
        space.setUpdatedAt(LocalDateTime.now());
        spaceRepository.save(space);
        
        // Auditoría: Espacio eliminado (soft delete)
        recordAudit("SPACE_DELETED", space, details -> {
            details.put("name", space.getName());
            details.put("softDelete", true);
        });
    }

    @Override
    public SpaceDTO changeStatus(Long id, boolean active) {
        SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR);
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space with id " + id + " not found"));
        applyStatusChange(space, active);
        space.setUpdatedAt(LocalDateTime.now());
        spaceRepository.save(space);
        return toDto(space);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceDTO> findAvailableSpaces(LocalDateTime startTime, LocalDateTime endTime,
            SpaceType type, Integer minimumCapacity) {
        SecurityUtils.requireAny(UserRole.USER, UserRole.SUPERVISOR, UserRole.ADMIN);
        availabilityValidator.validateTimeRange(startTime, endTime);
        return spaceRepository.findAll().stream()
                .filter(space -> space.getDeletedAt() == null)
                .filter(space -> Boolean.TRUE.equals(space.getActive()))
                .filter(space -> type == null || space.getType() == type)
                .filter(space -> minimumCapacity == null || space.getCapacity() >= minimumCapacity)
                .filter(space -> availabilityValidator.isAvailable(space, startTime, endTime, null))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void applyStatusChange(Space space, boolean active) {
        space.setActive(active);
        // Solo limpiar deletedAt si se está activando
        // NO marcar como eliminado al desactivar (eso es responsabilidad del método delete())
        if (active) {
            space.setDeletedAt(null);
        }
        // Si se está desactivando, solo cambia el campo active
        // No tocar deletedAt - eso es solo para eliminación real
    }

    private Space getActiveSpaceOrThrow(Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space with id " + id + " not found"));
        if (space.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Space with id " + id + " not found");
        }
        return space;
    }

    private SpaceDTO toDto(Space space) {
        SpaceDTO dto = modelMapper.map(space, SpaceDTO.class);
        dto.setImageIds(space.getImages() == null ? new ArrayList<>()
                : space.getImages().stream()
                        .map(SpaceImage::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        dto.setScheduleIds(space.getSchedules() == null ? new ArrayList<>()
                : space.getSchedules().stream()
                        .map(SpaceSchedule::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        dto.setReservationIds(space.getReservations() == null ? new ArrayList<>()
                : space.getReservations().stream()
                        .map(Reservation::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        if (dto.getActive() == null) {
            dto.setActive(Boolean.TRUE.equals(space.getActive()));
        }
        
        Double avgRating = ratingRepository.getAverageScoreBySpaceId(space.getId());
        dto.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        dto.setReviewCount(ratingRepository.getRatingCountBySpaceId(space.getId()));
        
        return dto;
    }

    void validateSpaceAvailability(Space space, LocalDateTime startTime, LocalDateTime endTime,
            Long reservationIdToIgnore) {
        availabilityValidator.assertAvailability(space, startTime, endTime, reservationIdToIgnore);
    }

    @Override
    public List<SpaceDTO> searchSpaces(SpaceType type, Integer minCapacity, Integer maxCapacity, 
                                        String location, Boolean active) {
        List<Space> allSpaces = spaceRepository.findAll();
        
        return allSpaces.stream()
                .filter(space -> {
                    // Filtrar espacios eliminados
                    if (space.getDeletedAt() != null) {
                        return false;
                    }
                    
                    // Filtro por tipo
                    if (type != null && !type.equals(space.getType())) {
                        return false;
                    }
                    
                    // Filtro por capacidad mínima
                    if (minCapacity != null && space.getCapacity() < minCapacity) {
                        return false;
                    }
                    
                    // Filtro por capacidad máxima
                    if (maxCapacity != null && space.getCapacity() > maxCapacity) {
                        return false;
                    }
                    
                    // Filtro por ubicación (búsqueda parcial case-insensitive)
                    if (location != null && !location.trim().isEmpty()) {
                        if (space.getLocation() == null || 
                            !space.getLocation().toLowerCase().contains(location.toLowerCase().trim())) {
                            return false;
                        }
                    }
                    
                    // Filtro por estado activo/inactivo
                    if (active != null && !active.equals(space.getActive())) {
                        return false;
                    }
                    
                    return true;
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Registra un evento de auditoría para acciones de espacio
     */
    private void recordAudit(String action, Space space, Consumer<ObjectNode> detailsCustomizer) {
        Long actorId = null;
        try {
            actorId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
            // Si no hay usuario autenticado, se registra como null
            actorId = null;
        }
        
        ObjectNode details = objectMapper.createObjectNode();
        details.put("spaceId", space.getId());
        if (space.getName() != null) {
            details.put("spaceName", space.getName());
        }
        if (space.getType() != null) {
            details.put("type", space.getType().name());
        }
        details.put("capacity", space.getCapacity() != null ? space.getCapacity() : 0);
        details.put("active", space.getActive() != null ? space.getActive() : false);
        
        if (detailsCustomizer != null) {
            detailsCustomizer.accept(details);
        }
        
        String entityId = space.getId() != null ? space.getId().toString() : null;
        auditLogService.logEvent(actorId, action, entityId, details);
    }
}