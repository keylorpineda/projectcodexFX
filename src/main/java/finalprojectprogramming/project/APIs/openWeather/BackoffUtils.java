package finalprojectprogramming.project.APIs.openWeather;

import java.time.Duration;

public final class BackoffUtils {

    private BackoffUtils() {
    }

    public static void sleep(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return;
        }
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}