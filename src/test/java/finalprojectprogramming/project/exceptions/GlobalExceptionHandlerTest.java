package finalprojectprogramming.project.exceptions;

import finalprojectprogramming.project.exceptions.api.ApiError;
import finalprojectprogramming.project.exceptions.openWeather.RateLimitExceededException;
import finalprojectprogramming.project.exceptions.openWeather.WeatherProviderException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/uri");
        when(request.getHeader(anyString())).thenReturn(null);
    }

    @Test
    void resourceNotFound() {
        ResponseEntity<ApiError> res = handler.handleResourceNotFound(new ResourceNotFoundException("nf"), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getMessage()).contains("nf");
    }

    @Test
    void businessRule422() {
        ResponseEntity<ApiError> res = handler.handleBusinessRuleException(new BusinessRuleException("rule"), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void invalidPassword400() {
        ResponseEntity<ApiError> res = handler.handleInvalidPasswordException(new InvalidPasswordException("bad"), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void methodArgumentNotValid400() {
        // Direct construction of MethodArgumentNotValidException es engorroso.
        // Validamos la ruta de ConstraintViolationException que también mapea a VALIDATION_ERROR.
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = new Path() {
            @Override
            public java.util.Iterator<Node> iterator() {
                return java.util.Collections.emptyIterator();
            }
            @Override
            public String toString() { return "key"; }
        };
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiError> res = handler.handleConstraintViolation(ex, request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
    assertThat(res.getBody().getValidationErrors()).extracting("field").contains("key");
    }

    @Test
    void badCredentials401() {
        ResponseEntity<ApiError> res = handler.handleAuthentication(new BadCredentialsException("x"), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getMessage()).contains("Invalid credentials");
    }

    @Test
    void authenticationGeneric401() {
        AuthenticationException ex = new AuthenticationException("") {};
        ResponseEntity<ApiError> res = handler.handleAuthentication(ex, request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void accessDenied403() {
        ResponseEntity<ApiError> res = handler.handleAccessDenied(new org.springframework.security.access.AccessDeniedException("") , request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void dataIntegrity409() {
        ResponseEntity<ApiError> res = handler.handleDataIntegrityViolation(new org.springframework.dao.DataIntegrityViolationException("dup"), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void illegalArgument400() {
        ResponseEntity<ApiError> res = handler.handleIllegalArgument(new IllegalArgumentException("bad"), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rateLimit429() {
        ResponseEntity<ApiError> res = handler.handleRateLimit(new RateLimitExceededException("rl"), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void weatherProviderCustomStatus() {
        WeatherProviderException ex = new WeatherProviderException(HttpStatus.BAD_GATEWAY, "UPSTREAM", "down");
        ResponseEntity<ApiError> res = handler.handleWeatherProvider(ex, request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getCode()).isEqualTo("UPSTREAM");
    }

    @Test
    void generic500() {
        ResponseEntity<ApiError> res = handler.handleGenericException(new RuntimeException("boom"), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
