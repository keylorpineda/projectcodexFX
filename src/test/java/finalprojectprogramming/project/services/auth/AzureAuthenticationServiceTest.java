package finalprojectprogramming.project.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import finalprojectprogramming.project.dtos.AuthResponseDTO;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.UserRepository;
import finalprojectprogramming.project.security.jwt.JwtService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class AzureAuthenticationServiceTest {

    private static final String ACCESS_TOKEN = "azure-token";
    private static final String EMAIL = "person@example.com";

    @Mock
    private AzureGraphClient azureGraphClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AzureAuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AzureAuthenticationService(azureGraphClient, userRepository, jwtService);
    }

    @Test
    void authenticateReturnsTokenForExistingActiveUser() {
        User existingUser = buildUser(5L, "Existing User", true);
        AzureUserInfo azureUserInfo = new AzureUserInfo("id-123", EMAIL, "Existing User");
    // Compatibilidad con flujo asíncrono usado por el servicio
        when(azureGraphClient.fetchCurrentUserAsync(ACCESS_TOKEN))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(azureUserInfo));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        Date expiration = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
    when(jwtService.generateToken(org.mockito.ArgumentMatchers.<Map<String, Object>>any(), eq(EMAIL))).thenReturn("jwt-token");
        when(jwtService.extractExpiration("jwt-token")).thenReturn(expiration);

        AuthResponseDTO response = service.authenticate(ACCESS_TOKEN);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getUserId()).isEqualTo(existingUser.getId());
        assertThat(response.isNewUser()).isFalse();
        assertThat(response.isProfileComplete()).isTrue();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresAt()).isEqualTo(expiration.toInstant());
        assertThat(response.getRole()).isEqualTo(existingUser.getRole().name());
        assertThat(response.getName()).isEqualTo(existingUser.getName());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> claimsCaptor = (ArgumentCaptor<Map<String, Object>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Map.class);
        verify(jwtService).generateToken(claimsCaptor.capture(), eq(EMAIL));
        Map<String, Object> claims = claimsCaptor.getValue();
        assertThat(claims.get("uid")).isEqualTo(existingUser.getId());
        assertThat(claims.get("newUser")).isEqualTo(false);
        assertThat(claims.get("profileComplete")).isEqualTo(true);
        assertThat(claims.get("name")).isEqualTo(existingUser.getName());
        assertThat(claims.get("role")).isEqualTo(existingUser.getRole().name());

        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void authenticateCreatesNewUserWhenNotFound() {
        AzureUserInfo azureUserInfo = new AzureUserInfo("id-456", EMAIL, "Azure Name");
    // Compatibilidad con flujo asíncrono
        when(azureGraphClient.fetchCurrentUserAsync(ACCESS_TOKEN))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(azureUserInfo));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User persisted = invocation.getArgument(0);
            persisted.setId(77L);
            return persisted;
        });
    when(jwtService.generateToken(org.mockito.ArgumentMatchers.<Map<String, Object>>any(), eq(EMAIL))).thenReturn("jwt-token");
        when(jwtService.extractExpiration("jwt-token")).thenReturn(null);

        AuthResponseDTO response = service.authenticate(ACCESS_TOKEN);

        assertThat(response.getUserId()).isEqualTo(77L);
        assertThat(response.isNewUser()).isTrue();
        assertThat(response.isProfileComplete()).isTrue();
        assertThat(response.getName()).isEqualTo("Azure Name");
        assertThat(response.getExpiresAt()).isNull();

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getName()).isEqualTo("Azure Name");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getLastLoginAt()).isNotNull();
        assertThat(saved.getReservations()).isNotNull();
        assertThat(saved.getApprovedReservations()).isNotNull();
        assertThat(saved.getAuditLogs()).isNotNull();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> claimsCaptor = (ArgumentCaptor<Map<String, Object>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Map.class);
        verify(jwtService).generateToken(claimsCaptor.capture(), eq(EMAIL));
        assertThat(claimsCaptor.getValue().get("newUser")).isEqualTo(true);
        assertThat(claimsCaptor.getValue().get("profileComplete")).isEqualTo(true);
    }

    @Test
    void authenticateThrowsWhenUserDisabled() {
        User disabled = buildUser(10L, "Disabled", false);
        AzureUserInfo azureUserInfo = new AzureUserInfo("id-789", EMAIL, "Disabled");
    when(azureGraphClient.fetchCurrentUserAsync(ACCESS_TOKEN))
        .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(azureUserInfo));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(disabled));

    Throwable thrown1 = org.assertj.core.api.Assertions.catchThrowable(() -> service.authenticate(ACCESS_TOKEN));
    assertThat(thrown1).isInstanceOf(java.util.concurrent.CompletionException.class);
    assertThat(thrown1.getCause()).isInstanceOf(BadCredentialsException.class);
    assertThat(thrown1.getCause()).hasMessageContaining("disabled");

        verify(userRepository, times(0)).save(any());
        verify(jwtService, times(0)).generateToken(any(), any());
    }

    @Test
    void authenticateThrowsWhenAzureDoesNotProvideEmail() {
        AzureUserInfo azureUserInfo = new AzureUserInfo("id-000", null, "Someone");
    when(azureGraphClient.fetchCurrentUserAsync(ACCESS_TOKEN))
        .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(azureUserInfo));

    Throwable thrown2 = org.assertj.core.api.Assertions.catchThrowable(() -> service.authenticate(ACCESS_TOKEN));
    assertThat(thrown2).isInstanceOf(java.util.concurrent.CompletionException.class);
    assertThat(thrown2.getCause()).isInstanceOf(BadCredentialsException.class);
    assertThat(thrown2.getCause()).hasMessageContaining("email address");

        verify(userRepository, times(0)).findByEmail(any());
    }

    @Test
    void authenticatePropagatesClientErrors() {
    // Asegurar que el flujo asíncrono también refleja el error esperado
    when(azureGraphClient.fetchCurrentUserAsync(ACCESS_TOKEN))
        .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new IllegalStateException("boom")));

    Throwable thrown3 = org.assertj.core.api.Assertions.catchThrowable(() -> service.authenticate(ACCESS_TOKEN));
    assertThat(thrown3).isInstanceOf(java.util.concurrent.CompletionException.class);
    assertThat(thrown3.getCause()).isInstanceOf(IllegalStateException.class);
    assertThat(thrown3.getCause()).hasMessageContaining("boom");
    }

    private static User buildUser(Long id, String name, boolean active) {
        User user = new User();
        user.setId(id);
        user.setEmail(EMAIL);
        user.setName(name);
        user.setActive(active);
        user.setRole(UserRole.ADMIN);
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now().minusHours(2));
        return user;
    }

    @Test
    void authenticateFillsMissingRoleAndCreatedAtForExistingUser() {
        // Existing user without role and createdAt; name blank so it should be set from Azure display name
        User existing = new User();
        existing.setId(42L);
        existing.setEmail(EMAIL);
        existing.setActive(true);
        existing.setRole(null);
        existing.setCreatedAt(null);
        existing.setName(" ");

        AzureUserInfo azureUserInfo = new AzureUserInfo("id-xyz", EMAIL, "Azure Display");
        when(azureGraphClient.fetchCurrentUserAsync(ACCESS_TOKEN))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(azureUserInfo));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(jwtService.generateToken(org.mockito.ArgumentMatchers.<Map<String, Object>>any(), eq(EMAIL))).thenReturn("tkn");
        when(jwtService.extractExpiration("tkn")).thenReturn(null);

        AuthResponseDTO response = service.authenticate(ACCESS_TOKEN);

        // Role and createdAt should have been filled, and name set from Azure display name
        assertThat(existing.getRole()).isEqualTo(UserRole.USER);
        assertThat(existing.getCreatedAt()).isNotNull();
        assertThat(existing.getName()).isEqualTo("Azure Display");
        assertThat(response.getRole()).isEqualTo(UserRole.USER.name());
        assertThat(response.getName()).isEqualTo("Azure Display");
    }

    @Test
    void authenticate_existing_user_without_name_and_azure_without_displayName_results_profileIncomplete_and_no_name_claim() {
        // Existing user with blank name and no displayName from Azure
        User existing = new User();
        existing.setId(101L);
        existing.setEmail(EMAIL);
        existing.setActive(true);
        existing.setRole(UserRole.USER);
        existing.setCreatedAt(LocalDateTime.now().minusDays(2));
        existing.setName(" ");

        AzureUserInfo azureUserInfo = new AzureUserInfo("id-no-name", EMAIL, " ");
        when(azureGraphClient.fetchCurrentUserAsync(ACCESS_TOKEN))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(azureUserInfo));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(jwtService.generateToken(org.mockito.ArgumentMatchers.<Map<String, Object>>any(), eq(EMAIL)))
                .thenReturn("tkn2");
        when(jwtService.extractExpiration("tkn2")).thenReturn(null);

        AuthResponseDTO response = service.authenticate(ACCESS_TOKEN);

        assertThat(response.isNewUser()).isFalse();
        assertThat(response.isProfileComplete()).isFalse();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> claimsCaptor = (ArgumentCaptor<Map<String, Object>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Map.class);
        verify(jwtService).generateToken(claimsCaptor.capture(), eq(EMAIL));
        Map<String, Object> claims = claimsCaptor.getValue();
        // name claim should NOT be present when profile is incomplete
        assertThat(claims).doesNotContainKey("name");
        assertThat(claims.get("profileComplete")).isEqualTo(false);
    }
}
