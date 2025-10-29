package com.municipal.ui.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public final class QRCodeGenerator {

    private static final int DEFAULT_SIZE = 280;

    private QRCodeGenerator() {
    }

    /**
     * Generates a QR code image using the default size.
     *
     * @param contents text contents to encode within the QR image
     * @return QR code rendered as a {@link WritableImage}
     * @throws WriterException if the QR image cannot be generated
     */
    public static WritableImage generate(String contents) throws WriterException {
        return generate(contents, DEFAULT_SIZE);
    }

    /**
     * Generates a QR code with the provided size.
     *
     * @param contents text contents to encode within the QR image
     * @param size     square size (width = height) of the resulting image in pixels
     * @return QR code rendered as a {@link WritableImage}
     * @throws WriterException if the QR image cannot be generated
     */
    public static WritableImage generate(String contents, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, size, size, hints);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix, new MatrixToImageConfig());
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}