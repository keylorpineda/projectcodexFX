package finalprojectprogramming.project.services.rating;

import finalprojectprogramming.project.dtos.RatingDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Rating;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.repositories.RatingRepository;
import finalprojectprogramming.project.repositories.ReservationRepository;
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
        if (reservation.getRating() != null || ratingRepository.findByReservationId(reservation.getId()).isPresent()) {
            throw new BusinessRuleException("Reservation already has a rating");
        }

        Rating rating = new Rating();
        rating.setReservation(reservation);
        rating.setScore(ratingDTO.getScore());
        rating.setComment(ratingDTO.getComment());
        rating.setCreatedAt(ratingDTO.getCreatedAt() != null ? ratingDTO.getCreatedAt() : LocalDateTime.now());

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
        return dto;
    }
}