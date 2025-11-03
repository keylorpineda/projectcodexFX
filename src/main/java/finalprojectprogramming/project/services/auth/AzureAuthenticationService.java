package finalprojectprogramming.project.services.auth;

import finalprojectprogramming.project.dtos.AuthResponseDTO;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.UserRepository;
import finalprojectprogramming.project.security.jwt.JwtService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class AzureAuthenticationService {

    private final AzureGraphClient azureGraphClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AzureAuthenticationService(
            AzureGraphClient azureGraphClient,
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.azureGraphClient = azureGraphClient;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates user synchronously (for backward compatibility)
     */
    public AuthResponseDTO authenticate(String accessToken) {
        return authenticateAsync(accessToken).join();
    }

    /**
     * Authenticates user asynchronously for better performance
     */
    public CompletableFuture<AuthResponseDTO> authenticateAsync(String accessToken) {
        // Fetch user info from Microsoft Graph asynchronously
        return azureGraphClient.fetchCurrentUserAsync(accessToken)
            .thenCompose(azureUser -> {
                if (!azureUser.hasEmail()) {
                    return CompletableFuture.failedFuture(
                        new BadCredentialsException("Azure account must expose an email address")
                    );
                }

                // Process user in database asynchronously
                return CompletableFuture.supplyAsync(() -> processUserAuthentication(azureUser));
            });
    }

    private AuthResponseDTO processUserAuthentication(AzureUserInfo azureUser) {
        String email = azureUser.email();
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null && Boolean.FALSE.equals(user.getActive())) {
            throw new BadCredentialsException("User account is disabled");
        }
        
        boolean newUser = false;
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setActive(true);
            user.setRole(UserRole.USER);
            user.setCreatedAt(now);
            user.setReservations(new ArrayList<>());
            user.setApprovedReservations(new ArrayList<>());
            user.setAuditLogs(new ArrayList<>());
            newUser = true;
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }

        if ((user.getName() == null || user.getName().isBlank()) && azureUser.hasDisplayName()) {
            user.setName(azureUser.displayName());
        }

        user.setLastLoginAt(now);
        user.setUpdatedAt(now);

        User saved = userRepository.save(user);

        boolean profileComplete = saved.getName() != null && !saved.getName().isBlank();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("uid", saved.getId());
        extraClaims.put("role", saved.getRole() != null ? saved.getRole().name() : null);
        extraClaims.put("email", saved.getEmail());
        extraClaims.put("profileComplete", profileComplete);
        extraClaims.put("newUser", newUser);
        if (saved.getName() != null && !saved.getName().isBlank()) {
            extraClaims.put("name", saved.getName());
        }

        String token = jwtService.generateToken(extraClaims, saved.getEmail());
        Date expirationDate = jwtService.extractExpiration(token);
        Instant expiresAt = expirationDate != null ? expirationDate.toInstant() : null;

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .userId(saved.getId())
                .role(saved.getRole() != null ? saved.getRole().name() : null)
                .email(saved.getEmail())
                .name(saved.getName())
                .profileComplete(profileComplete)
                .newUser(newUser)
                .build();
    }
}