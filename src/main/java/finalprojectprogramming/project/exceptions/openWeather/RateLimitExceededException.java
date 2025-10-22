package finalprojectprogramming.project.exceptions.openWeather;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}