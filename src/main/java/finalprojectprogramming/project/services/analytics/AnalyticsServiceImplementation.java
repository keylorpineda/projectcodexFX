package finalprojectprogramming.project.services.analytics;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImplementation implements AnalyticsService {

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;

    @Autowired
    public AnalyticsServiceImplementation(
            ReservationRepository reservationRepository,
            SpaceRepository spaceRepository,
            UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<Long, Double> getOccupancyRateBySpace() {
        List<Space> spaces = spaceRepository.findAll();
        List<Reservation> allReservations = reservationRepository.findAll();
        
        Map<Long, List<Reservation>> reservationsBySpace = allReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getSpace().getId()));
        
        Map<Long, Double> occupancyRates = new HashMap<>();
        
        for (Space space : spaces) {
            List<Reservation> spaceReservations = reservationsBySpace.getOrDefault(space.getId(), List.of());
            long confirmedReservations = spaceReservations.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || 
                                 r.getStatus() == ReservationStatus.CHECKED_IN)
                    .count();
            
            // Tasa de ocupación = reservaciones confirmadas / total reservaciones * 100
            double occupancyRate = spaceReservations.isEmpty() ? 0.0 : 
                    (confirmedReservations * 100.0 / spaceReservations.size());
            
            occupancyRates.put(space.getId(), occupancyRate);
        }
        
        return occupancyRates;
    }

    @Override
    public List<SpaceStatistics> getMostReservedSpaces(int limit) {
        List<Space> spaces = spaceRepository.findAll();
        List<Reservation> allReservations = reservationRepository.findAll();
        
        Map<Long, List<Reservation>> reservationsBySpace = allReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getSpace().getId()));
        
        return spaces.stream()
                .map(space -> {
                    List<Reservation> spaceReservations = reservationsBySpace.getOrDefault(space.getId(), List.of());
                    long totalReservations = spaceReservations.size();
                    long confirmedReservations = spaceReservations.stream()
                            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || 
                                         r.getStatus() == ReservationStatus.CHECKED_IN)
                            .count();
                    
                    double occupancyRate = totalReservations > 0 ? 
                            (confirmedReservations * 100.0 / totalReservations) : 0.0;
                    
                    return new SpaceStatistics(
                            space.getId(),
                            space.getName(),
                            space.getType().toString(),
                            totalReservations,
                            confirmedReservations,
                            occupancyRate
                    );
                })
                .sorted(Comparator.comparing(SpaceStatistics::totalReservations).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Long> getReservationsByHour() {
        List<Reservation> allReservations = reservationRepository.findAll();
        
        return allReservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStartTime().getHour(),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<Long, Double> getNoShowRateByUser() {
        List<User> users = userRepository.findAll();
        List<Reservation> allReservations = reservationRepository.findAll();
        
        Map<Long, List<Reservation>> reservationsByUser = allReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getUser().getId()));
        
        Map<Long, Double> noShowRates = new HashMap<>();
        
        for (User user : users) {
            List<Reservation> userReservations = reservationsByUser.getOrDefault(user.getId(), List.of());
            long noShowCount = userReservations.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.NO_SHOW)
                    .count();
            
            double noShowRate = userReservations.isEmpty() ? 0.0 : 
                    (noShowCount * 100.0 / userReservations.size());
            
            noShowRates.put(user.getId(), noShowRate);
        }
        
        return noShowRates;
    }

    @Override
    public SystemStatistics getSystemStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getActive)
                .count();
        
        long totalSpaces = spaceRepository.count();
        
        List<Reservation> allReservations = reservationRepository.findAll();
        long totalReservations = allReservations.size();
        
        long confirmedReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || 
                             r.getStatus() == ReservationStatus.CHECKED_IN)
                .count();
        
        long canceledReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELED)
                .count();
        
        long pendingReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .count();
        
        // Promedio de ocupación de todos los espacios
        Map<Long, Double> occupancyRates = getOccupancyRateBySpace();
        double averageOccupancyRate = occupancyRates.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // Tasa general de no-show
        long noShowCount = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.NO_SHOW)
                .count();
        double noShowRate = totalReservations > 0 ? 
                (noShowCount * 100.0 / totalReservations) : 0.0;
        
        return new SystemStatistics(
                totalUsers,
                activeUsers,
                totalSpaces,
                totalReservations,
                confirmedReservations,
                canceledReservations,
                pendingReservations,
                averageOccupancyRate,
                noShowRate
        );
    }

    @Override
    public Map<String, Long> getReservationsByStatus() {
        List<Reservation> allReservations = reservationRepository.findAll();
        
        return allReservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus().name(),
                        Collectors.counting()
                ));
    }
}
