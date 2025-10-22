package finalprojectprogramming.project.services.space;

import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceImage;
import finalprojectprogramming.project.models.SpaceSchedule;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public SpaceServiceImplementation(SpaceRepository spaceRepository, ModelMapper modelMapper,
            SpaceAvailabilityValidator availabilityValidator) {
        this.spaceRepository = spaceRepository;
        this.modelMapper = modelMapper;
        this.availabilityValidator = availabilityValidator;
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
            space.setAverageRating(0f);
        }
        Space saved = spaceRepository.save(space);
        return toDto(saved);
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
        applyStatusChange(space, false);
        space.setDeletedAt(LocalDateTime.now());
        space.setUpdatedAt(LocalDateTime.now());
        spaceRepository.save(space);
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
        if (active) {
            space.setDeletedAt(null);
        } else if (space.getDeletedAt() == null) {
            space.setDeletedAt(LocalDateTime.now());
        }
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
        return dto;
    }

    void validateSpaceAvailability(Space space, LocalDateTime startTime, LocalDateTime endTime,
            Long reservationIdToIgnore) {
        availabilityValidator.assertAvailability(space, startTime, endTime, reservationIdToIgnore);
    }
}