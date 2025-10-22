package finalprojectprogramming.project.services.reservation;

import finalprojectprogramming.project.dtos.ReservationDTO;
import java.util.List;

public interface ReservationService {

    ReservationDTO create(ReservationDTO reservationDTO);

    ReservationDTO update(Long id, ReservationDTO reservationDTO);

    ReservationDTO findById(Long id);

    List<ReservationDTO> findAll();

    List<ReservationDTO> findByUser(Long userId);

    List<ReservationDTO> findBySpace(Long spaceId);

    ReservationDTO cancel(Long id, String cancellationReason);

    ReservationDTO approve(Long id, Long approverUserId);

    ReservationDTO markCheckIn(Long id);

    ReservationDTO markNoShow(Long id);

    void delete(Long id);
}
