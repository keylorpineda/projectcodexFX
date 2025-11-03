package finalprojectprogramming.project.services.mail;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.User;

/**
 * Servicio de envío de emails con lógica específica por tipo.
 * Cada método tiene una responsabilidad clara y envía solo la información necesaria.
 */
public interface EmailService {

    /**
     * Envía email cuando se crea una nueva solicitud de reserva.
     * NO incluye código QR (aún no está confirmada).
     * Solo notifica que la solicitud fue recibida y está en revisión.
     */
    void sendReservationPending(Reservation reservation);

    /**
     * Envía email de confirmación de reserva (INCLUYE código QR).
     * Contiene toda la información necesaria: QR, detalles del espacio, horario, etc.
     * Este es el único email que debe incluir el código QR.
     */
    void sendReservationConfirmed(Reservation reservation);

    /**
     * Envía email de cancelación de reserva.
     * Incluye motivo de cancelación y resumen de la reserva cancelada.
     * NO incluye código QR (la reserva ya no es válida).
     */
    void sendReservationCanceled(Reservation reservation);
    
    /**
     * Envía email de recordatorio de reserva próxima.
     * Incluye detalles básicos: fecha, hora, espacio, código QR para ingreso.
     */
    void sendReservationReminder(Reservation reservation);
    
    /**
     * Envía email de confirmación de check-in.
     * Notifica que el ingreso al espacio fue registrado exitosamente.
     */
    void sendCheckInConfirmed(Reservation reservation);
    
    /**
     * Envía email personalizado del admin a un usuario específico.
     * NO incluye información de reservas.
     * Solo contiene el asunto y mensaje personalizado del administrador.
     */
    void sendCustomAdminEmail(String recipientEmail, String recipientName, String subject, String message);
    
    /**
     * Envía email de bienvenida a un nuevo usuario.
     * Incluye información general del sistema y primeros pasos.
     */
    void sendWelcomeEmail(User user);
    
    /**
     * Envía email para restablecer contraseña.
     * Incluye token/link temporal para cambiar la contraseña.
     */
    void sendPasswordResetEmail(User user, String resetToken);
}