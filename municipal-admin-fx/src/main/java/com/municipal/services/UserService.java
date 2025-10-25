package com.municipal.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.UserDTO;
import com.municipal.dtos.UserInputDTO;

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

    public UserDTO create(UserInputDTO input, String bearerToken) {
        Objects.requireNonNull(input, "input");
        return apiClient.post("/api/users", input, bearerToken, UserDTO.class);
    }

    public UserDTO update(Long id, UserInputDTO input, String bearerToken) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(input, "input");
        return apiClient.put("/api/users/" + id, input, bearerToken, UserDTO.class);
    }

    public void delete(Long id, String bearerToken) {
        Objects.requireNonNull(id, "id");
        apiClient.delete("/api/users/" + id, bearerToken);
    }
}
