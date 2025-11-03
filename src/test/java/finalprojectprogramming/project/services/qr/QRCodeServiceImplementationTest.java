package finalprojectprogramming.project.services.qr;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class QRCodeServiceImplementationTest {

    private final QRCodeServiceImplementation service = new QRCodeServiceImplementation();

    @Test
    void generateQRCodeImage_success_returns_png_bytes() throws Exception {
        byte[] png = service.generateQRCodeImage("hello-world", 120, 120);
        assertThat(png).isNotEmpty();
        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        assertThat(png[0]).isEqualTo((byte)0x89);
        assertThat(png[1]).isEqualTo((byte)0x50);
        assertThat(png[2]).isEqualTo((byte)0x4E);
        assertThat(png[3]).isEqualTo((byte)0x47);
        assertThat(png[4]).isEqualTo((byte)0x0D);
        assertThat(png[5]).isEqualTo((byte)0x0A);
        assertThat(png[6]).isEqualTo((byte)0x1A);
        assertThat(png[7]).isEqualTo((byte)0x0A);
    }

    @Test
    void generateQRCodeImage_with_defaults() throws Exception {
        byte[] png = service.generateQRCodeImage("with-defaults");
        assertThat(png).isNotEmpty();
    }

    @Test
    void generateQRCodeImage_invalid_text_throws() {
        assertThatThrownBy(() -> service.generateQRCodeImage(" ", 100, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Text cannot be null or empty");
    }

    @Test
    void generateQRCodeImage_invalid_dimensions_throws() {
        assertThatThrownBy(() -> service.generateQRCodeImage("ok", -1, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.generateQRCodeImage("ok", 100, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toBase64DataUri_success_and_invalid_inputs() throws IOException {
        byte[] png = service.generateQRCodeImage("encode-me", 80, 80);
        String dataUri = service.toBase64DataUri(png);
        assertThat(dataUri).startsWith("data:image/png;base64,");
        assertThat(dataUri.length()).isGreaterThan(30);

        assertThatThrownBy(() -> service.toBase64DataUri(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.toBase64DataUri(new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateQRCodeImage_when_content_too_large_for_dimensions_translates_writer_exception_to_ioexception() {
        // Texto extremadamente largo con dimensiones muy pequeñas fuerza un WriterException interno
        String huge = "x".repeat(5000);
        assertThatThrownBy(() -> service.generateQRCodeImage(huge, 10, 10))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to generate QR code");
    }
}
