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
     * If a CONFIRMED or PENDING reservation is not checked in within this window, mark as NO_SHOW.
     * 
     * ✅ LÓGICA CORRECTA:
     * - Durante el período de check-in (30 min antes hasta 30 min después): La reserva permanece CONFIRMED/PENDING
     * - Solo DESPUÉS de 30 minutos del inicio: Si no hay check-in, se marca NO_SHOW
     * - Esto permite que el QR esté disponible durante todo el período de check-in
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void markExpiredReservationsAsNoShow() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // ✅ CORREGIDO: La ventana de check-in EXPIRA 30 minutos DESPUÉS del inicio
            // Si ahora es 10:35 y la reserva inicia a 10:00, la ventana expiró (10:00 + 30min = 10:30 < 10:35)
            // Si ahora es 10:25 y la reserva inicia a 10:00, la ventana aún está activa (10:00 + 30min = 10:30 > 10:25)
            
            List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getDeletedAt() == null)
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.PENDING)
                .filter(r -> r.getStartTime() != null)
                // ✅ Marcar NO_SHOW solo si han pasado MÁS de 30 minutos desde el inicio
                .filter(r -> {
                    LocalDateTime checkInWindowEnd = r.getStartTime().plusMinutes(30);
                    return now.isAfter(checkInWindowEnd);
                })
                .toList();

            if (!expiredReservations.isEmpty()) {
                logger.info("Found {} reservations with expired check-in window (30 min after start time)", expiredReservations.size());
                
                for (Reservation reservation : expiredReservations) {
                    LocalDateTime checkInWindowEnd = reservation.getStartTime().plusMinutes(30);
                    
                    reservation.setStatus(ReservationStatus.NO_SHOW);
                    reservation.setUpdatedAt(now);
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
