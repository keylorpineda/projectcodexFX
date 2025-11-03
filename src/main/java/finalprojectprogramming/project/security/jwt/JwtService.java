package finalprojectprogramming.project.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;
    private final Clock clock;

    @Autowired
    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs
    ) {
        this(secret, expirationMs, Clock.systemUTC());
    }

    public JwtService(String secret, long expirationMs, Clock clock) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public String generateToken(String subject) {
        return generateToken(Map.of(), subject);
    }

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        Instant issuedAtInstant = clock.instant();
        Date issuedAt = Date.from(issuedAtInstant);
        Date expiration = Date.from(issuedAtInstant.plusMillis(expirationMs));

        // Build token by first setting registered claims, then merging any extra custom claims.
        // Using addClaims avoids overwriting registered claims like 'sub', 'iat', and 'exp'.
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .addClaims(extraClaims == null ? Map.of() : extraClaims)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        String username = extractUsername(token);
        return username != null
                && expectedUsername != null
                && username.equalsIgnoreCase(expectedUsername)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration == null || expiration.before(Date.from(clock.instant()));
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    // Use the same clock used for issuing tokens to ensure deterministic tests
                    .setClock(() -> Date.from(clock.instant()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return resolver.apply(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private Key getSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be empty.");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException("JWT secret must be at least 256 bits when base64 encoded.");
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (DecodingException | IllegalArgumentException e) {
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            byte[] normalized = normalizeRawSecret(raw);
            return Keys.hmacShaKeyFor(normalized);
        }
    }

    private byte[] normalizeRawSecret(byte[] raw) {
        if (raw.length >= 32) {
            return raw;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(raw);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available in the current environment.", ex);
        }
    }
}