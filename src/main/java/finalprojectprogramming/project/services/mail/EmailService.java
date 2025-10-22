package finalprojectprogramming.project.services.mail;

import finalprojectprogramming.project.models.Reservation;

public interface EmailService {

    void sendReservationCreated(Reservation reservation);

    void sendReservationApproved(Reservation reservation);

    void sendReservationCanceled(Reservation reservation);
}