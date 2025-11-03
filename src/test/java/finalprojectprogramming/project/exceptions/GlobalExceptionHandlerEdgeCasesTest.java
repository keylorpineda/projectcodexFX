package finalprojectprogramming.project.exceptions;

import finalprojectprogramming.project.exceptions.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerEdgeCasesTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resolveRequestId_falls_back_to_correlation_id_and_strips_trailing_dot() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader("X-Request-Id")).thenReturn(null);
        when(req.getHeader("X-Correlation-Id")).thenReturn("cid-123");
        when(req.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ApiError> response = handler.handleIllegalArgument(new IllegalArgumentException("Bad."), req);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRequestId()).isEqualTo("cid-123");
        assertThat(response.getBody().getMessage()).isEqualTo("Bad");
    }

    @Test
    void accessDenied_uses_default_message_when_blank() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(anyString())).thenReturn(null);
        when(req.getRequestURI()).thenReturn("/api/secure");

        ResponseEntity<ApiError> response = handler.handleAccessDenied(new org.springframework.security.access.AccessDeniedException(" "), req);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
    }

    @Test
    void authentication_uses_default_message_when_blank() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(anyString())).thenReturn(null);
        when(req.getRequestURI()).thenReturn("/api/login");

        org.springframework.security.core.AuthenticationException ex = new org.springframework.security.core.AuthenticationException("") {};

        ResponseEntity<ApiError> response = handler.handleAuthentication(ex, req);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Authentication failed");
    }
}
