package finalprojectprogramming.project.services.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.dtos.NotificationDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.models.enums.NotificationType;
import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.repositories.NotificationRepository;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
import finalprojectprogramming.project.services.mail.EmailService;
import finalprojectprogramming.project.services.mail.EmailServiceImplementation.MailSendingException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImplementation implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImplementation.class);
    
    private final NotificationRepository notificationRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public NotificationServiceImplementation(NotificationRepository notificationRepository,
            ReservationRepository reservationRepository, ModelMapper modelMapper, EmailService emailService,
            AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.reservationRepository = reservationRepository;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
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
        
        // Auditoría: Notificación eliminada
        recordAudit("NOTIFICATION_DELETED", notification, details -> {
            details.put("type", notification.getType().name());
            details.put("sentTo", notification.getSentTo());
        });
        
        notificationRepository.delete(notification);
    }

    @Override
    public void sendCustomEmailToReservation(Long reservationId, String subject, String message) {
        SecurityUtils.requireAny(UserRole.SUPERVISOR, UserRole.ADMIN);
        Reservation reservation = getActiveReservation(reservationId);
        
        if (reservation.getUser() == null || reservation.getUser().getEmail() == null) {
            throw new ResourceNotFoundException("Reservation has no associated user or email");
        }
        
        // Crear notificación
        Notification notification = Notification.builder()
                .reservation(reservation)
                .type(NotificationType.REMINDER)
                .sentTo(reservation.getUser().getEmail())
                .messageContent(message)
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.SENT)
                .build();
        
        try {
            // Enviar email personalizado del admin (SIN datos de reserva, solo el mensaje)
            String userName = reservation.getUser().getName();
            String userEmail = reservation.getUser().getEmail();
            emailService.sendCustomAdminEmail(userEmail, userName, subject, message);
            LOGGER.info("Custom admin email sent to {} (no reservation details included)", userEmail);
            
            // Auditoría: Email personalizado enviado
            recordAudit("CUSTOM_EMAIL_SENT", notification, details -> {
                details.put("subject", subject);
                details.put("recipientEmail", userEmail);
                details.put("messageLength", message.length());
                details.put("note", "Email personalizado sin datos de reserva");
            });
        } catch (MailSendingException ex) {
            notification.setStatus(NotificationStatus.FAILED);
            LOGGER.error("Failed to send custom email for user {}", reservation.getUser().getEmail(), ex);
            throw ex;
        } finally {
            // Guardar notificación de todas formas
            if (reservation.getNotifications() != null) {
                reservation.getNotifications().add(notification);
            }
            notificationRepository.save(notification);
        }
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
    
    /**
     * Registra un evento de auditoría para acciones de notificaciones
     */
    private void recordAudit(String action, Notification notification, Consumer<ObjectNode> detailsCustomizer) {
        Long actorId = null;
        try {
            actorId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
            actorId = null;
        }
        
        ObjectNode details = objectMapper.createObjectNode();
        details.put("notificationId", notification.getId());
        if (notification.getReservation() != null) {
            details.put("reservationId", notification.getReservation().getId());
        }
        details.put("status", notification.getStatus().name());
        
        if (detailsCustomizer != null) {
            detailsCustomizer.accept(details);
        }
        
        String entityId = notification.getId() != null ? notification.getId().toString() : null;
        auditLogService.logEvent(actorId, action, entityId, details);
    }
}