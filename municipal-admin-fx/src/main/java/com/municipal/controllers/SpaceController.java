package com.municipal.controllers;

import com.municipal.dtos.SpaceDTO;
import com.municipal.dtos.SpaceInputDTO;
import com.municipal.services.SpaceService;

import java.util.List;

/**
 * Simple facade to request data from {@link SpaceService} while keeping UI
 * controllers unaware of HTTP details.
 */
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController() {
        this(new SpaceService());
    }

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    public List<SpaceDTO> loadSpaces(String bearerToken) {
        return spaceService.findAll(bearerToken);
    }

    public SpaceDTO createSpace(SpaceInputDTO input, String bearerToken) {
        return spaceService.create(input, bearerToken);
    }

    public SpaceDTO updateSpace(Long id, SpaceInputDTO input, String bearerToken) {
        return spaceService.update(id, input, bearerToken);
    }

    public SpaceDTO changeStatus(Long id, boolean active, String bearerToken) {
        return spaceService.changeStatus(id, active, bearerToken);
    }

    public void deleteSpace(Long id, String bearerToken) {
        spaceService.delete(id, bearerToken);
    }

    public List<SpaceDTO> loadAvailableSpaces(String startTime, String endTime, String bearerToken) {
        return spaceService.findAvailableSpaces(startTime, endTime, bearerToken);
    }
    
    /**
     * Busca espacios con filtros avanzados.
     * 
     * @param type Tipo de espacio (opcional)
     * @param minCapacity Capacidad mínima (opcional)
     * @param maxCapacity Capacidad máxima (opcional)
     * @param location Ubicación parcial (opcional)
     * @param active Estado activo (opcional)
     * @param bearerToken Token de autenticación
     * @return Lista de espacios filtrados
     */
    public List<SpaceDTO> searchSpaces(String type, Integer minCapacity, Integer maxCapacity, 
                                       String location, Boolean active, String bearerToken) {
        return spaceService.searchSpaces(type, minCapacity, maxCapacity, location, active, bearerToken);
    }
}
