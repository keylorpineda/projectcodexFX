package finalprojectprogramming.project.services.rating;

import finalprojectprogramming.project.dtos.RatingDTO;
import java.util.List;

public interface RatingService {

    RatingDTO create(RatingDTO ratingDTO);

    RatingDTO update(Long id, RatingDTO ratingDTO);

    RatingDTO findById(Long id);

    List<RatingDTO> findAll();

    RatingDTO findByReservation(Long reservationId);

    void delete(Long id);
}