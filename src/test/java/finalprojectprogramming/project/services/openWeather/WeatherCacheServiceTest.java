package finalprojectprogramming.project.services.openWeather;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;

class WeatherCacheServiceTest {

    @Test
    void constructor_rejects_non_positive_ttl() {
        assertThatThrownBy(() -> new WeatherCacheService(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void put_and_get_within_ttl_and_type_check() {
        Instant base = Instant.parse("2025-01-01T00:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);
        WeatherCacheService cache = new WeatherCacheService(60, clock);

        cache.put("k1", "v1");
        assertThat(cache.get("k1", String.class)).contains("v1");
        assertThat(cache.get("k1", Integer.class)).isEmpty();
    }

    @Test
    void get_evicted_after_ttl_and_null_value_removes() {
        Instant base = Instant.parse("2025-01-01T00:00:00Z");
        FakeClock clock = new FakeClock(base);
        WeatherCacheService cache = new WeatherCacheService(1, clock);

        cache.put("k1", "v1");
        assertThat(cache.get("k1", String.class)).contains("v1");
        clock.advanceSeconds(2);
        assertThat(cache.get("k1", String.class)).isEmpty();

        cache.put("k2", null);
        assertThat(cache.get("k2", Object.class)).isEmpty();
    }

    private static final class FakeClock extends Clock {
        private Instant current;
        private FakeClock(Instant current) { this.current = current; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return current; }
        public void advanceSeconds(long seconds) { current = current.plusSeconds(seconds); }
    }
}
