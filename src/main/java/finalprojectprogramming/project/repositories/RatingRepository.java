package finalprojectprogramming.project.repositories;

import finalprojectprogramming.project.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    Optional<Rating> findByReservationId(Long reservationId);

    List<Rating> findBySpaceIdAndVisibleTrueOrderByCreatedAtDesc(Long spaceId);

    List<Rating> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Rating> findAllByOrderByCreatedAtDesc();

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.space.id = :spaceId AND r.visible = true")
    Double getAverageScoreBySpaceId(@Param("spaceId") Long spaceId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.space.id = :spaceId AND r.visible = true")
    Long getRatingCountBySpaceId(@Param("spaceId") Long spaceId);
}