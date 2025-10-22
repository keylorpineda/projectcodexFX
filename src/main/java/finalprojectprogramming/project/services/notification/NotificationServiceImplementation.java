package finalprojectprogramming.project.services.notification;

import finalprojectprogramming.project.dtos.NotificationDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.NotificationRepository;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImplementation implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    public NotificationServiceImplementation(NotificationRepository notificationRepository,
            ReservationRepository reservationRepository, ModelMapper modelMapper) {
        this.notificationRepository = notificationRepository;
        this.reservationRepository = reservationRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public NotificationDTO create(NotificationDTO notificationDTO) {
        Reservation reservation = getActiveReservation(notificationDTO.getReservationId());
        Notification notification = new Notification();
        notification.setReservation(reservation);
        notification.setType(notificationDTO.getType());
        notification.setSentTo(notificationDTO.getSentTo());
        notification.setMessageContent(notificationDTO.getMessageContent());
        notification.setSentAt(notificationDTO.getSentAt() != null ? notificationDTO.getSentAt() : LocalDateTime.now());
        notification.setStatus(notificationDTO.getStatus());

        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Override
    public NotificationDTO update(Long id, NotificationDTO notificationDTO) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with id " + id + " not found"));

        if (notificationDTO.getReservationId() != null
                && !Objects.equals(notificationDTO.getReservationId(),
                        notification.getReservation() != null ? notification.getReservation().getId() : null)) {
            notification.setReservation(getActiveReservation(notificationDTO.getReservationId()));
        }
        if (notificationDTO.getType() != null) {
            notification.setType(notificationDTO.getType());
        }
        if (notificationDTO.getSentTo() != null) {
            notification.setSentTo(notificationDTO.getSentTo());
        }
        if (notificationDTO.getMessageContent() != null) {
            notification.setMessageContent(notificationDTO.getMessageContent());
        }
        if (notificationDTO.getSentAt() != null) {
            notification.setSentAt(notificationDTO.getSentAt());
        }
        if (notificationDTO.getStatus() != null) {
            notification.setStatus(notificationDTO.getStatus());
        }

        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO findById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with id " + id + " not found"));
        Reservation reservation = notification.getReservation();
        Long ownerId = reservation != null && reservation.getUser() != null ? reservation.getUser().getId() : null;
        SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN);
        return toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> findAll() {
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        return notificationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> findByReservation(Long reservationId) {
        Reservation reservation = getActiveReservation(reservationId);
        Long ownerId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        SecurityUtils.requireSelfOrAny(ownerId, UserRole.SUPERVISOR, UserRole.ADMIN);
        return notificationRepository.findByReservationId(reservationId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with id " + id + " not found"));
        notificationRepository.delete(notification);
    }

    private Reservation getActiveReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Reservation with id " + reservationId + " not found"));
        if (reservation.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Reservation with id " + reservationId + " not found");
        }
        return reservation;
    }

    private NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = modelMapper.map(notification, NotificationDTO.class);
        dto.setReservationId(notification.getReservation() != null ? notification.getReservation().getId() : null);
        return dto;
    }
}