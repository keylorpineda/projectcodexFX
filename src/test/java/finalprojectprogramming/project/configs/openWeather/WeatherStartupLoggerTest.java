package finalprojectprogramming.project.configs.openWeather;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;

class WeatherStartupLoggerTest {

    @Test
    void onApplicationEvent_logs_messages_without_errors() {
        WeatherProperties props = new WeatherProperties();
        props.setZoneId("America/Mexico_City");
        props.setApiKey("x"); // only to keep object consistent if validated elsewhere
        WeatherStartupLogger logger = new WeatherStartupLogger(props);
        ApplicationReadyEvent evt = mock(ApplicationReadyEvent.class);
        assertThatCode(() -> logger.onApplicationEvent(evt)).doesNotThrowAnyException();
    }
}
