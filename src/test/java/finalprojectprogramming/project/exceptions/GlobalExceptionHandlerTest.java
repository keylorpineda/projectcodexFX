package finalprojectprogramming.project.exceptions;

import finalprojectprogramming.project.exceptions.api.ApiError;
import finalprojectprogramming.project.exceptions.openWeather.RateLimitExceededException;
import finalprojectprogramming.project.exceptions.openWeather.WeatherProviderException;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import finalprojectprogramming.project.models.enums.UserRole;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.ObjectError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

 

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
        // Simulamos errores de validación BindingResult (field + global)
        org.springframework.validation.BeanPropertyBindingResult bind = new org.springframework.validation.BeanPropertyBindingResult(new Object(), "t");
        bind.addError(new org.springframework.validation.FieldError("t", "field", "must not be blank."));
        bind.addError(new ObjectError("t", "global error."));

        var manve = new org.springframework.web.bind.MethodArgumentNotValidException(null, bind);
        ResponseEntity<ApiError> res = handler.handleMethodArgumentNotValid(manve, request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(res.getBody().getValidationErrors()).hasSize(2);
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

    @Test
    void badRequest_variants_and_request_ids() throws Exception {
        // HttpMessageNotReadable with InvalidFormatException on enum → mensaje incluye valores permitidos
    InvalidFormatException inv = InvalidFormatException.from(null, "bad", "X", UserRole.class);
    HttpMessageNotReadableException hmr = new HttpMessageNotReadableException("", inv, null);
        ResponseEntity<ApiError> r1 = handler.handleBadRequest(hmr, request);
        assertThat(r1.getBody()).isNotNull();
        assertThat(r1.getBody().getMessage()).contains("Allowed values");

        // Mapping exception con path
    JsonMappingException jm = new JsonMappingException((com.fasterxml.jackson.core.JsonParser) null, "msg");
    jm.prependPath("obj", 0);
    jm.prependPath("obj", "items");
    HttpMessageNotReadableException hmr2 = new HttpMessageNotReadableException("", jm, null);
        ResponseEntity<ApiError> r2 = handler.handleBadRequest(hmr2, request);
        assertThat(r2.getBody()).isNotNull();
        assertThat(r2.getBody().getMessage()).contains("Malformed request payload");

        // Missing parameter
        MissingServletRequestParameterException miss = new MissingServletRequestParameterException("q", "String");
        ResponseEntity<ApiError> r3 = handler.handleBadRequest(miss, request);
        assertThat(r3.getBody()).isNotNull();
        assertThat(r3.getBody().getMessage()).contains("Missing required parameter");

        // Type mismatch (mocked para evitar construir MethodParameter)
        MethodArgumentTypeMismatchException mismatch = mock(MethodArgumentTypeMismatchException.class);
    when(mismatch.getRequiredType()).thenAnswer(x -> Integer.class);
        when(mismatch.getName()).thenReturn("age");
        ResponseEntity<ApiError> r4 = handler.handleBadRequest(mismatch, request);
        assertThat(r4.getBody()).isNotNull();
        assertThat(r4.getBody().getMessage()).contains("age");

        // Request IDs
        HttpServletRequest withIds = mock(HttpServletRequest.class);
        when(withIds.getRequestURI()).thenReturn("/x");
        when(withIds.getHeader("X-Request-Id")).thenReturn("RID");
        ResponseEntity<ApiError> r5 = handler.handleGenericException(new RuntimeException("x"), withIds);
        assertThat(r5.getBody()).isNotNull();
        assertThat(r5.getBody().getRequestId()).isEqualTo("RID");
    }
}
