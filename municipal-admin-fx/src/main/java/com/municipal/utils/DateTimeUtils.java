package com.municipal.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utilidades para manejo de fechas y zonas horarias.
 * Convierte entre hora local de Costa Rica y UTC para enviar al backend.
 */
public final class DateTimeUtils {
    
    private static final ZoneId COSTA_RICA_ZONE = ZoneId.of("America/Costa_Rica");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    
    private DateTimeUtils() {
        // Utility class
    }
    
    /**
     * Convierte un LocalDateTime de la zona horaria de Costa Rica a UTC.
     * Esto es necesario porque el backend espera todas las fechas en UTC.
     * 
     * @param localDateTime La fecha/hora en zona horaria de Costa Rica
     * @return La misma fecha/hora convertida a UTC
     */
    public static LocalDateTime costaRicaToUtc(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        
        // Crear ZonedDateTime con zona de Costa Rica
        ZonedDateTime costaRicaTime = localDateTime.atZone(COSTA_RICA_ZONE);
        
        // Convertir a UTC
        ZonedDateTime utcTime = costaRicaTime.withZoneSameInstant(UTC_ZONE);
        
        // Retornar como LocalDateTime (sin zona)
        return utcTime.toLocalDateTime();
    }
    
    /**
     * Convierte un LocalDateTime de UTC a zona horaria de Costa Rica.
     * Esto se usa para mostrar fechas del backend en la zona correcta.
     * 
     * @param utcDateTime La fecha/hora en UTC
     * @return La misma fecha/hora convertida a zona de Costa Rica
     */
    public static LocalDateTime utcToCostaRica(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        
        // Crear ZonedDateTime con zona UTC
        ZonedDateTime utcTime = utcDateTime.atZone(UTC_ZONE);
        
        // Convertir a Costa Rica
        ZonedDateTime costaRicaTime = utcTime.withZoneSameInstant(COSTA_RICA_ZONE);
        
        // Retornar como LocalDateTime (sin zona)
        return costaRicaTime.toLocalDateTime();
    }
    
    /**
     * Obtiene la hora actual en zona horaria de Costa Rica.
     * 
     * @return LocalDateTime actual en Costa Rica
     */
    public static LocalDateTime nowCostaRica() {
        return ZonedDateTime.now(COSTA_RICA_ZONE).toLocalDateTime();
    }
    
    /**
     * Obtiene la hora actual en UTC.
     * 
     * @return LocalDateTime actual en UTC
     */
    public static LocalDateTime nowUtc() {
        return ZonedDateTime.now(UTC_ZONE).toLocalDateTime();
    }
}
