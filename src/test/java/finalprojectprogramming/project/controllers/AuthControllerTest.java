package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.AuthResponseDTO;
import finalprojectprogramming.project.dtos.AzureLoginRequestDTO;
import finalprojectprogramming.project.services.auth.AzureAuthenticationService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class AuthControllerTest extends BaseControllerTest {

    @MockBean
    private AzureAuthenticationService azureAuthenticationService;

    @Test
    void azureLoginReturnsTokenForValidRequest() throws Exception {
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

        performPost("/api/auth/azure-login", request)
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(azureAuthenticationService).authenticate(eq("valid-token"));
    }

    @Test
    void azureLoginFailsValidationWhenTokenMissing() throws Exception {
        AzureLoginRequestDTO request = AzureLoginRequestDTO.builder().accessToken(" ").build();

        performPost("/api/auth/azure-login", request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void azureLoginPropagatesInternalError() throws Exception {
        AzureLoginRequestDTO request = AzureLoginRequestDTO.builder().accessToken("invalid").build();
        when(azureAuthenticationService.authenticate("invalid")).thenThrow(new RuntimeException("boom"));

        performPost("/api/auth/azure-login", request)
                .andExpect(status().isInternalServerError());

        verify(azureAuthenticationService).authenticate(eq("invalid"));
    }
}
