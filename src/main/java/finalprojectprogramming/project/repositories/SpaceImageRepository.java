package finalprojectprogramming.project.repositories;

import finalprojectprogramming.project.models.SpaceImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceImageRepository extends JpaRepository<SpaceImage, Long> {
    List<SpaceImage> findBySpaceId(Long spaceId);
}