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
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void markExpiredReservationsAsNoShow() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Find reservations where check-in window has expired
            // Window ends 30 minutes after start time
            LocalDateTime windowExpiredBefore = now.minusMinutes(30);
            
            List<Reservation> expiredReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getDeletedAt() == null)
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.PENDING)
                .filter(r -> r.getStartTime() != null)
                .filter(r -> r.getStartTime().isBefore(windowExpiredBefore))
                .toList();

            if (!expiredReservations.isEmpty()) {
                logger.info("Found {} reservations to mark as NO_SHOW", expiredReservations.size());
                
                for (Reservation reservation : expiredReservations) {
                    reservation.setStatus(ReservationStatus.NO_SHOW);
                    reservation.setUpdatedAt(now);
                    reservationRepository.save(reservation);
                    
                    logger.info("Marked reservation #{} as NO_SHOW (user: {}, space: {}, scheduled: {})",
                        reservation.getId(),
                        reservation.getUser() != null ? reservation.getUser().getId() : "unknown",
                        reservation.getSpace() != null ? reservation.getSpace().getId() : "unknown",
                        reservation.getStartTime());
                }
                
                logger.info("Successfully marked {} reservations as NO_SHOW", expiredReservations.size());
            }
        } catch (Exception e) {
            logger.error("Error while marking expired reservations as NO_SHOW", e);
        }
    }
}
