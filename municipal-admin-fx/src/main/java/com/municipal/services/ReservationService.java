package com.municipal.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.ReservationDTO;

import java.util.List;
import java.util.Objects;

/**
 * Service used by the JavaFX application to request reservation data from the backend.
 */
public class ReservationService {

    private static final TypeReference<List<ReservationDTO>> RESERVATION_LIST_TYPE = new TypeReference<>() {
    };

    private final ApiClient apiClient;

    public ReservationService() {
        this(new ApiClient());
    }

    public ReservationService(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    public List<ReservationDTO> findAll(String bearerToken) {
        return apiClient.get("/api/reservations", bearerToken, RESERVATION_LIST_TYPE);
    }
}
