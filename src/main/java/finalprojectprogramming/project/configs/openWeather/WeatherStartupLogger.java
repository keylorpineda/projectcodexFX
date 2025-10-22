package finalprojectprogramming.project.configs.openWeather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

public class WeatherStartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherStartupLogger.class);

    private final WeatherProperties properties;

    public WeatherStartupLogger(WeatherProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        LOGGER.info(
                "Weather module listo. Ejemplo de consulta diaria: curl \"http://localhost:8080/api/weather/daily?lat=9.36&lon=-83.70\"");
        LOGGER.debug("Zona horaria configurada para agregacion: {}", properties.getZoneId());
    }
}