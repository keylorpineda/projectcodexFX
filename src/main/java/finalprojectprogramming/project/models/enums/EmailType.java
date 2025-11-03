package finalprojectprogramming.project.models.enums;

/**
 * Tipos de emails del sistema con lógica específica para cada uno.
 * Define qué información debe incluirse en cada tipo de correo.
 */
public enum EmailType {
    
    /**
     * Email de confirmación de reserva (incluye QR y detalles completos).
     * Se envía cuando un admin confirma una reserva.
     */
    RESERVATION_CONFIRMATION,
    
    /**
     * Email de cancelación de reserva (incluye motivo y detalles de la reserva).
     * Se envía cuando una reserva es cancelada.
     */
    RESERVATION_CANCELLED,
    
    /**
     * Email de recordatorio de reserva próxima (incluye detalles básicos).
     * Se envía automáticamente antes de la fecha de la reserva.
     */
    RESERVATION_REMINDER,
    
    /**
     * Email de solicitud creada (sin QR, solo confirmación de recepción).
     * Se envía cuando un usuario crea una nueva solicitud de reserva.
     */
    RESERVATION_PENDING,
    
    /**
     * Email personalizado del admin (solo mensaje, SIN datos de reserva).
     * El admin puede enviar notificaciones personalizadas a usuarios.
     */
    CUSTOM_ADMIN,
    
    /**
     * Email de bienvenida para nuevos usuarios (información general).
     * Se envía al registrar un nuevo usuario en el sistema.
     */
    WELCOME,
    
    /**
     * Email de restablecimiento de contraseña (incluye token/link).
     * Se envía cuando un usuario solicita recuperar su contraseña.
     */
    PASSWORD_RESET,
    
    /**
     * Email de check-in registrado (confirma ingreso al espacio).
     * Se envía cuando el supervisor registra el ingreso del usuario.
     */
    CHECK_IN_CONFIRMED
}
