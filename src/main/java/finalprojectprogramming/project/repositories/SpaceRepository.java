package finalprojectprogramming.project.repositories;

import finalprojectprogramming.project.models.Space;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {
}