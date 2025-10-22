package finalprojectprogramming.project.services.space;

import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.models.enums.SpaceType;
import java.time.LocalDateTime;
import java.util.List;

public interface SpaceService {

    SpaceDTO create(SpaceDTO spaceDTO);

    SpaceDTO update(Long id, SpaceDTO spaceDTO);

    SpaceDTO findById(Long id);

    List<SpaceDTO> findAll();

    void delete(Long id);

    SpaceDTO changeStatus(Long id, boolean active);

    List<SpaceDTO> findAvailableSpaces(LocalDateTime startTime, LocalDateTime endTime,
            SpaceType type, Integer minimumCapacity);
}