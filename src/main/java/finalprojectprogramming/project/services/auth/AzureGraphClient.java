package finalprojectprogramming.project.services.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class AzureGraphClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureGraphClient.class);
    private static final String GRAPH_ME_ENDPOINT =
            "https://graph.microsoft.com/v1.0/me?$select=id,displayName,mail,userPrincipalName";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AzureGraphClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches current user info from Microsoft Graph synchronously (for backward compatibility)
     */
    public AzureUserInfo fetchCurrentUser(String accessToken) {
        try {
            return fetchCurrentUserAsync(accessToken).join();
        } catch (Exception ex) {
            if (ex.getCause() instanceof BadCredentialsException) {
                throw (BadCredentialsException) ex.getCause();
            }
            if (ex.getCause() instanceof BusinessRuleException) {
                throw (BusinessRuleException) ex.getCause();
            }
            throw new IllegalStateException("Unable to fetch user from Microsoft Graph", ex);
        }
    }

    /**
     * Fetches current user info from Microsoft Graph asynchronously
     */
    public CompletableFuture<AzureUserInfo> fetchCurrentUserAsync(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return CompletableFuture.failedFuture(
                new BadCredentialsException("Azure access token is required")
            );
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GRAPH_ME_ENDPOINT))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int statusCode = response.statusCode();
                    
                    if (statusCode == 401 || statusCode == 403) {
                        LOGGER.warn("Microsoft Graph rejected the provided Azure token with status {}", statusCode);
                        throw new BadCredentialsException("Invalid Azure access token");
                    }

                    if (statusCode >= 400) {
                        LOGGER.error("Unexpected response from Microsoft Graph. Status: {}, Body: {}", 
                            statusCode, response.body());
                        throw new BusinessRuleException("Microsoft Graph rejected the authentication attempt");
                    }

                    try {
                        JsonNode payload = objectMapper.readTree(response.body());
                        String id = readText(payload, "id");
                        String displayName = readText(payload, "displayName");
                        String email = readText(payload, "mail");
                        if (email == null || email.isBlank()) {
                            email = readText(payload, "userPrincipalName");
                        }

                        AzureUserInfo info = new AzureUserInfo(id, email, displayName);
                        if (!info.hasEmail()) {
                            throw new BadCredentialsException("Azure user does not expose an email address");
                        }
                        return info;
                    } catch (IOException ex) {
                        throw new IllegalStateException("Unable to parse Microsoft Graph response", ex);
                    }
                })
                .exceptionally(throwable -> {
                    // Re-throw business exceptions
                    if (throwable instanceof BadCredentialsException 
                        || throwable instanceof BusinessRuleException) {
                        throw (RuntimeException) throwable;
                    }
                    if (throwable.getCause() instanceof BadCredentialsException
                        || throwable.getCause() instanceof BusinessRuleException) {
                        throw (RuntimeException) throwable.getCause();
                    }
                    LOGGER.error("Error fetching user from Microsoft Graph", throwable);
                    throw new IllegalStateException("Unable to contact Microsoft Graph", throwable);
                });
    }

    private String readText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text != null && !text.isBlank() ? text : null;
    }
}
