package finalprojectprogramming.project.security.hash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordHashServiceTest {

    private PasswordEncoder encoder;
    private PasswordPolicy policy;
    private PasswordHashService service;

    @BeforeEach
    void setUp() {
        encoder = mock(PasswordEncoder.class);
        policy = mock(PasswordPolicy.class);
        service = new PasswordHashService(encoder, policy);
    }

    @Test
    void encodeValidatesAndEncodes() {
        when(encoder.encode("raw")).thenReturn("ENC");

        String result = service.encode("raw");

        assertThat(result).isEqualTo("ENC");
        verify(policy).validate("raw");
        verify(encoder).encode("raw");
    }

    @Test
    void encodeTreatsNullAsEmptyButStillValidates() {
        when(encoder.encode("")).thenReturn("ENC");

        String result = service.encode(null);

        assertThat(result).isEqualTo("ENC");
        verify(policy).validate(null);
        verify(encoder).encode("");
    }

    @Test
    void matchesShortCircuitsOnBlankStored() {
        boolean ok = service.matches("x", " ");
        assertThat(ok).isFalse();
        verifyNoInteractions(encoder);
    }

    @Test
    void matchesDelegates() {
        when(encoder.matches("raw", "ENC")).thenReturn(true);

        boolean ok = service.matches("raw", "ENC");

        assertThat(ok).isTrue();
        verify(encoder).matches("raw", "ENC");
    }

    @Test
    void matchesTreatsNullRawAsEmpty() {
        when(encoder.matches("", "ENC")).thenReturn(true);
        assertThat(service.matches(null, "ENC")).isTrue();
        verify(encoder).matches("", "ENC");
    }
}
