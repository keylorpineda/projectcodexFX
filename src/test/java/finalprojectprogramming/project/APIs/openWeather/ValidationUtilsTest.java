package finalprojectprogramming.project.APIs.openWeather;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ValidationUtilsTest {

    @Test
    void validateLatLon_accepts_valid_bounds() {
        assertThatCode(() -> ValidationUtils.validateLatLon(0, 0)).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.validateLatLon(90, 180)).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.validateLatLon(-90, -180)).doesNotThrowAnyException();
        assertThatCode(() -> ValidationUtils.validateLatLon(10.5, -76.3)).doesNotThrowAnyException();
    }

    @Test
    void validateLatLon_rejects_invalid_values() {
        assertThatThrownBy(() -> ValidationUtils.validateLatLon(Double.NaN, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NaN");
        assertThatThrownBy(() -> ValidationUtils.validateLatLon(0, Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ValidationUtils.validateLatLon(Double.POSITIVE_INFINITY, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("infinitas");
        assertThatThrownBy(() -> ValidationUtils.validateLatLon(0, Double.NEGATIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ValidationUtils.validateLatLon(91, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitud fuera de rango");
        assertThatThrownBy(() -> ValidationUtils.validateLatLon(-91, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ValidationUtils.validateLatLon(0, 181))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitud fuera de rango");
        assertThatThrownBy(() -> ValidationUtils.validateLatLon(0, -181))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
