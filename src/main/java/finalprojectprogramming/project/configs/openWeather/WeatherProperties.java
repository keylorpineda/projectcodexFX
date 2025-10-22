package finalprojectprogramming.project.configs.openWeather;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "weather")
@Validated
public class WeatherProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String forecast5Path;

    @NotBlank
    private String currentPath;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String units;

    @NotBlank
    private String lang;

    @Min(100)
    private long timeoutMs;

    @Min(1)
    private int perUserRateLimitPerMinute;

    @Min(1)
    private long cacheTtlSeconds;

    @NotBlank
    private String zoneId;

    @PostConstruct
    void validate() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                    "La propiedad weather.api-key es obligatoria; configure la variable de entorno OWM_API_KEY antes de iniciar la aplicacion.");
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getForecast5Path() {
        return forecast5Path;
    }

    public void setForecast5Path(String forecast5Path) {
        this.forecast5Path = forecast5Path;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getPerUserRateLimitPerMinute() {
        return perUserRateLimitPerMinute;
    }

    public void setPerUserRateLimitPerMinute(int perUserRateLimitPerMinute) {
        this.perUserRateLimitPerMinute = perUserRateLimitPerMinute;
    }

    public long getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(long cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}