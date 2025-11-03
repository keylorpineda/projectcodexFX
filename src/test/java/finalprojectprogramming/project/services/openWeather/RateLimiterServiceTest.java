package finalprojectprogramming.project.services.openWeather;

import finalprojectprogramming.project.exceptions.openWeather.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;

class RateLimiterServiceTest {

    @Test
    void constructor_rejects_non_positive_limit() {
        assertThatThrownBy(() -> new RateLimiterService(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RateLimiterService(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkAndConsume_rejects_blank_userKey() {
        RateLimiterService limiter = new RateLimiterService(5);
        assertThatThrownBy(() -> limiter.checkAndConsume(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> limiter.checkAndConsume(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkAndConsume_allows_within_limit_and_enforces_window() {
        Instant base = Instant.parse("2025-01-01T00:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);
        RateLimiterService limiter = new RateLimiterService(2, clock);

        limiter.checkAndConsume("u1");
        limiter.checkAndConsume("u1");
        assertThatThrownBy(() -> limiter.checkAndConsume("u1"))
                .isInstanceOf(RateLimitExceededException.class);
    }
}
