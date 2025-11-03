package com.municipal.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.SpaceDTO;
import com.municipal.dtos.SpaceInputDTO;

import java.util.List;
import java.util.Objects;

/**
 * Service that wraps calls to the space management endpoints exposed by the
 * backend API.
 */
public class SpaceService {

    private static final TypeReference<List<SpaceDTO>> SPACE_LIST_TYPE = new TypeReference<>() {
    };

    private final ApiClient apiClient;

    public SpaceService() {
        this(new ApiClient());
    }

    public SpaceService(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    public List<SpaceDTO> findAll(String bearerToken) {
        return apiClient.get("/api/spaces", bearerToken, SPACE_LIST_TYPE);
    }

    public SpaceDTO create(SpaceInputDTO input, String bearerToken) {
        Objects.requireNonNull(input, "input");
        return apiClient.post("/api/spaces", input, bearerToken, SpaceDTO.class);
    }

    public SpaceDTO update(Long id, SpaceInputDTO input, String bearerToken) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(input, "input");
        return apiClient.put("/api/spaces/" + id, input, bearerToken, SpaceDTO.class);
    }

    public SpaceDTO changeStatus(Long id, boolean active, String bearerToken) {
        Objects.requireNonNull(id, "id");
        String path = "/api/spaces/" + id + "/status?active=" + active;
        return apiClient.patch(path, null, bearerToken, SpaceDTO.class);
    }

    public void delete(Long id, String bearerToken) {
        Objects.requireNonNull(id, "id");
        apiClient.delete("/api/spaces/" + id, bearerToken);
    }

    public List<SpaceDTO> findAvailableSpaces(String startTime, String endTime, String bearerToken) {
        String path = "/api/spaces/available?startTime=" + startTime + "&endTime=" + endTime;
        return apiClient.get(path, bearerToken, SPACE_LIST_TYPE);
    }
    
    /**
     * Busca espacios con filtros avanzados usando el endpoint /api/spaces/search.
     * Todos los parámetros son opcionales.
     */
    public List<SpaceDTO> searchSpaces(String type, Integer minCapacity, Integer maxCapacity, 
                                       String location, Boolean active, String bearerToken) {
        StringBuilder path = new StringBuilder("/api/spaces/search?");
        boolean firstParam = true;
        
        if (type != null && !type.isBlank()) {
            path.append("type=").append(type);
            firstParam = false;
        }
        
        if (minCapacity != null) {
            if (!firstParam) path.append("&");
            path.append("minCapacity=").append(minCapacity);
            firstParam = false;
        }
        
        if (maxCapacity != null) {
            if (!firstParam) path.append("&");
            path.append("maxCapacity=").append(maxCapacity);
            firstParam = false;
        }
        
        if (location != null && !location.isBlank()) {
            if (!firstParam) path.append("&");
            path.append("location=").append(location);
            firstParam = false;
        }
        
        if (active != null) {
            if (!firstParam) path.append("&");
            path.append("active=").append(active);
        }
        
        return apiClient.get(path.toString(), bearerToken, SPACE_LIST_TYPE);
    }
}
