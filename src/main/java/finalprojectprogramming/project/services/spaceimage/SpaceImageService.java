package finalprojectprogramming.project.services.spaceimage;

import finalprojectprogramming.project.dtos.SpaceImageDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface SpaceImageService {

    SpaceImageDTO create(SpaceImageDTO spaceImageDTO);

    SpaceImageDTO update(Long id, SpaceImageDTO spaceImageDTO);

    SpaceImageDTO findById(Long id);

    List<SpaceImageDTO> findAll();

    List<SpaceImageDTO> findBySpace(Long spaceId);

    SpaceImageDTO upload(Long spaceId, MultipartFile file, String description, Boolean active, Integer displayOrder);

    void delete(Long id);
}