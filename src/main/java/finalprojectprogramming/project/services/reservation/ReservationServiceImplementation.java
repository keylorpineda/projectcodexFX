package finalprojectprogramming.project.services.reservation;

import finalprojectprogramming.project.dtos.ReservationDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.repositories.UserRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.services.notification.ReservationNotificationService;
import finalprojectprogramming.project.services.space.SpaceAvailabilityValidator;
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
public class ReservationServiceImplementation implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final ModelMapper modelMapper;
    private final SpaceAvailabilityValidator availabilityValidator;
    private final ReservationNotificationService reservationNotificationService;

    public ReservationServiceImplementation(ReservationRepository reservationRepository,
            UserRepository userRepository, SpaceRepository spaceRepository,
            ModelMapper modelMapper, SpaceAvailabilityValidator availabilityValidator,
            ReservationNotificationService reservationNotificationService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.modelMapper = modelMapper;
        this.availabilityValidator = availabilityValidator;
        this.reservationNotificationService = reservationNotificationService;

    }

    @Override
    public ReservationDTO create(ReservationDTO reservationDTO) {
        SecurityUtils.requireSelfOrAny(reservationDTO.getUserId(), UserRole.SUPERVISOR, UserRole.ADMIN);
        User user = getUser(reservationDTO.getUserId());
        Space space = getSpace(reservationDTO.getSpaceId());
        availabilityValidator.assertAvailability(space, reservationDTO.getStartTime(), reservationDTO.getEndTime(),
                null);
        validateQrCode(reservationDTO.getQrCode(), null);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setSpace(space);
        reservation.setStartTime(reservationDTO.getStartTime());
        reservation.setEndTime(reservationDTO.getEndTime());
        reservation.setStatus(determineInitialStatus(space, reservationDTO.getStatus()));
        reservation.setQrCode(reservationDTO.getQrCode());
        reservation.setNotes(reservationDTO.getNotes());
        reservation.setAttendees(reservationDTO.getAttendees());
        reservation.setWeatherCheck(reservationDTO.getWeatherCheck());
        reservation.setCancellationReason(null);
        reservation.setApprovedBy(resolveApproverForCreation(space, reservationDTO.getApprovedByUserId()));
        reservation.setCanceledAt(null);
        reservation.setCheckinAt(null);
        LocalDateTime now = LocalDateTime.now();
        reservation.setCreatedAt(now);
        reservation.setUpdatedAt(now);
        reservation.setDeletedAt(null);
        if (reservation.getNotifications() == null) {
            reservation.setNotifications(new ArrayList<>());
        }
        Reservation saved = reservationRepository.save(reservation);
        reservationNotificationService.notifyReservationCreated(saved);
        return toDto(saved);
    }

    @Override
    public ReservationDTO update(Long id, ReservationDTO reservationDTO) {
        Reservation reservation = getActiveReservation(id);
        SecurityUtils.requireSelfOrAny(reservation.getUser() != null ? reservation.getUser().getId() : null,
                UserRole.SUPERVISOR, UserRole.ADMIN);
        Space targetSpace = reservation.getSpace();

        if (reservationDTO.getSpaceId() != null && !Objects.equals(reservationDTO.getSpaceId(), targetSpace.getId())) {
            targetSpace = getSpace(reservationDTO.getSpaceId());
            reservation.setSpace(targetSpace);
        }

        LocalDateTime newStart = reservationDTO.getStartTime() != null ? reservationDTO.getStartTime()
                : reservation.getStartTime();
        LocalDateTime newEnd = reservationDTO.getEndTime() != null ? reservationDTO.getEndTime()
                : reservation.getEndTime();
        availabilityValidator.assertAvailability(targetSpace, newStart, newEnd, reservation.getId());

        if (reservationDTO.getQrCode() != null && !reservationDTO.getQrCode().isBlank()) {
            validateQrCode(reservationDTO.getQrCode(), reservation.getId());
            reservation.setQrCode(reservationDTO.getQrCode());
        }
        reservation.setStartTime(newStart);
        reservation.setEndTime(newEnd);
        if (reservationDTO.getAttendees() != null) {
            reservation.setAttendees(reservationDTO.getAttendees());
        }
        if (reservationDTO.getNotes() != null) {
            reservation.setNotes(reservationDTO.getNotes());
        }
        if (reservationDTO.getWeatherCheck() != null) {
            reservation.setWeatherCheck(reservationDTO.getWeatherCheck());
        }
        if (reservationDTO.getApprovedByUserId() != null) {
            reservation.setApprovedBy(getUser(reservationDTO.getApprovedByUserId()));
        }
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDTO findById(Long id) {
        Reservation reservation = getActiveReservation(id);
        SecurityUtils.requireSelfOrAny(reservation.getUser() != null ? reservation.getUser().getId() : null,
                UserRole.SUPERVISOR, UserRole.ADMIN);
        return toDto(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> findAll() {
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getDeletedAt() == null)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> findByUser(Long userId) {
        SecurityUtils.requireSelfOrAny(userId, UserRole.SUPERVISOR, UserRole.ADMIN);
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getDeletedAt() == null)
                .filter(reservation -> reservation.getUser() != null
                        && Objects.equals(reservation.getUser().getId(), userId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> findBySpace(Long spaceId) {
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getDeletedAt() == null)
                .filter(reservation -> reservation.getSpace() != null
                        && Objects.equals(reservation.getSpace().getId(), spaceId))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDTO cancel(Long id, String cancellationReason) {
        Reservation reservation = getActiveReservation(id);
        SecurityUtils.requireSelfOrAny(reservation.getUser() != null ? reservation.getUser().getId() : null,
                UserRole.SUPERVISOR, UserRole.ADMIN);
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return toDto(reservation);
        }
        if (reservation.getStartTime() != null && reservation.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Reservations cannot be canceled after their start time");
        }
        reservation.setStatus(ReservationStatus.CANCELED);
        reservation.setCancellationReason(cancellationReason);
        reservation.setCanceledAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        reservationNotificationService.notifyReservationCanceled(saved);
        return toDto(saved);
    }

    @Override
    public ReservationDTO approve(Long id, Long approverUserId) {
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        Reservation reservation = getActiveReservation(id);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessRuleException("Only pending reservations can be approved");
        }
        reservation.setApprovedBy(getUser(approverUserId));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        reservationNotificationService.notifyReservationApproved(saved);
        return toDto(saved);
    }

    @Override
    public ReservationDTO markCheckIn(Long id) {
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        Reservation reservation = getActiveReservation(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Only confirmed reservations can be checked in");
        }
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setCheckinAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        return toDto(saved);
    }

    @Override
    public ReservationDTO markNoShow(Long id) {
        Reservation reservation = getActiveReservation(id);
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Only confirmed reservations can be marked as no-show");
        }
        if (reservation.getCheckinAt() != null) {
            throw new BusinessRuleException("Reservation already has a check-in registered");
        }
        if (reservation.getStartTime() != null && reservation.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("No-show can only be registered after the reservation start time");
        }
        reservation.setStatus(ReservationStatus.NO_SHOW);
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        Reservation reservation = getActiveReservation(id);
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        reservation.setDeletedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);
    }

    private Reservation getActiveReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with id " + id + " not found"));
        if (reservation.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Reservation with id " + id + " not found");
        }
        return reservation;
    }

    private User getUser(Long userId) {
        if (userId == null) {
            throw new BusinessRuleException("User identifier is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
    }

    private Space getSpace(Long spaceId) {
        if (spaceId == null) {
            throw new BusinessRuleException("Space identifier is required");
        }
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Space with id " + spaceId + " not found"));
        if (space.getDeletedAt() != null || Boolean.FALSE.equals(space.getActive())) {
            throw new BusinessRuleException("Space is not available for reservations");
        }
        return space;
    }

    private ReservationDTO toDto(Reservation reservation) {
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);
        dto.setUserId(reservation.getUser() != null ? reservation.getUser().getId() : null);
        dto.setSpaceId(reservation.getSpace() != null ? reservation.getSpace().getId() : null);
        dto.setApprovedByUserId(reservation.getApprovedBy() != null ? reservation.getApprovedBy().getId() : null);
        dto.setRatingId(reservation.getRating() != null ? reservation.getRating().getId() : null);
        dto.setNotificationIds(reservation.getNotifications() == null ? new ArrayList<>()
                : reservation.getNotifications().stream()
                        .map(Notification::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        return dto;
    }

    private ReservationStatus determineInitialStatus(Space space, ReservationStatus requestedStatus) {
        if (space.getRequiresApproval() != null && space.getRequiresApproval()) {
            return ReservationStatus.PENDING;
        }
        if (requestedStatus == null) {
            return ReservationStatus.CONFIRMED;
        }
        if (requestedStatus == ReservationStatus.CANCELED || requestedStatus == ReservationStatus.NO_SHOW) {
            throw new BusinessRuleException("Reservation cannot be created with status " + requestedStatus);
        }
        return requestedStatus;
    }

    private User resolveApproverForCreation(Space space, Long approverId) {
        if (approverId == null) {
            return null;
        }
        if (space.getRequiresApproval() != null && space.getRequiresApproval()) {
            throw new BusinessRuleException("Spaces that require approval cannot have a pre-approved reservation");
        }
        return getUser(approverId);
    }

    private void validateQrCode(String qrCode, Long reservationId) {
        if (qrCode == null || qrCode.isBlank()) {
            throw new BusinessRuleException("QR code is required");
        }
        boolean exists = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getDeletedAt() == null)
                .filter(reservation -> reservationId == null || !Objects.equals(reservation.getId(), reservationId))
                .anyMatch(reservation -> qrCode.equalsIgnoreCase(reservation.getQrCode()));
        if (exists) {
            throw new BusinessRuleException("QR code is already associated with another reservation");
        }
    }
}
