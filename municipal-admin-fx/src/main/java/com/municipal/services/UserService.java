package com.municipal.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.UserDTO;

import java.util.List;
import java.util.Objects;

/**
 * Service that retrieves user data from the backend API.
 */
public class UserService {

    private static final TypeReference<List<UserDTO>> USER_LIST_TYPE = new TypeReference<>() {
    };

    private final ApiClient apiClient;

    public UserService() {
        this(new ApiClient());
    }

    public UserService(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    public List<UserDTO> findAll(String bearerToken) {
        return apiClient.get("/api/users", bearerToken, USER_LIST_TYPE);
    }
}
