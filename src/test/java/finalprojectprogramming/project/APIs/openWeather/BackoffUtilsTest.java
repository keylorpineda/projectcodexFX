package finalprojectprogramming.project.APIs.openWeather;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class BackoffUtilsTest {

    @Test
    void sleep_handles_null_zero_negative_and_positive() {
        // null
        assertThatCode(() -> BackoffUtils.sleep(null)).doesNotThrowAnyException();
        // zero
        assertThatCode(() -> BackoffUtils.sleep(Duration.ZERO)).doesNotThrowAnyException();
        // negative
        assertThatCode(() -> BackoffUtils.sleep(Duration.ofMillis(-5))).doesNotThrowAnyException();
        // small positive (no assertions on time)
        assertThatCode(() -> BackoffUtils.sleep(Duration.ofMillis(1))).doesNotThrowAnyException();
    }

    @Test
    void private_constructor_is_invokable_via_reflection() throws Exception {
        var ctor = BackoffUtils.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        // should not throw
        ctor.newInstance();
    }
}
