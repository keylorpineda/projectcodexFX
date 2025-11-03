package finalprojectprogramming.project.services.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QRCodeServiceImplementation implements QRCodeService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(QRCodeServiceImplementation.class);
    private static final int DEFAULT_SIZE = 300;
    private static final String IMAGE_FORMAT = "PNG";
    
    @Override
    public byte[] generateQRCodeImage(String text, int width, int height) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            
            // Configuración para mejor calidad del QR
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Alta corrección de errores
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1); // Margen mínimo
            
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            
            byte[] imageBytes = outputStream.toByteArray();
            LOGGER.debug("QR code generated successfully for text: {} (size: {} bytes)", 
                        text.length() > 50 ? text.substring(0, 50) + "..." : text, 
                        imageBytes.length);
            
            return imageBytes;
            
        } catch (WriterException e) {
            LOGGER.error("Error generating QR code for text: {}", text, e);
            throw new IOException("Failed to generate QR code", e);
        }
    }
    
    @Override
    public byte[] generateQRCodeImage(String text) throws IOException {
        return generateQRCodeImage(text, DEFAULT_SIZE, DEFAULT_SIZE);
    }
    
    @Override
    public String toBase64DataUri(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image bytes cannot be null or empty");
        }
        
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64Image;
    }
}
