package finalprojectprogramming.project.services.spaceschedule;

import finalprojectprogramming.project.dtos.SpaceScheduleDTO;
import java.util.List;

public interface SpaceScheduleService {

    SpaceScheduleDTO create(SpaceScheduleDTO spaceScheduleDTO);

    SpaceScheduleDTO update(Long id, SpaceScheduleDTO spaceScheduleDTO);

    SpaceScheduleDTO findById(Long id);

    List<SpaceScheduleDTO> findAll();

    List<SpaceScheduleDTO> findBySpace(Long spaceId);

    void delete(Long id);
}