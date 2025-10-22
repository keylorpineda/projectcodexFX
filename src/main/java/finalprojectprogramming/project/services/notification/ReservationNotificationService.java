package finalprojectprogramming.project.services.notification;

import finalprojectprogramming.project.models.Reservation;

public interface ReservationNotificationService {

    void notifyReservationCreated(Reservation reservation);

    void notifyReservationApproved(Reservation reservation);

    void notifyReservationCanceled(Reservation reservation);
}