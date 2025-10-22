package finalprojectprogramming.project.repositories;

import finalprojectprogramming.project.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}