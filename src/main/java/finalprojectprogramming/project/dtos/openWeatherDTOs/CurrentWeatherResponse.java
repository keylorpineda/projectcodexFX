package finalprojectprogramming.project.dtos.openWeatherDTOs;

import java.time.OffsetDateTime;

public class CurrentWeatherResponse {

    private Double temp;
    private Double feelsLike;
    private Integer humidity;
    private Double windSpeed;
    private String description;
    private String icon;
    private OffsetDateTime dt;

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }

    public Double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(Double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public OffsetDateTime getDt() {
        return dt;
    }

    public void setDt(OffsetDateTime dt) {
        this.dt = dt;
    }
}