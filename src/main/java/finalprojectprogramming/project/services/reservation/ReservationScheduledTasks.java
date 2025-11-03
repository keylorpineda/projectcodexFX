package finalprojectprogramming.project.services.reservation;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Scheduled tasks for automatic reservation management.
 * - Marks reservations as NO_SHOW if check-in window expires
 */
@Component
public class ReservationScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ReservationScheduledTasks.class);
    
    private final ReservationRepository reservationRepository;

    public ReservationScheduledTasks(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Runs every 5 minutes to check for reservations that missed their check-in window.
     * Check-in window: 30 minutes before start time until 30 minutes after start time.
     * ✅ Solo marca CONFIRMED como NO_SHOW. PENDING debe ser aprobado primero.
     * 
     * ✅ LÓGICA CORRECTA:
     * - Durante el período de check-in (30 min antes hasta 30 min después): La reserva permanece CONFIRMED
     * - Solo DESPUÉS de 30 minutos del inicio: Si no hay check-in, se marca NO_SHOW
     * - PENDING NO se marca como NO_SHOW (debe ser aprobado o rechazado manualmente)
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void markExpiredReservationsAsNoShow() {
        try {
            // ✅ Usar zona horaria de Costa Rica explícitamente
            ZoneId costaRicaZone = ZoneId.of("America/Costa_Rica");
            ZonedDateTime now = ZonedDateTime.now(costaRicaZone);
            
            // ✅ CORREGIDO: Solo marcar CONFIRMED (no PENDING) como NO_SHOW
            // Las reservas PENDING están esperando aprobación, no check-in
            
            List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getDeletedAt() == null)
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED) // ✅ Solo CONFIRMED
                .filter(r -> r.getStartTime() != null)
                // ✅ Marcar NO_SHOW solo si han pasado MÁS de 30 minutos desde el inicio
                .filter(r -> {
                    // Convertir LocalDateTime a ZonedDateTime para comparación correcta
                    ZonedDateTime startTimeZoned = r.getStartTime().atZone(costaRicaZone);
                    ZonedDateTime checkInWindowEnd = startTimeZoned.plusMinutes(30);
                    return now.isAfter(checkInWindowEnd);
                })
                .toList();

            if (!expiredReservations.isEmpty()) {
                logger.info("Found {} CONFIRMED reservations with expired check-in window (30 min after start time)", expiredReservations.size());
                
                for (Reservation reservation : expiredReservations) {
                    ZonedDateTime startTimeZoned = reservation.getStartTime().atZone(costaRicaZone);
                    ZonedDateTime checkInWindowEnd = startTimeZoned.plusMinutes(30);
                    
                    reservation.setStatus(ReservationStatus.NO_SHOW);
                    reservation.setUpdatedAt(now.toLocalDateTime());
                    reservationRepository.save(reservation);
                    
                    logger.info("Marked reservation #{} as NO_SHOW (user: {}, space: {}, start: {}, check-in window ended: {})",
                        reservation.getId(),
                        reservation.getUser() != null ? reservation.getUser().getId() : "unknown",
                        reservation.getSpace() != null ? reservation.getSpace().getId() : "unknown",
                        reservation.getStartTime(),
                        checkInWindowEnd);
                }
                
                logger.info("Successfully marked {} reservations as NO_SHOW after check-in window expired", expiredReservations.size());
            }
        } catch (Exception e) {
            logger.error("Error while marking expired reservations as NO_SHOW", e);
        }
    }
}
