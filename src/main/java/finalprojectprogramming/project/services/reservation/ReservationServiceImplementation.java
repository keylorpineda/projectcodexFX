package finalprojectprogramming.project.services.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.dtos.ReservationAttendeeDTO;
import finalprojectprogramming.project.dtos.ReservationCheckInRequest;
import finalprojectprogramming.project.dtos.ReservationDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.ReservationAttendee;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.repositories.UserRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
import finalprojectprogramming.project.services.notification.ReservationNotificationService;
import finalprojectprogramming.project.services.space.SpaceAvailabilityValidator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationServiceImplementation implements ReservationService {

    // ✅ Zona horaria fija para Costa Rica
    private static final ZoneId COSTA_RICA_ZONE = ZoneId.of("America/Costa_Rica");

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final ModelMapper modelMapper;
    private final SpaceAvailabilityValidator availabilityValidator;
    private final ReservationNotificationService reservationNotificationService;
    private final ReservationCancellationPolicy cancellationPolicy;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ReservationServiceImplementation(ReservationRepository reservationRepository,
            UserRepository userRepository, SpaceRepository spaceRepository,
            ModelMapper modelMapper, SpaceAvailabilityValidator availabilityValidator,
            ReservationNotificationService reservationNotificationService,
            ReservationCancellationPolicy cancellationPolicy, AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.modelMapper = modelMapper;
        this.availabilityValidator = availabilityValidator;
        this.reservationNotificationService = reservationNotificationService;
        this.cancellationPolicy = cancellationPolicy;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;

    }

    /**
     * Obtiene la hora actual en la zona horaria de Costa Rica.
     * Usa este método en lugar de LocalDateTime.now() para evitar problemas de zona horaria.
     */
    private LocalDateTime nowCostaRica() {
        return ZonedDateTime.now(COSTA_RICA_ZONE).toLocalDateTime();
    }

    @Override
    public ReservationDTO create(ReservationDTO reservationDTO) {
        SecurityUtils.requireSelfOrAny(reservationDTO.getUserId(), UserRole.SUPERVISOR, UserRole.ADMIN);
        
        // ✅ VALIDACIÓN: Mínimo 60 minutos de anticipación
        // Las reservas deben hacerse con anticipación para que un administrador las confirme
        // Usamos ZonedDateTime con zona horaria explícita de Costa Rica
        ZonedDateTime now = ZonedDateTime.now(COSTA_RICA_ZONE);
        LocalDateTime startTime = reservationDTO.getStartTime();
        if (startTime != null) {
            // Convertir LocalDateTime a ZonedDateTime para comparación correcta
            ZonedDateTime zonedStartTime = startTime.atZone(COSTA_RICA_ZONE);
            
            // Validar que la hora de inicio sea al menos 60 minutos en el futuro
            long minutesUntilStart = java.time.Duration.between(now, zonedStartTime).toMinutes();
            if (minutesUntilStart < 60) {
                throw new BusinessRuleException(
                    "Las reservas deben hacerse con al menos 60 minutos de anticipación. " +
                    "Tiempo restante: " + minutesUntilStart + " minutos"
                );
            }
        }
        
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
        reservation.setCreatedAt(now.toLocalDateTime());
        reservation.setUpdatedAt(now.toLocalDateTime());
        reservation.setDeletedAt(null);
        if (reservation.getNotifications() == null) {
            reservation.setNotifications(new ArrayList<>());
        }
         if (reservation.getAttendeeRecords() == null) {
            reservation.setAttendeeRecords(new ArrayList<>());
        }
        Reservation saved = reservationRepository.save(reservation);
        reservationNotificationService.notifyReservationCreated(saved);
        recordAudit("RESERVATION_CREATED", saved, details -> {
            details.put("startTime", saved.getStartTime() != null ? saved.getStartTime().toString() : null);
            details.put("endTime", saved.getEndTime() != null ? saved.getEndTime().toString() : null);
        });
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
        reservation.setUpdatedAt(nowCostaRica());
        Reservation saved = reservationRepository.save(reservation);
        recordAudit("RESERVATION_UPDATED", saved, details -> details.put("updatedAt", saved.getUpdatedAt().toString()));
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
        
        // ✅ CORRECCIÓN: Solo validar política si NO es ADMIN
        // Los ADMIN pueden cancelar en cualquier momento
        boolean isAdmin = SecurityUtils.hasAny(UserRole.ADMIN);
        if (!isAdmin) {
            cancellationPolicy.assertCancellationAllowed(reservation);
        }
        
        reservation.setStatus(ReservationStatus.CANCELED);
        reservation.setCancellationReason(cancellationReason);
        reservation.setCanceledAt(nowCostaRica());
        reservation.setUpdatedAt(nowCostaRica());
        Reservation saved = reservationRepository.save(reservation);
        reservationNotificationService.notifyReservationCanceled(saved);
        recordAudit("RESERVATION_CANCELED", saved, details -> {
            details.put("cancellationReason", cancellationReason != null ? cancellationReason : "");
            details.put("canceledAt", saved.getCanceledAt() != null ? saved.getCanceledAt().toString() : null);
        });
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
        reservation.setUpdatedAt(nowCostaRica());
        Reservation saved = reservationRepository.save(reservation);
        reservationNotificationService.notifyReservationApproved(saved);
        recordAudit("RESERVATION_APPROVED", saved, details -> details.put("approvedBy",
                saved.getApprovedBy() != null ? saved.getApprovedBy().getId() : null));
        return toDto(saved);
    }

    @Override
     public ReservationDTO markCheckIn(Long id, ReservationCheckInRequest request) {
         SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        Reservation reservation = getActiveReservation(id);
        if (request == null) {
            throw new BusinessRuleException("Check-in data is required");
        }
        
        // ✅ Validación mejorada del estado: el QR solo funciona si la reserva está CONFIRMED
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            throw new BusinessRuleException("QR code is not yet active. Reservation must be approved by an administrator first");
        }
        
        if (reservation.getStatus() != ReservationStatus.CONFIRMED
                && reservation.getStatus() != ReservationStatus.CHECKED_IN) {
             throw new BusinessRuleException("Only confirmed reservations can be checked in");
        }
        
        if (reservation.getQrCode() == null || !reservation.getQrCode().equalsIgnoreCase(request.getQrCode())) {
            throw new BusinessRuleException("Provided QR code does not match reservation");
        }
        
        // ✅ Validar ventana temporal: 30 minutos antes y 30 minutos después del inicio
        // Usar zona horaria de Costa Rica explícitamente
        ZonedDateTime now = ZonedDateTime.now(COSTA_RICA_ZONE);
        ZonedDateTime startTimeZoned = reservation.getStartTime().atZone(COSTA_RICA_ZONE);
        ZonedDateTime windowStart = startTimeZoned.minusMinutes(30);
        ZonedDateTime windowEnd = startTimeZoned.plusMinutes(30);
        
        if (now.isBefore(windowStart)) {
            long minutesUntil = java.time.Duration.between(now, windowStart).toMinutes();
            throw new BusinessRuleException(
                String.format("QR code cannot be scanned yet. Check-in opens %d minutes before the reservation starts (at %s)",
                    30, windowStart.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
            );
        }
        
        if (now.isAfter(windowEnd)) {
            throw new BusinessRuleException(
                "Check-in window has closed. This reservation will be automatically marked as NO_SHOW"
            );
        }
        List<ReservationAttendee> attendeeRecords = reservation.getAttendeeRecords();
        if (attendeeRecords == null) {
            attendeeRecords = new ArrayList<>();
            reservation.setAttendeeRecords(attendeeRecords);
        }

        if (reservation.getAttendees() != null && attendeeRecords.size() >= reservation.getAttendees()) {
            throw new BusinessRuleException("Reservation already reached the maximum number of attendees");
        }

        String requestedIdNumber = request.getAttendeeIdNumber() != null ? request.getAttendeeIdNumber().trim() : "";
        if (requestedIdNumber.isBlank()) {
            throw new BusinessRuleException("Attendee identification number is required");
        }

        boolean duplicatedAttendee = attendeeRecords.stream()
                .filter(Objects::nonNull)
                .map(ReservationAttendee::getIdNumber)
                .filter(Objects::nonNull)
                .anyMatch(existing -> existing.equalsIgnoreCase(requestedIdNumber));
        if (duplicatedAttendee) {
            throw new BusinessRuleException("This attendee has already been registered for the reservation");
        }

        String firstName = request.getAttendeeFirstName() != null ? request.getAttendeeFirstName().trim() : "";
        if (firstName.isBlank()) {
            throw new BusinessRuleException("Attendee first name is required");
        }

        String lastName = request.getAttendeeLastName() != null ? request.getAttendeeLastName().trim() : "";
        if (lastName.isBlank()) {
            throw new BusinessRuleException("Attendee last name is required");
        }

        // Usar zona horaria de Costa Rica para el timestamp de check-in
        LocalDateTime checkInTimestamp = now.toLocalDateTime();
        ReservationAttendee attendee = ReservationAttendee.builder()
                .reservation(reservation)
                .idNumber(requestedIdNumber)
                .firstName(firstName)
                .lastName(lastName)
                .checkInAt(checkInTimestamp)
                .build();
        attendeeRecords.add(attendee);
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setCheckinAt(checkInTimestamp);
        reservation.setUpdatedAt(checkInTimestamp);
        Reservation saved = reservationRepository.save(reservation);
        recordAudit("RESERVATION_CHECKED_IN", saved, details -> {
            details.put("checkInAt", saved.getCheckinAt() != null ? saved.getCheckinAt().toString() : null);
            details.put("attendeeCount",
                    saved.getAttendeeRecords() != null ? saved.getAttendeeRecords().size() : 0);
        });
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
        if (reservation.getStartTime() != null && reservation.getStartTime().isAfter(nowCostaRica())) {
            throw new BusinessRuleException("No-show can only be registered after the reservation start time");
        }
        reservation.setStatus(ReservationStatus.NO_SHOW);
        reservation.setUpdatedAt(nowCostaRica());
        Reservation saved = reservationRepository.save(reservation);
        recordAudit("RESERVATION_MARKED_NO_SHOW", saved, details ->
                details.put("markedAt", saved.getUpdatedAt().toString()));
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        Reservation reservation = getActiveReservation(id);
        
        // Los usuarios pueden eliminar sus propias reservas canceladas
        // Los supervisores y admins pueden eliminar cualquier reserva
        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            // Si está cancelada, el dueño puede eliminarla
            SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN);
        } else {
            // Para otros estados, solo SUPERVISOR o ADMIN
            SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        }
        
        reservation.setDeletedAt(nowCostaRica());
        reservation.setUpdatedAt(nowCostaRica());
        reservationRepository.save(reservation);
        recordAudit("RESERVATION_SOFT_DELETED", reservation,
                details -> details.put("deletedAt", reservation.getDeletedAt().toString()));
    }

    @Override
    public void hardDelete(Long id) {
        SecurityUtils.requireAny(UserRole.ADMIN);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with id " + id + " not found"));
        
        // ✅ ADMIN puede eliminar permanentemente CUALQUIER reserva sin restricciones
        // Ya no validamos estados - ADMIN tiene control total
        
        reservationRepository.delete(reservation);
        recordAudit("RESERVATION_HARD_DELETED", reservation, details -> {
            details.put("status", reservation.getStatus() != null ? reservation.getStatus().name() : null);
        });
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
                         dto.setAttendeeRecords(reservation.getAttendeeRecords() == null ? new ArrayList<>()
                : reservation.getAttendeeRecords().stream()
                        .filter(Objects::nonNull)
                        .sorted((left, right) -> {
                            LocalDateTime leftTime = left.getCheckInAt();
                            LocalDateTime rightTime = right.getCheckInAt();
                            if (leftTime == null && rightTime == null) {
                                return 0;
                            }
                            if (leftTime == null) {
                                return -1;
                            }
                            if (rightTime == null) {
                                return 1;
                            }
                            return leftTime.compareTo(rightTime);
                        })
                        .map(attendee -> ReservationAttendeeDTO.builder()
                                .id(attendee.getId())
                                .reservationId(reservation.getId())
                                .idNumber(attendee.getIdNumber())
                                .firstName(attendee.getFirstName())
                                .lastName(attendee.getLastName())
                                .checkInAt(attendee.getCheckInAt())
                                .build())
                        .collect(Collectors.toList()));
        return dto;
    }

    private ReservationStatus determineInitialStatus(Space space, ReservationStatus requestedStatus) {
        // ✅ TODAS las reservas empiezan en PENDING para que un administrador las confirme
        // Solo después de la confirmación del admin, el QR se habilita para check-in
        if (requestedStatus == ReservationStatus.CANCELED || requestedStatus == ReservationStatus.NO_SHOW) {
            throw new BusinessRuleException("Reservation cannot be created with status " + requestedStatus);
        }
        return ReservationStatus.PENDING;
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

    private void recordAudit(String action, Reservation reservation, Consumer<ObjectNode> detailsCustomizer) {
        try {
            Long actorId = null;
            try {
                actorId = SecurityUtils.getCurrentUserId();
            } catch (AuthenticationCredentialsNotFoundException ignored) {
                actorId = null;
            }

            ObjectNode details = objectMapper.createObjectNode();
            details.put("reservationId", reservation.getId());
            if (reservation.getUser() != null) {
                details.put("userId", reservation.getUser().getId());
            }
            if (reservation.getSpace() != null) {
                details.put("spaceId", reservation.getSpace().getId());
            }
            if (reservation.getStatus() != null) {
                details.put("status", reservation.getStatus().name());
            }
            if (detailsCustomizer != null) {
                detailsCustomizer.accept(details);
            }

            String entityId = reservation.getId() != null ? reservation.getId().toString() : null;
            auditLogService.logEvent(actorId, action, entityId, details);
        } catch (Exception e) {
            // ❌ Error al registrar audit log - no romper la operación principal
            System.err.println("❌ Error al registrar audit log para acción " + action + ": " + e.getMessage());
        }
    }
}
