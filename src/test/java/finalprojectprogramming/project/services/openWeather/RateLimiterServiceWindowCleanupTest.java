package finalprojectprogramming.project.services.openWeather;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class RateLimiterServiceWindowCleanupTest {

    private static class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void plusMillis(long millis) {
            this.instant = this.instant.plusMillis(millis);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @Test
    void removes_old_entries_from_window_and_allows_new_request() {
        // t0
        MutableClock clock = new MutableClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        RateLimiterService limiter = new RateLimiterService(1, clock);

        // First request at t0 fills the window
        limiter.checkAndConsume("u");

        // Advance just beyond 60s so cleanup while-loop purges old entry
        clock.plusMillis(60_001);

        // Should not throw because previous entry was evicted by window cleanup
        assertThatCode(() -> limiter.checkAndConsume("u")).doesNotThrowAnyException();
    }
}
