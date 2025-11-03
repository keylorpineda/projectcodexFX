package com.municipal.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.SpaceImageDTO;
import com.municipal.exceptions.ApiClientException;
import com.municipal.utils.MultipartBodyPublisher;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class SpaceImageService {

    private static final TypeReference<List<SpaceImageDTO>> LIST_TYPE = new TypeReference<>() {
    };

    private final ApiClient apiClient;

    public SpaceImageService() {
        this(new ApiClient());
    }

    public SpaceImageService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<SpaceImageDTO> findBySpace(Long spaceId, String bearerToken) {
        return apiClient.get("/api/space-images/space/" + spaceId, bearerToken, LIST_TYPE);
    }

    public SpaceImageDTO upload(Long spaceId, Path file, String description, Integer displayOrder, Boolean active,
            String bearerToken) {
        try {
            byte[] data = Files.readAllBytes(file);
            String contentType = Files.probeContentType(file);
            MultipartBodyPublisher multipart = MultipartBodyPublisher.newBuilder()
                    .addPart("spaceId", String.valueOf(spaceId))
                    .addPart("description", description != null && !description.isBlank() ? description : null)
                    .addPart("displayOrder", displayOrder != null ? String.valueOf(displayOrder) : null)
                    .addPart("active", active != null ? String.valueOf(active) : null)
                    .addBinaryPart("file", file.getFileName().toString(),
                            contentType != null ? contentType : "application/octet-stream", data);
            return apiClient.postMultipart("/api/space-images/upload", multipart, bearerToken, SpaceImageDTO.class);
        } catch (IOException exception) {
            throw new ApiClientException(500, "Failed to read file for upload", exception);
        }
    }

    public void delete(Long imageId, String bearerToken) {
        apiClient.delete("/api/space-images/" + imageId, bearerToken);
    }

    public byte[] downloadImage(Long imageId, String bearerToken) {
        try {
            SpaceImageDTO image = apiClient.get("/api/space-images/" + imageId, bearerToken, 
                new TypeReference<SpaceImageDTO>() {});
            if (image == null || image.imageUrl() == null || image.imageUrl().isBlank()) {
                return null;
            }

            String imageUrl = resolveImageUrl(image.imageUrl());
            
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET();
            
            if (bearerToken != null && !bearerToken.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + bearerToken);
            }

            HttpResponse<byte[]> response = apiClient.getHttpClient().send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            
            throw new ApiClientException(response.statusCode(), "Failed to download image");
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ApiClientException(500, "Failed to download image: " + e.getMessage(), e);
        }
    }

    public String resolveImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        if (imageUrl.startsWith("/")) {
            return apiClient.getBaseUrl() + imageUrl;
        }
        return apiClient.getBaseUrl() + "/" + imageUrl;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }
}
