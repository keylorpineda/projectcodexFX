package finalprojectprogramming.project.services.openWeather;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherCacheService {

    private static final int MAX_ENTRIES = 1000;

    private final long ttlMillis;
    private final Clock clock;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public WeatherCacheService(long ttlSeconds) {
        this(ttlSeconds, Clock.systemUTC());
    }

    WeatherCacheService(long ttlSeconds, Clock clock) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("El TTL de cache debe ser mayor que cero");
        }
        this.ttlMillis = ttlSeconds * 1000;
        this.clock = clock;
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Objects.requireNonNull(key, "La clave de cache no puede ser nula");
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expireAt < clock.millis()) {
            cache.remove(key);
            return Optional.empty();
        }
        Object value = entry.value;
        if (!type.isInstance(value)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(value));
    }

    public void put(String key, Object value) {
        Objects.requireNonNull(key, "La clave de cache no puede ser nula");
        if (value == null) {
            cache.remove(key);
            return;
        }
        if (cache.size() >= MAX_ENTRIES) {
            cleanup();
        }
        cache.put(key, new CacheEntry(value, clock.millis() + ttlMillis));
    }

    public void evict(String key) {
        if (key != null) {
            cache.remove(key);
        }
    }

    private void cleanup() {
        long now = clock.millis();
        cache.entrySet().removeIf(entry -> entry.getValue().expireAt < now);
        int overflow = cache.size() - MAX_ENTRIES;
        if (overflow > 0) {
            cache.entrySet().stream()
                    .sorted((a, b) -> Long.compare(a.getValue().expireAt, b.getValue().expireAt))
                    .limit(overflow)
                    .map(Map.Entry::getKey)
                    .forEach(cache::remove);
        }
    }

    private static final class CacheEntry {
        private final Object value;
        private final long expireAt;

        private CacheEntry(Object value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }
}