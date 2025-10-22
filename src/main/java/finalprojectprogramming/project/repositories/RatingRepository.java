package finalprojectprogramming.project.repositories;

import finalprojectprogramming.project.models.Rating;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByReservationId(Long reservationId);
}