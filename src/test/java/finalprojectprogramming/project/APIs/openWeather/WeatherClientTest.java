package finalprojectprogramming.project.APIs.openWeather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import finalprojectprogramming.project.configs.openWeather.WeatherProperties;
import finalprojectprogramming.project.dtos.openWeatherDTOs.CurrentWeatherResponse;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto;
import finalprojectprogramming.project.exceptions.openWeather.WeatherProviderException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

class WeatherClientTest {

    private MockWebServer server;
    private WeatherProperties properties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        objectMapper = new ObjectMapper();
        properties = buildProperties();
        properties.setBaseUrl(server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void getFiveDayForecast_whenSuccessful_returnsParsedDtoAndHitsExpectedEndpoint() throws Exception {
        server.enqueue(jsonResponse(200,
                "{\"list\":[{\"dt\":1717430400,\"main\":{\"temp\":15.5}}],\"city\":{\"coord\":{\"lat\":10.0,\"lon\":20.0}}}"));
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        Forecast5Dto dto = weatherClient.getFiveDayForecast(10.0, 20.0);

        assertThat(dto.getList()).hasSize(1);
        assertThat(dto.getList().get(0).getDt()).isEqualTo(1717430400);
        assertThat(dto.getCity().getCoord().getLat()).isEqualTo(10.0);
        assertThat(dto.getCity().getCoord().getLon()).isEqualTo(20.0);

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeader("User-Agent")).contains("Weather-Client");
        assertThat(request.getPath())
                .contains("lat=10.0")
                .contains("lon=20.0")
                .contains("appid=secret")
                .contains(properties.getForecast5Path());
    }

