package finalprojectprogramming.project.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import finalprojectprogramming.project.exceptions.api.ApiError;
import finalprojectprogramming.project.exceptions.api.ApiValidationError;
import finalprojectprogramming.project.exceptions.openWeather.RateLimitExceededException;
import finalprojectprogramming.project.exceptions.openWeather.WeatherProviderException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.name(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRuleException(BusinessRuleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.name(), ex.getMessage(),
                request, null);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPasswordException(InvalidPasswordException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.name(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<ApiValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiValidationError(error.getField(), resolveMessage(error.getDefaultMessage())))
                .collect(Collectors.toList());

        validationErrors.addAll(ex.getBindingResult().getGlobalErrors().stream()
                .map(error -> new ApiValidationError(error.getObjectName(), resolveMessage(error.getDefaultMessage())))
                .collect(Collectors.toList()));

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", request,
                validationErrors);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        String message;
        if (ex instanceof BadCredentialsException) {
            message = "Invalid credentials";
        } else {
            message = resolveMessage(ex.getMessage());
            if (message == null || message.isBlank()) {
                message = "Authentication failed";
            }
        }

        return buildResponse(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.name(), message, request, null);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleCustomValidation(ValidationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request,
                ex.getValidationErrors());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
            HttpServletRequest request) {
        List<ApiValidationError> validationErrors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiValidationError(violation.getPropertyPath().toString(),
                        resolveMessage(violation.getMessage())))
                .collect(Collectors.toList());

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", request,
                validationErrors);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
        String message;
        if (ex instanceof HttpMessageNotReadableException readableException) {
            message = resolveHttpMessageNotReadableMessage(readableException);
        } else if (ex instanceof MissingServletRequestParameterException missing) {
            message = String.format("Missing required parameter '%s'", missing.getParameterName());
        } else if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            Class<?> requiredType = mismatch.getRequiredType();
            String requiredTypeName = (requiredType != null) ? requiredType.getSimpleName() : "unknown";
            message = String.format("Parameter '%s' should be of type %s", mismatch.getName(), requiredTypeName);
        } else {
            message = resolveMessage(ex.getMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.name(), message, request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        LOGGER.error("Data integrity violation", ex);
        return buildResponse(HttpStatus.CONFLICT, HttpStatus.CONFLICT.name(), "Data integrity violation", request,
                null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.name(), resolveMessage(ex.getMessage()),
                request, null);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", resolveMessage(ex.getMessage()),
                request, null);
    }

    @ExceptionHandler(WeatherProviderException.class)
    public ResponseEntity<ApiError> handleWeatherProvider(WeatherProviderException ex, HttpServletRequest request) {
        return buildResponse(ex.getStatus(), ex.getCode(), resolveMessage(ex.getMessage()), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        LOGGER.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String code, String message,
            HttpServletRequest request, List<ApiValidationError> validationErrors) {
        String requestId = resolveRequestId(request);
        ApiError error = new ApiError(status, code, message, request.getRequestURI(), requestId, validationErrors);
        return ResponseEntity.status(status).body(error);
    }

    private String resolveHttpMessageNotReadableMessage(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            return resolveInvalidFormatMessage(invalidFormat);
        }
        if (cause instanceof JsonMappingException mappingException) {
            String path = buildJsonPath(mappingException.getPath());
            if (path != null) {
                return "Malformed request payload near '" + path + "'";
            }
            return "Malformed request payload";
        }
        return "Malformed request payload";
    }

    private String resolveInvalidFormatMessage(InvalidFormatException exception) {
        String path = buildJsonPath(exception.getPath());
        Object value = exception.getValue();
        StringBuilder message = new StringBuilder();
        String fieldLabel = path == null ? "request payload"
                : "field '" + path + "'";
        if (value == null) {
            message.append("Invalid value for ").append(fieldLabel);
        } else {
            message.append("Invalid value '").append(value).append("' for ").append(fieldLabel);
        }

        Class<?> targetType = exception.getTargetType();
        if (targetType != null && targetType.isEnum()) {
            String allowedValues = Arrays.stream(targetType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message.append(". Allowed values: ").append(allowedValues);
        }
        return message.toString();
    }

    private String buildJsonPath(List<JsonMappingException.Reference> path) {
        String joinedPath = path.stream()
                .map(reference -> {
                    if (reference.getFieldName() != null) {
                        return reference.getFieldName();
                    }
                    if (reference.getIndex() != -1) {
                        return "[" + reference.getIndex() + "]";
                    }
                    return "?";
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));

        if (joinedPath.isBlank()) {
            return null;
        }
        return joinedPath.replace(".[", "[");
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = request.getHeader("X-Correlation-Id");
        }
        return requestId;
    }

    private String resolveMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.strip().replaceAll("\\.$", "").trim();
    }
}
