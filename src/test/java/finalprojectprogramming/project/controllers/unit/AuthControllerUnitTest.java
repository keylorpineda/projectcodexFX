package finalprojectprogramming.project.controllers.unit;

import finalprojectprogramming.project.controllers.AuthController;
import finalprojectprogramming.project.dtos.AuthResponseDTO;
import finalprojectprogramming.project.dtos.AzureLoginRequestDTO;
import finalprojectprogramming.project.services.auth.AzureAuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AzureAuthenticationService azureAuthenticationService;

    @InjectMocks
    private AuthController controller;

    @Test
    void azureLoginReturnsOkWithToken() {
        AzureLoginRequestDTO request = AzureLoginRequestDTO.builder().accessToken("valid-token").build();
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt")
                .tokenType("Bearer")
                .expiresAt(Instant.parse("2025-01-01T00:00:00Z"))
                .userId(5L)
                .role("ADMIN")
                .email("user@example.com")
                .name("User")
                .profileComplete(true)
                .newUser(false)
                .build();
        when(azureAuthenticationService.authenticate("valid-token")).thenReturn(response);

        ResponseEntity<AuthResponseDTO> resp = controller.azureLogin(request);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isEqualTo(response);
        verify(azureAuthenticationService).authenticate(eq("valid-token"));
    }
}
