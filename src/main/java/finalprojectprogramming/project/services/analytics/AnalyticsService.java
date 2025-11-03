package finalprojectprogramming.project.services.analytics;

import java.util.List;
import java.util.Map;

/**
 * Servicio para generar métricas y estadísticas del sistema
 */
public interface AnalyticsService {
    
    /**
     * Obtiene la tasa de ocupación por espacio
     * @return Mapa con ID de espacio y su tasa de ocupación (0-100%)
     */
    Map<Long, Double> getOccupancyRateBySpace();
    
    /**
     * Obtiene los espacios más reservados
     * @param limit Número de espacios a retornar
     * @return Lista de SpaceStatistics con los espacios más populares
     */
    List<SpaceStatistics> getMostReservedSpaces(int limit);
    
    /**
     * Obtiene distribución de reservaciones por hora del día
     * @return Mapa con hora (0-23) y cantidad de reservaciones
     */
    Map<Integer, Long> getReservationsByHour();
    
    /**
     * Obtiene tasa de "no-show" por usuario
     * @return Mapa con ID de usuario y su tasa de no-show (0-100%)
     */
    Map<Long, Double> getNoShowRateByUser();
    
    /**
     * Obtiene estadísticas generales del sistema
     * @return Objeto con estadísticas generales
     */
    SystemStatistics getSystemStatistics();
    
    /**
     * Obtiene estadísticas de reservaciones por estado
     * @return Mapa con estado de reservación y cantidad
     */
    Map<String, Long> getReservationsByStatus();
    
    /**
     * Clase para estadísticas de espacios
     */
    record SpaceStatistics(
            Long spaceId,
            String spaceName,
            String spaceType,
            Long totalReservations,
            Long confirmedReservations,
            Double occupancyRate
    ) {}
    
    /**
     * Clase para estadísticas generales del sistema
     */
    record SystemStatistics(
            Long totalUsers,
            Long activeUsers,
            Long totalSpaces,
            Long totalReservations,
            Long confirmedReservations,
            Long canceledReservations,
            Long pendingReservations,
            Double averageOccupancyRate,
            Double noShowRate
    ) {}
}
