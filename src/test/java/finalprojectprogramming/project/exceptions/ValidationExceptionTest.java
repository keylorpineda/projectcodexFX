package finalprojectprogramming.project.exceptions;

import finalprojectprogramming.project.exceptions.api.ApiValidationError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationExceptionTest {

    @Test
    void constructors_store_message_and_errors() {
        ValidationException ex1 = new ValidationException("bad");
        assertThat(ex1.getMessage()).isEqualTo("bad");
        assertThat(ex1.getValidationErrors()).isEmpty();

    ApiValidationError err = new ApiValidationError("field", "rejected");
        ValidationException ex2 = new ValidationException("oops", List.of(err));
        assertThat(ex2.getMessage()).isEqualTo("oops");
        assertThat(ex2.getValidationErrors()).containsExactly(err);
    }
}
