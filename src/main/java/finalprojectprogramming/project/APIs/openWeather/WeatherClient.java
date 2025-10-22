package finalprojectprogramming.project.APIs.openWeather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import finalprojectprogramming.project.configs.openWeather.WeatherProperties;
import finalprojectprogramming.project.dtos.openWeatherDTOs.CurrentWeatherResponse;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto;
import finalprojectprogramming.project.exceptions.openWeather.WeatherProviderException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

public class WeatherClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherClient.class);
    private static final Duration[] RETRY_DELAYS = { Duration.ofMillis(200), Duration.ofMillis(500) };

    private final WeatherProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestTimeout;

    public WeatherClient(WeatherProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.requestTimeout = Duration.ofMillis(Math.max(properties.getTimeoutMs(), 100));
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(requestTimeout)
                .build();
    }

    public Forecast5Dto getFiveDayForecast(double lat, double lon) {
        URI uri = buildUri(properties.getForecast5Path(), lat, lon);
        return send(uri, Forecast5Dto.class);
    }

    public CurrentWeatherResponse getCurrentWeather(double lat, double lon) {
        URI uri = buildUri(properties.getCurrentPath(), lat, lon);
        JsonNode body = send(uri, JsonNode.class);
        return mapCurrentWeather(body);
    }

    private URI buildUri(String path, double lat, double lon) {
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(path)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("units", properties.getUnits())
                .queryParam("lang", properties.getLang())
                .queryParam("appid", properties.getApiKey())
                .build(true)
                .toUri();
    }

    private <T> T send(URI uri, Class<T> responseType) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(requestTimeout)
                .GET()
                .header("Accept", "application/json")
                .header("User-Agent", "FinalProject-Weather-Client")
                .build();
        String sanitizedUri = sanitizeUri(uri.toString());
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Invocando OpenWeatherMap (intento {}): {}", attempt, sanitizedUri);
                }
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return deserialize(response.body(), responseType);
                }
                if (statusCode == 400 || statusCode == 422) {
                    throw new WeatherProviderException(HttpStatus.BAD_REQUEST, "WEATHER_PROVIDER_BAD_REQUEST",
                            "Parametros invalidos para el proveedor de clima");
                }
                if (statusCode == 401 || statusCode == 403) {
                    throw new WeatherProviderException(HttpStatus.BAD_GATEWAY, "WEATHER_PROVIDER_AUTH",
                            "Error de autenticacion con proveedor de clima");
                }
                if (statusCode == 404) {
                    throw new WeatherProviderException(HttpStatus.NOT_FOUND, "WEATHER_PROVIDER_NOT_FOUND",
                            "No hay datos de clima disponibles para las coordenadas indicadas");
                }
                if (statusCode == 429) {
                    int delayIndex = attempt - 1;
                    if (delayIndex < RETRY_DELAYS.length) {
                        BackoffUtils.sleep(RETRY_DELAYS[delayIndex]);
                        continue;
                    }
                    throw new WeatherProviderException(HttpStatus.SERVICE_UNAVAILABLE, "WEATHER_PROVIDER_RATE_LIMIT",
                            "Proveedor de clima saturado");
                }
                if (statusCode >= 500 && statusCode < 600) {
                    int delayIndex = attempt - 1;
                    if (delayIndex < RETRY_DELAYS.length) {
                        BackoffUtils.sleep(RETRY_DELAYS[delayIndex]);
                        continue;
                    }
                    throw new WeatherProviderException(HttpStatus.SERVICE_UNAVAILABLE, "WEATHER_PROVIDER_TEMPORARY_ERROR",
                            "Error temporal del proveedor de clima");
                }
                throw new WeatherProviderException(HttpStatus.BAD_GATEWAY, "WEATHER_PROVIDER_ERROR",
                        "Respuesta inesperada del proveedor de clima");
            } catch (HttpTimeoutException timeout) {
                throw new WeatherProviderException(HttpStatus.GATEWAY_TIMEOUT, "WEATHER_PROVIDER_TIMEOUT",
                        "Tiempo de espera agotado consultando clima");
            } catch (IOException io) {
                throw new WeatherProviderException(HttpStatus.GATEWAY_TIMEOUT, "WEATHER_PROVIDER_IO",
                        "Tiempo de espera agotado consultando clima");
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new WeatherProviderException(HttpStatus.SERVICE_UNAVAILABLE, "WEATHER_PROVIDER_INTERRUPTED",
                        "Consulta al proveedor de clima interrumpida");
            }
        }
    }

    private <T> T deserialize(String body, Class<T> responseType) throws IOException {
        if (responseType.equals(JsonNode.class)) {
            return responseType.cast(objectMapper.readTree(body));
        }
        return objectMapper.readValue(body, responseType);
    }

    private CurrentWeatherResponse mapCurrentWeather(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isMissingNode()) {
            return null;
        }
        CurrentWeatherResponse response = new CurrentWeatherResponse();
        JsonNode main = jsonNode.path("main");
        response.setTemp(main.hasNonNull("temp") ? main.get("temp").asDouble() : null);
        response.setFeelsLike(main.hasNonNull("feels_like") ? main.get("feels_like").asDouble() : null);
        response.setHumidity(main.hasNonNull("humidity") ? main.get("humidity").asInt() : null);
        JsonNode wind = jsonNode.path("wind");
        response.setWindSpeed(wind.hasNonNull("speed") ? wind.get("speed").asDouble() : null);
        JsonNode weatherNode = jsonNode.path("weather");
        if (weatherNode.isArray() && weatherNode.size() > 0) {
            JsonNode first = weatherNode.get(0);
            response.setDescription(asText(first.path("description")));
            response.setIcon(asText(first.path("icon")));
        }
        long epoch = jsonNode.path("dt").asLong(0);
        int timezoneOffset = jsonNode.path("timezone").asInt(0);
        if (epoch > 0) {
            response.setDt(
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.ofTotalSeconds(timezoneOffset)));
        }
        return response;
    }

    private String asText(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private String sanitizeUri(String rawUri) {
        if (rawUri == null) {
            return null;
        }
        return rawUri.replaceAll("appid=[^&]+", "appid=***");
    }
}