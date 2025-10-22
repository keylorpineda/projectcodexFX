package finalprojectprogramming.project.exceptions.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiValidationError(String field, String message) {
}