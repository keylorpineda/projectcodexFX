package finalprojectprogramming.project.repositories;

import finalprojectprogramming.project.models.SpaceSchedule;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceScheduleRepository extends JpaRepository<SpaceSchedule, Long> {
    List<SpaceSchedule> findBySpaceId(Long spaceId);
    Optional<SpaceSchedule> findBySpaceIdAndDayOfWeek(Long spaceId, DayOfWeek dayOfWeek);
}