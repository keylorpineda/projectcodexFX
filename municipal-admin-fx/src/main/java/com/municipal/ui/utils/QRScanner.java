package com.municipal.ui.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utilidad para escanear códigos QR usando la cámara web
 */
public class QRScanner {
    
    private Webcam webcam;
    private ScheduledExecutorService executor;
    private Consumer<String> onQRCodeDetected;
    private Consumer<Image> onFrameUpdate;
    private volatile boolean running = false;
    private final Reader reader;
    
    public QRScanner() {
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        this.reader = new MultiFormatReader();
        ((MultiFormatReader) reader).setHints(hints);
    }
    
    /**
     * Inicia el escaneo de códigos QR
     * @param onQRCodeDetected Callback cuando se detecta un código QR
     * @param onFrameUpdate Callback para actualizar la vista previa de la cámara
     * @return true si se inició correctamente, false si no hay cámara disponible
     */
    public boolean start(Consumer<String> onQRCodeDetected, Consumer<Image> onFrameUpdate) {
        if (running) {
            return true;
        }
        
        this.onQRCodeDetected = onQRCodeDetected;
        this.onFrameUpdate = onFrameUpdate;
        
        // Obtener la cámara predeterminada
        webcam = Webcam.getDefault();
        if (webcam == null) {
            return false;
        }
        
        // Configurar resolución (640x480 es un buen balance entre calidad y rendimiento)
        webcam.setViewSize(new Dimension(640, 480));
        
        // Abrir la cámara
        if (!webcam.open()) {
            return false;
        }
        
        running = true;
        
        // Iniciar el executor para capturar frames continuamente
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "QR-Scanner-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        executor.scheduleAtFixedRate(this::captureAndProcess, 0, 100, TimeUnit.MILLISECONDS);
        
        return true;
    }
    
    /**
     * Detiene el escaneo y libera recursos
     */
    public void stop() {
        running = false;
        
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
    
    /**
     * Captura un frame de la cámara y lo procesa
     */
    private void captureAndProcess() {
        if (!running || webcam == null || !webcam.isOpen()) {
            return;
        }
        
        try {
            BufferedImage image = webcam.getImage();
            if (image == null) {
                return;
            }
            
            // Actualizar vista previa en el hilo de JavaFX
            if (onFrameUpdate != null) {
                Image fxImage = SwingFXUtils.toFXImage(image, null);
                Platform.runLater(() -> onFrameUpdate.accept(fxImage));
            }
            
            // Intentar decodificar el código QR
            try {
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = reader.decode(bitmap);
                
                if (result != null && result.getText() != null && !result.getText().isEmpty()) {
                    String qrCode = result.getText();
                    
                    // Notificar en el hilo de JavaFX
                    if (onQRCodeDetected != null) {
                        Platform.runLater(() -> onQRCodeDetected.accept(qrCode));
                    }
                    
                    // Detener después de detectar un código (evitar múltiples detecciones)
                    stop();
                }
            } catch (NotFoundException e) {
                // No se encontró código QR en este frame, continuar buscando
            } catch (ChecksumException | FormatException e) {
                // Error al decodificar, ignorar y continuar
            }
            
        } catch (Exception e) {
            System.err.println("Error al capturar/procesar imagen: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si hay cámaras disponibles
     * @return true si hay al menos una cámara disponible
     */
    public static boolean isCameraAvailable() {
        try {
            Webcam webcam = Webcam.getDefault();
            return webcam != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica si el scanner está actualmente escaneando
     * @return true si está activo
     */
    public boolean isRunning() {
        return running;
    }
}
