package finalprojectprogramming.project.services.reservation;

public interface ReservationExportService {

    byte[] exportAllReservations();

    byte[] exportReservationsForUser(Long userId);
}
