package com.municipal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.config.AppConfig;
import com.municipal.exceptions.ApiClientException;
import com.municipal.utils.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/**
 * Lightweight HTTP client used by the JavaFX application to communicate with
 * the Spring Boot backend.
 */
public class ApiClient {

    private final HttpClient httpClient;
    private final String baseUrl;

    public ApiClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build(),
                AppConfig.require("api.base-url"));
    }

    public ApiClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
    }

    public HttpRequest.Builder requestBuilder(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + normalizedPath))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(body)))
                .build();
        return send(request, responseType);
    }

    public <T> T post(String path, Object body, String bearerToken, Class<T> responseType) {
        HttpRequest.Builder builder = authorizedBuilder(path, bearerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(body)));
        return send(builder.build(), responseType);
    }

    public <T> T put(String path, Object body, String bearerToken, Class<T> responseType) {
        HttpRequest.Builder builder = authorizedBuilder(path, bearerToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(body)));
        return send(builder.build(), responseType);
    }

    public <T> T patch(String path, Object body, String bearerToken, Class<T> responseType) {
        HttpRequest.BodyPublisher publisher = body != null
                ? HttpRequest.BodyPublishers.ofString(JsonUtils.toJson(body))
                : HttpRequest.BodyPublishers.noBody();
        HttpRequest.Builder builder = authorizedBuilder(path, bearerToken)
                .header("Content-Type", "application/json")
                .method("PATCH", publisher);
        return send(builder.build(), responseType);
    }

    public void delete(String path, String bearerToken) {
        HttpRequest.Builder builder = authorizedBuilder(path, bearerToken)
                .DELETE();
        send(builder.build(), Void.class);
    }

    public <T> T get(String path, String bearerToken, TypeReference<T> responseType) {
        HttpRequest.Builder builder = requestBuilder(path).GET();
        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return send(builder.build(), responseType);
    }

    private HttpRequest.Builder authorizedBuilder(String path, String bearerToken) {
        HttpRequest.Builder builder = requestBuilder(path);
        if (bearerToken != null && !bearerToken.isBlank()) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }
        return builder;
    }

    public <T> T send(HttpRequest request, Class<T> responseType) {
        HttpResponse<String> response = execute(request);
        if (responseType == Void.class) {
            return null;
        }
        return JsonUtils.fromJson(response.body(), responseType);
    }

    public <T> T send(HttpRequest request, TypeReference<T> responseType) {
        HttpResponse<String> response = execute(request);
        return JsonUtils.fromJson(response.body(), responseType);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private HttpResponse<String> execute(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return response;
            }
            throw new ApiClientException(statusCode, response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiClientException(500, "Request execution interrupted", exception);
        } catch (IOException exception) {
            throw new ApiClientException(500, "Failed to execute request", exception);
        }
    }
}
