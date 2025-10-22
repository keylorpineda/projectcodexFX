package finalprojectprogramming.project.exceptions.openWeather;

import org.springframework.http.HttpStatus;

public class WeatherProviderException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public WeatherProviderException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}