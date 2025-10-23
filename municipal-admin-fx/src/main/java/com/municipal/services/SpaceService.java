package com.municipal.reservationsfx.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.reservationsfx.backend.ApiClient;
import com.municipal.reservationsfx.backend.dtos.SpaceDTO;

import java.util.List;

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
        this.apiClient = apiClient;
    }

    public List<SpaceDTO> findAll(String bearerToken) {
        return apiClient.get("/api/spaces", bearerToken, SPACE_LIST_TYPE);
    }
}
