package finalprojectprogramming.project.services.reservation;

import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Setting;
import finalprojectprogramming.project.repositories.SettingRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationCancellationPolicy {

    static final String MIN_HOURS_KEY = "reservations.cancellation.minHoursBefore";
    static final String MAX_HOURS_KEY = "reservations.cancellation.maxHoursBefore";

    private static final long DEFAULT_MIN_HOURS = 2L;
    private static final long DEFAULT_MAX_HOURS = 24L * 30L; // 30 días

    private final SettingRepository settingRepository;

    public ReservationCancellationPolicy(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public void assertCancellationAllowed(Reservation reservation) {
        LocalDateTime startTime = reservation.getStartTime();
        if (startTime == null) {
            throw new BusinessRuleException("Reservation start time is required for cancellation");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!startTime.isAfter(now)) {
            throw new BusinessRuleException("Reservations cannot be canceled after their start time");
        }

        long hoursUntilStart = ChronoUnit.HOURS.between(now, startTime);
        long minHours = resolveHoursSetting(MIN_HOURS_KEY, DEFAULT_MIN_HOURS);
        long maxHours = resolveHoursSetting(MAX_HOURS_KEY, DEFAULT_MAX_HOURS);

        if (minHours < 0 || maxHours < 0) {
            throw new BusinessRuleException("Cancellation windows cannot be negative");
        }
        if (minHours > maxHours) {
            throw new BusinessRuleException("Cancellation window is misconfigured: minimum is greater than maximum");
        }

        if (hoursUntilStart < minHours) {
            throw new BusinessRuleException(String.format(Locale.getDefault(),
                    "Reservations can only be canceled at least %d hours before the start time", minHours));
        }
        if (hoursUntilStart > maxHours) {
            throw new BusinessRuleException(String.format(Locale.getDefault(),
                    "Reservations can only be canceled within %d hours prior to the start time", maxHours));
        }
    }

    private long resolveHoursSetting(String key, long defaultValue) {
        return settingRepository.findByKey(key)
                .map(Setting::getValue)
                .map(value -> parsePositiveLong(key, value, defaultValue))
                .orElse(defaultValue);
    }

    private long parsePositiveLong(String key, String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed < 0) {
                throw new BusinessRuleException(
                        "Configuration " + key + " must be zero or a positive number of hours");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new BusinessRuleException(
                    "Configuration " + key + " must be a valid number of hours");
        }
    }
}
