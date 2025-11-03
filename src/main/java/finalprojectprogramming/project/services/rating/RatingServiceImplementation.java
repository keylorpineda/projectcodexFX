package finalprojectprogramming.project.services.rating;

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
import java.time.LocalDateTime;
import java.util.List;
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

    public RatingServiceImplementation(RatingRepository ratingRepository,
            ReservationRepository reservationRepository, ModelMapper modelMapper) {
        this.ratingRepository = ratingRepository;
        this.reservationRepository = reservationRepository;
        this.modelMapper = modelMapper;
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
        rating.setVisible(!rating.getVisible());
        Rating saved = ratingRepository.save(rating);
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
}