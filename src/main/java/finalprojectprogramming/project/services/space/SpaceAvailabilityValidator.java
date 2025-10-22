package finalprojectprogramming.project.services.space;

import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceSchedule;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class SpaceAvailabilityValidator {

    public void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessRuleException("Start time and end time are required to check availability");
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessRuleException("Start time must be before end time");
        }
    }

    public boolean isAvailable(Space space, LocalDateTime startTime, LocalDateTime endTime,
            Long reservationIdToIgnore) {
        if (Boolean.FALSE.equals(space.getActive()) || space.getDeletedAt() != null) {
            return false;
        }
        if (!isWithinSchedule(space, startTime, endTime)) {
            return false;
        }
        if (hasConflictingReservation(space, startTime, endTime, reservationIdToIgnore)) {
            return false;
        }
        return isDurationAllowed(space, startTime, endTime);
    }

    public void assertAvailability(Space space, LocalDateTime startTime, LocalDateTime endTime,
            Long reservationIdToIgnore) {
        validateTimeRange(startTime, endTime);
        if (Boolean.FALSE.equals(space.getActive()) || space.getDeletedAt() != null) {
            throw new BusinessRuleException("Space is not available for reservations");
        }
        if (!isWithinSchedule(space, startTime, endTime)) {
            throw new BusinessRuleException("Selected time is outside of the space schedule");
        }
        if (hasConflictingReservation(space, startTime, endTime, reservationIdToIgnore)) {
            throw new BusinessRuleException("Space is already reserved for the selected time range");
        }
        if (!isDurationAllowed(space, startTime, endTime)) {
            throw new BusinessRuleException("Reservation duration exceeds the allowed maximum for this space");
        }
    }

    private boolean isWithinSchedule(Space space, LocalDateTime startTime, LocalDateTime endTime) {
        List<SpaceSchedule> schedules = space.getSchedules();
        if (schedules == null || schedules.isEmpty()) {
            return true;
        }
        if (!startTime.toLocalDate().equals(endTime.toLocalDate())) {
            return false;
        }
        return schedules.stream()
                .filter(schedule -> schedule.getDayOfWeek() != null)
                .filter(schedule -> schedule.getDayOfWeek().equals(startTime.getDayOfWeek()))
                .anyMatch(schedule -> isTimeWithinSchedule(schedule, startTime.toLocalTime(), endTime.toLocalTime()));
    }

    private boolean isTimeWithinSchedule(SpaceSchedule schedule, LocalTime start, LocalTime end) {
        LocalTime open = schedule.getOpenTime();
        LocalTime close = schedule.getCloseTime();
        if (open == null || close == null) {
            return false;
        }
        return !start.isBefore(open) && !end.isAfter(close);
    }

    private boolean hasConflictingReservation(Space space, LocalDateTime startTime, LocalDateTime endTime,
            Long reservationIdToIgnore) {
        List<Reservation> reservations = space.getReservations();
        if (reservations == null || reservations.isEmpty()) {
            return false;
        }
        return reservations.stream()
                .filter(reservation -> reservation.getDeletedAt() == null)
                .filter(reservation -> reservationIdToIgnore == null
                        || !Objects.equals(reservation.getId(), reservationIdToIgnore))
                .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELED)
                .anyMatch(reservation -> overlaps(reservation.getStartTime(), reservation.getEndTime(), startTime, endTime));
    }

    private boolean overlaps(LocalDateTime existingStart, LocalDateTime existingEnd,
            LocalDateTime newStart, LocalDateTime newEnd) {
        if (existingStart == null || existingEnd == null) {
            return false;
        }
        return existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart);
    }

    private boolean isDurationAllowed(Space space, LocalDateTime startTime, LocalDateTime endTime) {
        Integer maxDuration = space.getMaxReservationDuration();
        if (maxDuration == null) {
            return true;
        }
        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        return durationMinutes <= maxDuration;
    }
}