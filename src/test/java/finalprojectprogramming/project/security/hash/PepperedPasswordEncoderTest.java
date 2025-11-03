package finalprojectprogramming.project.security.hash;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PepperedPasswordEncoderTest {

    @Test
    void encodeAppendsPepperBeforeDelegating() {
        PasswordEncoder delegate = mock(PasswordEncoder.class);
        when(delegate.encode("secretPEP")).thenReturn("ENC");
        PepperedPasswordEncoder encoder = new PepperedPasswordEncoder(delegate, "PEP");

        String encoded = encoder.encode("secret");

        assertThat(encoded).isEqualTo("ENC");
        verify(delegate).encode("secretPEP");
    }

    @Test
    void matchesAppliesPepper() {
        PasswordEncoder delegate = mock(PasswordEncoder.class);
        when(delegate.matches("secretPEP", "ENC")).thenReturn(true);
        PepperedPasswordEncoder encoder = new PepperedPasswordEncoder(delegate, "PEP");

        boolean ok = encoder.matches("secret", "ENC");

        assertThat(ok).isTrue();
        verify(delegate).matches("secretPEP", "ENC");
    }

    @Test
    void upgradeEncodingDelegates() {
        PasswordEncoder delegate = mock(PasswordEncoder.class);
        when(delegate.upgradeEncoding("X")).thenReturn(true);
        PepperedPasswordEncoder encoder = new PepperedPasswordEncoder(delegate, "PEP");

        assertThat(encoder.upgradeEncoding("X")).isTrue();
        verify(delegate).upgradeEncoding("X");
    }

    @Test
    void nullInputsHandledAsEmpty() {
        PasswordEncoder delegate = mock(PasswordEncoder.class);
        PepperedPasswordEncoder encoder = new PepperedPasswordEncoder(delegate, null);

        encoder.encode(null);
        verify(delegate).encode("");

        encoder.matches(null, "ENC");
        verify(delegate).matches("", "ENC");
    }
}
