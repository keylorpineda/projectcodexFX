package finalprojectprogramming.project.services.openWeather;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import finalprojectprogramming.project.exceptions.openWeather.RateLimitExceededException;

public class RateLimiterService {

    private final int limitPerMinute;
    private final Clock clock;
    private final Map<String, Deque<Long>> requests = new ConcurrentHashMap<>();

    public RateLimiterService(int limitPerMinute) {
        this(limitPerMinute, Clock.systemUTC());
    }

    RateLimiterService(int limitPerMinute, Clock clock) {
        if (limitPerMinute <= 0) {
            throw new IllegalArgumentException("El limite por minuto debe ser mayor que cero");
        }
        this.limitPerMinute = limitPerMinute;
        this.clock = clock;
    }

    public void checkAndConsume(String userKey) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalArgumentException("El identificador de usuario es obligatorio para aplicar rate limit");
        }
        long now = clock.millis();
        long windowStart = now - 60_000;
        Deque<Long> deque = requests.computeIfAbsent(userKey, key -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() < windowStart) {
                deque.pollFirst();
            }
            if (deque.size() >= limitPerMinute) {
                throw new RateLimitExceededException("Limite de consultas por minuto alcanzado");
            }
            deque.addLast(now);
        }
    }
}