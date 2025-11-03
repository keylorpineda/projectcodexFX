package finalprojectprogramming.project.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class JwtServiceTest {

    private static final String BASE64_SECRET = Base64.getEncoder().encodeToString(
            "0123456789ABCDEF0123456789ABCDEF".getBytes(StandardCharsets.UTF_8));

    @Test
    void generateToken_withExtraClaimsProducesExpectedValues() {
        Clock clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneOffset.UTC);
        JwtService service = new JwtService(BASE64_SECRET, 3_600_000L, clock);

        Map<String, Object> claims = Map.of("scope", "admin");
        String token = service.generateToken(claims, "user@example.com");

        assertThat(service.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(service.extractExpiration(token))
                .isEqualTo(Date.from(clock.instant().plusMillis(3_600_000L)));

    Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(BASE64_SECRET));
    Object scope = Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .setClock(() -> Date.from(clock.instant()))
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("scope");
        assertThat(scope).isEqualTo("admin");
    }

    @Test
    void isTokenValid_returnsTrueForValidTokenAndSubject() {
        Clock clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneOffset.UTC);
        JwtService service = new JwtService(BASE64_SECRET, 60_000L, clock);
        String token = service.generateToken("user@example.com");

        assertThat(service.isTokenValid(token, "user@example.com")).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseWhenExpired() {
        Instant baseInstant = Instant.parse("2023-01-01T00:00:00Z");
        JwtService issuingService = new JwtService(BASE64_SECRET, 1_000L, Clock.fixed(baseInstant, ZoneOffset.UTC));
        String token = issuingService.generateToken("user@example.com");

        JwtService validatingService =
                new JwtService(BASE64_SECRET, 1_000L, Clock.fixed(baseInstant.plusSeconds(10), ZoneOffset.UTC));
        assertThat(validatingService.isTokenValid(token, "user@example.com")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseWhenUsernameDiffers() {
        JwtService service = new JwtService(BASE64_SECRET, 60_000L, Clock.systemUTC());
        String token = service.generateToken("user@example.com");

        assertThat(service.isTokenValid(token, "another@example.com")).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseWhenSignatureDoesNotMatch() {
        JwtService issuingService = new JwtService(BASE64_SECRET, 60_000L, Clock.systemUTC());
        String token = issuingService.generateToken("user@example.com");

        String differentSecret = Base64.getEncoder().encodeToString(
                "FEDCBA9876543210FEDCBA9876543210".getBytes(StandardCharsets.UTF_8));
        JwtService validatingService = new JwtService(differentSecret, 60_000L, Clock.systemUTC());

        assertThat(validatingService.isTokenValid(token, "user@example.com")).isFalse();
    }

    @Test
    void extractMethods_returnNullForInvalidToken() {
        JwtService service = new JwtService(BASE64_SECRET, 60_000L, Clock.systemUTC());

        assertThat(service.extractUsername("not-a-token")).isNull();
        assertThat(service.extractExpiration("not-a-token")).isNull();
    }

    @Test
    void generateToken_allowsShortRawSecretsByNormalizing() {
        String shortSecret = "short-secret";
        Clock clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneOffset.UTC);
        JwtService service = new JwtService(shortSecret, 60_000L, clock);
        String token = service.generateToken("user@example.com");

        JwtService validatingService = new JwtService(shortSecret, 60_000L, clock);
        assertThat(validatingService.isTokenValid(token, "user@example.com")).isTrue();
    }

    @Test
    void generateToken_allowsRawSecretsWithSufficientLength() {
        String rawSecret = "0123456789ABCDEF0123456789ABCDEF";
        JwtService service = new JwtService(rawSecret, 60_000L, Clock.systemUTC());
        String token = service.generateToken("user@example.com");

        JwtService validatingService = new JwtService(rawSecret, 60_000L, Clock.systemUTC());
        assertThat(validatingService.isTokenValid(token, "user@example.com")).isTrue();
    }

    @Test
    void generateToken_withTooShortBase64Secret_fallsBackToRawAndWorks() {
        // Base64 for the word "short" -> decodes to 5 bytes (< 32), triggers fallback path
        String tooShortBase64 = java.util.Base64.getEncoder().encodeToString("short".getBytes(StandardCharsets.UTF_8));
        Clock clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneOffset.UTC);

        JwtService service = new JwtService(tooShortBase64, 60_000L, clock);
        String token = service.generateToken("user@example.com");

        JwtService validatingService = new JwtService(tooShortBase64, 60_000L, clock);
        assertThat(validatingService.isTokenValid(token, "user@example.com")).isTrue();
    }

    @Test
    void generateToken_throwsWhenSecretEmpty() {
        JwtService service = new JwtService("", 60_000L, Clock.systemUTC());

        assertThatThrownBy(() -> service.generateToken("subject"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT secret must not be empty.");
    }

    @Test
    void generateToken_throwsIllegalStateWhenSha256Unavailable() {
        String shortSecret = "short-secret"; // triggers normalizeRawSecret path (< 32 bytes)
        JwtService service = new JwtService(shortSecret, 60_000L, Clock.systemUTC());

        try (MockedStatic<MessageDigest> mocked = Mockito.mockStatic(MessageDigest.class)) {
            mocked.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("no sha-256"));

            assertThatThrownBy(() -> service.generateToken("user@example.com"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SHA-256 algorithm is not available");
        }
    }
}
