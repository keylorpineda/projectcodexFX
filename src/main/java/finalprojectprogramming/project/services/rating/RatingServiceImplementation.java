package finalprojectprogramming.project.services.rating;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.dtos.RatingDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Rating;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.RatingRepository;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RatingServiceImplementation implements RatingService {

    private final RatingRepository ratingRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public RatingServiceImplementation(RatingRepository ratingRepository,
            ReservationRepository reservationRepository, ModelMapper modelMapper,
            AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.ratingRepository = ratingRepository;
        this.reservationRepository = reservationRepository;
        this.modelMapper = modelMapper;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public RatingDTO create(RatingDTO ratingDTO) {
        Reservation reservation = getActiveReservation(ratingDTO.getReservationId());
        if (reservation.getUser() == null) {
            throw new BusinessRuleException("Reservation has no associated user");
        }
        SecurityUtils.requireSelfOrAny(reservation.getUser().getId(), UserRole.SUPERVISOR, UserRole.ADMIN);
        if (reservation.getRating() != null || ratingRepository.findByReservationId(reservation.getId()).isPresent()) {
            throw new BusinessRuleException("Reservation already has a rating");
        }
        if (reservation.getStatus() != ReservationStatus.COMPLETED && reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessRuleException("Only completed or checked-in reservations can be rated");
        }

        Rating rating = Rating.builder()
                .reservation(reservation)
                .user(reservation.getUser())
                .space(reservation.getSpace())
                .score(ratingDTO.getScore())
                .comment(ratingDTO.getComment())
                .helpfulCount(0)
                .visible(true)
                .build();

        reservation.setRating(rating);

        Rating saved = ratingRepository.save(rating);
        
        // Auditoría: Reseña creada
        recordAudit("RATING_CREATED", saved, details -> {
            details.put("score", saved.getScore());
            details.put("reservationId", reservation.getId());
            details.put("hasComment", saved.getComment() != null && !saved.getComment().isEmpty());
        });
        
        return toDto(saved);
    }

    @Override
    public RatingDTO update(Long id, RatingDTO ratingDTO) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with id " + id + " not found"));

        if (ratingDTO.getScore() != null) {
            rating.setScore(ratingDTO.getScore());
        }
        if (ratingDTO.getComment() != null) {
            rating.setComment(ratingDTO.getComment());
        }
        if (ratingDTO.getCreatedAt() != null) {
            rating.setCreatedAt(ratingDTO.getCreatedAt());
        }

        Rating saved = ratingRepository.save(rating);
        
        // Auditoría: Reseña actualizada
        recordAudit("RATING_UPDATED", saved, details -> {
            if (ratingDTO.getScore() != null) {
                details.put("scoreChanged", ratingDTO.getScore());
            }
            if (ratingDTO.getComment() != null) {
                details.put("commentChanged", true);
            }
        });
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingDTO findById(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with id " + id + " not found"));
        return toDto(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingDTO> findAll() {
        return ratingRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RatingDTO findByReservation(Long reservationId) {
        Rating rating = ratingRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rating for reservation " + reservationId + " not found"));
        return toDto(rating);
    }

    @Override
    public void delete(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with id " + id + " not found"));
        Reservation reservation = rating.getReservation();
        if (reservation != null) {
            reservation.setRating(null);
        }
        
        // Auditoría: Reseña eliminada
        recordAudit("RATING_DELETED", rating, details -> {
            details.put("score", rating.getScore());
            if (reservation != null) {
                details.put("reservationId", reservation.getId());
            }
        });
        
        ratingRepository.delete(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingDTO> findBySpace(Long spaceId) {
        return ratingRepository.findBySpaceIdAndVisibleTrueOrderByCreatedAtDesc(spaceId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageBySpace(Long spaceId) {
        Double average = ratingRepository.getAverageScoreBySpaceId(spaceId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCountBySpace(Long spaceId) {
        return ratingRepository.getRatingCountBySpaceId(spaceId);
    }

    @Override
    public RatingDTO toggleVisibility(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with id " + id + " not found"));
        boolean newVisibility = !rating.getVisible();
        rating.setVisible(newVisibility);
        Rating saved = ratingRepository.save(rating);
        
        // Auditoría: Visibilidad de reseña cambiada
        recordAudit("RATING_VISIBILITY_CHANGED", saved, details -> {
            details.put("visible", newVisibility);
            details.put("score", saved.getScore());
        });
        
        return toDto(saved);
    }

    @Override
    public RatingDTO incrementHelpful(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating with id " + id + " not found"));
        rating.setHelpfulCount(rating.getHelpfulCount() + 1);
        Rating saved = ratingRepository.save(rating);
        return toDto(saved);
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

    private RatingDTO toDto(Rating rating) {
        RatingDTO dto = modelMapper.map(rating, RatingDTO.class);
        dto.setReservationId(rating.getReservation() != null ? rating.getReservation().getId() : null);
        dto.setUserId(rating.getUser() != null ? rating.getUser().getId() : null);
        dto.setUserName(rating.getUser() != null ? rating.getUser().getName() : null);
        dto.setSpaceName(rating.getSpace() != null ? rating.getSpace().getName() : null);
        return dto;
    }
    
    /**
     * Registra un evento de auditoría para acciones de rating/reseña
     */
    private void recordAudit(String action, Rating rating, Consumer<ObjectNode> detailsCustomizer) {
        Long actorId = null;
        try {
            actorId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
            actorId = null;
        }
        
        ObjectNode details = objectMapper.createObjectNode();
        details.put("ratingId", rating.getId());
        if (rating.getUser() != null) {
            details.put("userId", rating.getUser().getId());
        }
        if (rating.getSpace() != null) {
            details.put("spaceId", rating.getSpace().getId());
            details.put("spaceName", rating.getSpace().getName());
        }
        details.put("score", rating.getScore());
        details.put("visible", rating.getVisible());
        
        if (detailsCustomizer != null) {
            detailsCustomizer.accept(details);
        }
        
        String entityId = rating.getId() != null ? rating.getId().toString() : null;
        auditLogService.logEvent(actorId, action, entityId, details);
    }
}