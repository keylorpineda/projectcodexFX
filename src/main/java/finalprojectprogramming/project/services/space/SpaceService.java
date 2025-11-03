package finalprojectprogramming.project.services.space;

import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.models.enums.SpaceType;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public interface SpaceService {

    SpaceDTO create(SpaceDTO spaceDTO);

    SpaceDTO createWithImage(SpaceDTO spaceDTO, MultipartFile image);

    SpaceDTO update(Long id, SpaceDTO spaceDTO);

    SpaceDTO findById(Long id);

    List<SpaceDTO> findAll();

    void delete(Long id);

    SpaceDTO changeStatus(Long id, boolean active);

    List<SpaceDTO> findAvailableSpaces(LocalDateTime startTime, LocalDateTime endTime,
            SpaceType type, Integer minimumCapacity);

    /**
     * Búsqueda avanzada de espacios con múltiples filtros
     * 
     * @param type Tipo de espacio (opcional)
     * @param minCapacity Capacidad mínima (opcional)
     * @param maxCapacity Capacidad máxima (opcional)
     * @param location Ubicación (búsqueda parcial, opcional)
     * @param active Estado activo/inactivo (opcional)
     * @return Lista de espacios que cumplen los criterios
     */
    List<SpaceDTO> searchSpaces(SpaceType type, Integer minCapacity, Integer maxCapacity, 
                                 String location, Boolean active);
}