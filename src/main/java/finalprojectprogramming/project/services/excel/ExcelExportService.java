package finalprojectprogramming.project.services.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Servicio para exportar datos a formato Excel
 */
public interface ExcelExportService {
    
    /**
     * Exporta el historial de reservaciones de un usuario a Excel
     * 
     * @param userId ID del usuario
     * @return ByteArrayOutputStream con el archivo Excel
     * @throws IOException Si hay error generando el archivo
     */
    ByteArrayOutputStream exportUserReservations(Long userId) throws IOException;
    
    /**
     * Exporta todas las reservaciones a Excel (solo para administradores)
     * 
     * @return ByteArrayOutputStream con el archivo Excel
     * @throws IOException Si hay error generando el archivo
     */
    ByteArrayOutputStream exportAllReservations() throws IOException;
    
    /**
     * Exporta estadísticas de espacios a Excel
     * 
     * @return ByteArrayOutputStream con el archivo Excel
     * @throws IOException Si hay error generando el archivo
     */
    ByteArrayOutputStream exportSpaceStatistics() throws IOException;
}
