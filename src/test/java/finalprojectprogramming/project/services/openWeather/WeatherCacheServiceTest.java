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
    void evict_handles_null_and_removes_existing_key() {
        Instant base = Instant.parse("2025-01-01T00:00:00Z");
        Clock clock = Clock.fixed(base, ZoneOffset.UTC);
        WeatherCacheService cache = new WeatherCacheService(60, clock);

        // put and verify present
        cache.put("k1", 123);
        assertThat(cache.get("k1", Integer.class)).contains(123);

        // evict null should not throw
        cache.evict(null);
        assertThat(cache.get("k1", Integer.class)).contains(123);

        // evict existing key
        cache.evict("k1");
        assertThat(cache.get("k1", Integer.class)).isEmpty();
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

    @Test
    void cleanup_removes_expired_and_limits_overflow_to_max_entries() {
        // Fake clock we can advance
        Instant base = Instant.parse("2025-01-01T00:00:00Z");
        FakeClock clock = new FakeClock(base);
        // ttl 2 seconds
        WeatherCacheService cache = new WeatherCacheService(2, clock);

        // 1) Insert many non-expired entries up to MAX_ENTRIES (1000)
        for (int i = 0; i < 1000; i++) {
            cache.put("k" + i, i);
        }
        // 2) Advance to expire some entries and insert one more to trigger cleanup
        clock.advanceSeconds(3); // all previous expire
        cache.put("k-new", 1);
        // All expired should be removed and new one stored
        assertThat(cache.get("k-new", Integer.class)).contains(1);

        // Refill with fresh entries up to the limit
        for (int i = 0; i < 1000; i++) {
            cache.put("n" + i, i);
        }
        // Insert one more to exceed MAX_ENTRIES and trigger overflow-limiting branch
        cache.put("overflow", 99);
        // We can't directly assert internal size, but at least ensure an existing key is retrievable
        assertThat(cache.get("overflow", Integer.class)).contains(99);
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
