package finalprojectprogramming.project.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotValidExceptionTest {

    @Test
    void canInstantiate_emptyClass_for_coverage() {
        notValidException ex = new notValidException();
        assertThat(ex).isNotNull();
    }
}
