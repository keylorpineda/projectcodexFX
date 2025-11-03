package finalprojectprogramming.project.services.qr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Servicio para generación de códigos QR
 */
public interface QRCodeService {
    
    /**
     * Genera una imagen QR en formato PNG a partir de un texto
     * 
     * @param text Texto a codificar en el QR
     * @param width Ancho de la imagen en píxeles
     * @param height Alto de la imagen en píxeles
     * @return Arreglo de bytes con la imagen PNG
     * @throws IOException Si hay error generando la imagen
     */
    byte[] generateQRCodeImage(String text, int width, int height) throws IOException;
    
    /**
     * Genera una imagen QR con dimensiones predeterminadas (300x300)
     * 
     * @param text Texto a codificar en el QR
     * @return Arreglo de bytes con la imagen PNG
     * @throws IOException Si hay error generando la imagen
     */
    byte[] generateQRCodeImage(String text) throws IOException;
    
    /**
     * Convierte un arreglo de bytes a una cadena Base64 para incrustar en HTML
     * 
     * @param imageBytes Bytes de la imagen
     * @return String con el data URI completo (data:image/png;base64,...)
     */
    String toBase64DataUri(byte[] imageBytes);
}
