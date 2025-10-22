package finalprojectprogramming.project.services.spaceimage;

import finalprojectprogramming.project.dtos.SpaceImageDTO;
import java.util.List;

public interface SpaceImageService {

    SpaceImageDTO create(SpaceImageDTO spaceImageDTO);

    SpaceImageDTO update(Long id, SpaceImageDTO spaceImageDTO);

    SpaceImageDTO findById(Long id);

    List<SpaceImageDTO> findAll();

    List<SpaceImageDTO> findBySpace(Long spaceId);

    void delete(Long id);
}