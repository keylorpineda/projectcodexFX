package finalprojectprogramming.project.security.hash;

import finalprojectprogramming.project.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DefaultPasswordPolicyTest {

    private final DefaultPasswordPolicy policy = new DefaultPasswordPolicy();

    @Test
    void acceptsValidPassword() {
        assertThatCode(() -> policy.validate("GoodPass1")).doesNotThrowAnyException();
    }

    @Test
    void rejectsNullOrBlank() {
        assertThatThrownBy(() -> policy.validate(null))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("no puede estar vacía");
        assertThatThrownBy(() -> policy.validate(" \t\n"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void rejectsTooShort() {
        assertThatThrownBy(() -> policy.validate("Aa1"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("al menos 8");
    }

    @Test
    void rejectsWithoutUppercase() {
        assertThatThrownBy(() -> policy.validate("lowercase1"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("mayúscula");
    }

    @Test
    void rejectsWithoutLowercase() {
        assertThatThrownBy(() -> policy.validate("UPPERCASE1"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("minúscula");
    }

    @Test
    void rejectsWithoutDigit() {
        assertThatThrownBy(() -> policy.validate("NoDigitsHere"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("dígito");
    }
}
