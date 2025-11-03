package finalprojectprogramming.project.configs.openWeather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class WeatherPropertiesTest {

    @Test
    void validate_throws_when_apiKey_missing_or_blank() {
        WeatherProperties props = new WeatherProperties();
        props.setApiKey(null);
        assertThatThrownBy(() -> invokeValidate(props))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("weather.api-key");

        props.setApiKey("   ");
        assertThatThrownBy(() -> invokeValidate(props))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getters_and_setters_work_and_validate_ok_when_apiKey_present() {
        WeatherProperties props = new WeatherProperties();
        props.setBaseUrl("http://x");
        props.setForecast5Path("/f");
        props.setCurrentPath("/c");
        props.setApiKey("key");
        props.setUnits("metric");
        props.setLang("es");
        props.setTimeoutMs(500);
        props.setPerUserRateLimitPerMinute(5);
        props.setCacheTtlSeconds(60);
        props.setZoneId("UTC");

        assertThat(props.getBaseUrl()).isEqualTo("http://x");
        assertThat(props.getForecast5Path()).isEqualTo("/f");
        assertThat(props.getCurrentPath()).isEqualTo("/c");
        assertThat(props.getApiKey()).isEqualTo("key");
        assertThat(props.getUnits()).isEqualTo("metric");
        assertThat(props.getLang()).isEqualTo("es");
        assertThat(props.getTimeoutMs()).isEqualTo(500);
        assertThat(props.getPerUserRateLimitPerMinute()).isEqualTo(5);
        assertThat(props.getCacheTtlSeconds()).isEqualTo(60);
        assertThat(props.getZoneId()).isEqualTo("UTC");

        // validate shouldn't throw now
        invokeValidate(props);
    }

    // Package-private method is accessible from same package, call directly
    private static void invokeValidate(WeatherProperties props) {
        props.validate();
    }
}
