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
    void accessDenied_uses_default_message_when_null() {
        ResponseEntity<ApiError> res = handler.handleAccessDenied(new org.springframework.security.access.AccessDeniedException(null), request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getMessage()).isEqualTo("Access denied");
    }

    @Test
    void authentication_generic_uses_default_when_message_null() {
        AuthenticationException ex = new AuthenticationException(null) {};
        ResponseEntity<ApiError> res = handler.handleAuthentication(ex, request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getMessage()).isEqualTo("Authentication failed");
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

    // Mapping exception with empty path -> generic message (no 'near')
    JsonMappingException jmEmpty = new JsonMappingException((com.fasterxml.jackson.core.JsonParser) null, "msg");
    HttpMessageNotReadableException hmrEmpty = new HttpMessageNotReadableException("", jmEmpty, null);
    ResponseEntity<ApiError> r2b = handler.handleBadRequest(hmrEmpty, request);
    assertThat(r2b.getBody()).isNotNull();
    assertThat(r2b.getBody().getMessage()).isEqualTo("Malformed request payload");

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

        // Type mismatch when requiredType is null → "unknown"
        MethodArgumentTypeMismatchException mismatchUnknown = mock(MethodArgumentTypeMismatchException.class);
        when(mismatchUnknown.getRequiredType()).thenReturn(null);
        when(mismatchUnknown.getName()).thenReturn("flag");
        ResponseEntity<ApiError> r4b = handler.handleBadRequest(mismatchUnknown, request);
        assertThat(r4b.getBody()).isNotNull();
        assertThat(r4b.getBody().getMessage()).contains("flag");

        // Request IDs
        HttpServletRequest withIds = mock(HttpServletRequest.class);
        when(withIds.getRequestURI()).thenReturn("/x");
        when(withIds.getHeader("X-Request-Id")).thenReturn("RID");
        ResponseEntity<ApiError> r5 = handler.handleGenericException(new RuntimeException("x"), withIds);
        assertThat(r5.getBody()).isNotNull();
        assertThat(r5.getBody().getRequestId()).isEqualTo("RID");

        // Fallback to X-Correlation-Id when X-Request-Id is blank
        HttpServletRequest withCorrelation = mock(HttpServletRequest.class);
        when(withCorrelation.getRequestURI()).thenReturn("/y");
        when(withCorrelation.getHeader("X-Request-Id")).thenReturn(" ");
        when(withCorrelation.getHeader("X-Correlation-Id")).thenReturn("CID-123");
        ResponseEntity<ApiError> r6 = handler.handleGenericException(new RuntimeException("x"), withCorrelation);
        assertThat(r6.getBody()).isNotNull();
        assertThat(r6.getBody().getRequestId()).isEqualTo("CID-123");
    }

    @Test
    void badRequest_invalidFormat_with_null_value_and_non_enum_target() {
        InvalidFormatException inv = InvalidFormatException.from(null, "bad", null, String.class);
        HttpMessageNotReadableException hmr = new HttpMessageNotReadableException("", inv, null);

        ResponseEntity<ApiError> res = handler.handleBadRequest(hmr, request);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getMessage()).contains("Invalid value for");
    }

    @Test
    void badRequest_else_branch_uses_resolveMessage() {
        Exception ex = new Exception("  some message.  ");
        ResponseEntity<ApiError> res = handler.handleBadRequest(ex, request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getMessage()).isEqualTo("some message");
    }

    @Test
    void constraintViolation_builds_validation_errors() {
    @SuppressWarnings("unchecked")
    jakarta.validation.ConstraintViolation<Object> cv = (jakarta.validation.ConstraintViolation<Object>) mock(jakarta.validation.ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        jakarta.validation.Path.Node node = mock(jakarta.validation.Path.Node.class);
        when(node.getName()).thenReturn("field");
        when(node.getIndex()).thenReturn(-1);
        when(path.iterator()).thenReturn(java.util.List.of(node).iterator());
        when(cv.getPropertyPath()).thenReturn(path);
        when(cv.getMessage()).thenReturn("must not be blank");

        jakarta.validation.ConstraintViolationException cve = new jakarta.validation.ConstraintViolationException(java.util.Set.of(cv));

        ResponseEntity<ApiError> res = handler.handleConstraintViolation(cve, request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getValidationErrors()).isNotEmpty();
    }

    @Test
    void customValidation_returns_payload_errors() {
        var errors = java.util.List.of(new finalprojectprogramming.project.exceptions.api.ApiValidationError("f", "bad"));
        var vex = new ValidationException("invalid", errors);
        ResponseEntity<ApiError> res = handler.handleCustomValidation(vex, request);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(res.getBody().getValidationErrors()).hasSize(1);
    }
}