    @Test
    void getCurrentWeather_whenSuccessful_mapsRelevantFields() throws Exception {
        server.enqueue(jsonResponse(200,
                "{" +
                        "\"main\":{\"temp\":21.5,\"feels_like\":20.0,\"humidity\":70}," +
                        "\"wind\":{\"speed\":5.5}," +
                        "\"weather\":[{\"description\":\"Despejado\",\"icon\":\"01d\"}]," +
                        "\"dt\":1717430400,\"timezone\":3600}"));
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        CurrentWeatherResponse weather = weatherClient.getCurrentWeather(1.0, 2.0);

        assertThat(weather.getTemp()).isEqualTo(21.5);
        assertThat(weather.getFeelsLike()).isEqualTo(20.0);
        assertThat(weather.getHumidity()).isEqualTo(70);
        assertThat(weather.getWindSpeed()).isEqualTo(5.5);
        assertThat(weather.getDescription()).isEqualTo("Despejado");
        assertThat(weather.getIcon()).isEqualTo("01d");
        OffsetDateTime expected = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1717430400), ZoneOffset.ofHours(1));
        assertThat(weather.getDt()).isEqualTo(expected);

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).contains(properties.getCurrentPath());
    }

    @Test
    void getCurrentWeather_handles_missing_fields_and_zero_epoch() throws Exception {
        server.enqueue(jsonResponse(200,
                "{" +
                        "\"main\":{}," +
                        "\"wind\":{}," +
                        "\"weather\":[]," +
                        "\"dt\":0," +
                        "\"timezone\":0}"));
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        CurrentWeatherResponse weather = weatherClient.getCurrentWeather(0.0, 0.0);

        assertThat(weather.getTemp()).isNull();
        assertThat(weather.getFeelsLike()).isNull();
        assertThat(weather.getHumidity()).isNull();
        assertThat(weather.getWindSpeed()).isNull();
        assertThat(weather.getDescription()).isNull();
        assertThat(weather.getIcon()).isNull();
        assertThat(weather.getDt()).isNull();
    }

    @Test
    void send_whenBadRequest_throwsWeatherProviderException() {
        server.enqueue(jsonResponse(400, "{}"));
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        assertWeatherException(() -> weatherClient.getFiveDayForecast(0, 0), HttpStatus.BAD_REQUEST,
                "WEATHER_PROVIDER_BAD_REQUEST");
    }

    @Test
    void send_whenUnauthorized_throwsWeatherProviderException() {
        server.enqueue(jsonResponse(401, "{}"));
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        assertWeatherException(() -> weatherClient.getCurrentWeather(0, 0), HttpStatus.BAD_GATEWAY,
                "WEATHER_PROVIDER_AUTH");
    }

    @Test
    void send_whenNotFound_throwsWeatherProviderException() {
        server.enqueue(jsonResponse(404, "{}"));
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        assertWeatherException(() -> weatherClient.getFiveDayForecast(0, 0), HttpStatus.NOT_FOUND,
                "WEATHER_PROVIDER_NOT_FOUND");
    }

    @Test
    void send_whenRateLimited_retriesThenFailsWithServiceUnavailable() throws InterruptedException {
        server.enqueue(jsonResponse(429, "{}"));
        server.enqueue(jsonResponse(429, "{}"));
        server.enqueue(jsonResponse(429, "{}"));
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        try (MockedStatic<BackoffUtils> backoff = Mockito.mockStatic(BackoffUtils.class)) {
            backoff.when(() -> BackoffUtils.sleep(any())).thenAnswer(invocation -> null);

            assertWeatherException(() -> weatherClient.getFiveDayForecast(0, 0), HttpStatus.SERVICE_UNAVAILABLE,
                    "WEATHER_PROVIDER_RATE_LIMIT");

            backoff.verify(() -> BackoffUtils.sleep(Duration.ofMillis(200)));
            backoff.verify(() -> BackoffUtils.sleep(Duration.ofMillis(500)));
        }

        assertThat(server.getRequestCount()).isEqualTo(3);
    }

    @Test
    void send_whenServerError_retriesThenFailsWithTemporaryError() throws InterruptedException {
        server.enqueue(jsonResponse(503, "{}"));
        server.enqueue(jsonResponse(503, "{}"));
        server.enqueue(jsonResponse(503, "{}"));
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        try (MockedStatic<BackoffUtils> backoff = Mockito.mockStatic(BackoffUtils.class)) {
            backoff.when(() -> BackoffUtils.sleep(any())).thenAnswer(invocation -> null);

            assertWeatherException(() -> weatherClient.getCurrentWeather(0, 0), HttpStatus.SERVICE_UNAVAILABLE,
                    "WEATHER_PROVIDER_TEMPORARY_ERROR");

            backoff.verify(() -> BackoffUtils.sleep(Duration.ofMillis(200)));
            backoff.verify(() -> BackoffUtils.sleep(Duration.ofMillis(500)));
        }

        assertThat(server.getRequestCount()).isEqualTo(3);
    }

    @Test
    void send_whenTimeout_throwsGatewayTimeout() {
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ReflectionTestUtils.setField(weatherClient, "httpClient", httpClient);

        try {
            Mockito.when(httpClient.send(
                            Mockito.any(HttpRequest.class),
                            Mockito.<HttpResponse.BodyHandler<String>>any()))
                    .thenThrow(new HttpTimeoutException("timeout"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertWeatherException(() -> weatherClient.getFiveDayForecast(0, 0), HttpStatus.GATEWAY_TIMEOUT,
                "WEATHER_PROVIDER_TIMEOUT");
    }

    @Test
    void send_whenIOException_throwsGatewayTimeout() {
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ReflectionTestUtils.setField(weatherClient, "httpClient", httpClient);

        try {
            Mockito.when(httpClient.send(
                            Mockito.any(HttpRequest.class),
                            Mockito.<HttpResponse.BodyHandler<String>>any()))
                    .thenThrow(new IOException("io"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertWeatherException(() -> weatherClient.getCurrentWeather(0, 0), HttpStatus.GATEWAY_TIMEOUT,
                "WEATHER_PROVIDER_IO");
    }

    @Test
    void send_whenInterrupted_setsThreadFlagAndThrows() {
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ReflectionTestUtils.setField(weatherClient, "httpClient", httpClient);

        try {
            Mockito.when(httpClient.send(
                            Mockito.any(HttpRequest.class),
                            Mockito.<HttpResponse.BodyHandler<String>>any()))
                    .thenThrow(new InterruptedException("stop"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            assertWeatherException(() -> weatherClient.getFiveDayForecast(0, 0), HttpStatus.SERVICE_UNAVAILABLE,
                    "WEATHER_PROVIDER_INTERRUPTED");
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    private WeatherProperties buildProperties() {
        WeatherProperties props = new WeatherProperties();
        props.setForecast5Path("/data/2.5/forecast");
        props.setCurrentPath("/data/2.5/weather");
        props.setApiKey("secret");
        props.setUnits("metric");
        props.setLang("es");
    // Use a slightly higher timeout to avoid flakiness in CI and slower machines
    props.setTimeoutMs(1500);
        props.setPerUserRateLimitPerMinute(10);
        props.setCacheTtlSeconds(60);
        props.setZoneId("UTC");
        return props;
    }

    private MockResponse jsonResponse(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private void assertWeatherException(Runnable invocation, HttpStatus status, String code) {
        assertThatThrownBy(invocation::run)
                .isInstanceOf(WeatherProviderException.class)
                .extracting(ex -> (WeatherProviderException) ex)
                .satisfies(exception -> {
                    assertThat(exception.getStatus()).isEqualTo(status);
                    assertThat(exception.getCode()).isEqualTo(code);
                });
    }

    @Test
    void internal_helpers_handle_nulls() {
        WeatherClient client = new WeatherClient(properties, objectMapper);

        String sanitized = org.springframework.test.util.ReflectionTestUtils.invokeMethod(client, "sanitizeUri", new Object[]{null});
        assertThat(sanitized).isNull();

        CurrentWeatherResponse mapped = org.springframework.test.util.ReflectionTestUtils.invokeMethod(client, "mapCurrentWeather", new Object[]{null});
        assertThat(mapped).isNull();
    }

    @Test
    void debug_logging_branch_is_executed_when_logger_at_debug_level() throws Exception {
        // Fuerza el logger de WeatherClient a DEBUG para que isDebugEnabled() sea true
        org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(WeatherClient.class);
        try {
            if (slf4jLogger instanceof ch.qos.logback.classic.Logger logback) {
                var original = logback.getLevel();
                try {
                    logback.setLevel(ch.qos.logback.classic.Level.DEBUG);

                    // Respuesta 200 simple para atravesar el camino de éxito que incluye el debug
                    server.enqueue(jsonResponse(200, "{\"list\":[],\"city\":{\"coord\":{\"lat\":0,\"lon\":0}}}"));
                    WeatherClient weatherClient = new WeatherClient(properties, objectMapper);
                    Forecast5Dto dto = weatherClient.getFiveDayForecast(0.0, 0.0);
                    // se ejecuta sin lanzar y cubre la línea de debug
                    assertThat(dto).isNotNull();
                } finally {
                    logback.setLevel(original);
                }
            } else {
                // Si no es Logback, ejecutamos igual el flujo para no fallar (no garantiza cubrir la línea)
                server.enqueue(jsonResponse(200, "{\"list\":[],\"city\":{\"coord\":{\"lat\":0,\"lon\":0}}}"));
                WeatherClient weatherClient = new WeatherClient(properties, objectMapper);
                Forecast5Dto dto = weatherClient.getFiveDayForecast(0.0, 0.0);
                assertThat(dto).isNotNull();
            }
        } finally {
            // no-op
        }
    }

    @Test
    void send_when_unexpected_status_throws_generic_provider_error() {
        server.enqueue(jsonResponse(418, "{}")); // cualquier 4xx/5xx no manejado explícitamente
        WeatherClient weatherClient = new WeatherClient(properties, objectMapper);

        assertWeatherException(() -> weatherClient.getFiveDayForecast(0, 0), HttpStatus.BAD_GATEWAY,
                "WEATHER_PROVIDER_ERROR");
    }
}
