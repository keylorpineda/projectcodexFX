package finalprojectprogramming.project.exceptions;

import java.util.List;

import finalprojectprogramming.project.exceptions.api.ApiValidationError;

public class ValidationException extends RuntimeException {

    private final List<ApiValidationError> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = List.of();
    }

    public ValidationException(String message, List<ApiValidationError> validationErrors) {
        super(message);
        this.validationErrors = validationErrors == null ? List.of() : List.copyOf(validationErrors);
    }

    public List<ApiValidationError> getValidationErrors() {
        return validationErrors;
    }
}
