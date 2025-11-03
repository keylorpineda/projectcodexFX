package com.municipal.utils;

import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.SpaceDTO;
import com.municipal.dtos.UserDTO;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Caché thread-safe para datos del sistema con TTL de 2 segundos
 * Mejora la performance evitando llamadas innecesarias al backend
 */
public class DataCache {
    
    private static final long TTL_MILLIS = 2000; // 2 segundos
    
    // Cache de usuarios
    private static volatile List<UserDTO> usersCache = new CopyOnWriteArrayList<>();
    private static volatile long usersCacheTimestamp = 0;
    
    // Cache de reservas
    private static volatile List<ReservationDTO> reservationsCache = new CopyOnWriteArrayList<>();
    private static volatile long reservationsCacheTimestamp = 0;
    
    // Cache de espacios
    private static volatile List<SpaceDTO> spacesCache = new CopyOnWriteArrayList<>();
    private static volatile long spacesCacheTimestamp = 0;
    
    private DataCache() {
        // Singleton - constructor privado
    }
    
    // ==================== USERS ====================
    
    public static synchronized void cacheUsers(List<UserDTO> users) {
        if (users != null) {
            usersCache = new CopyOnWriteArrayList<>(users);
            usersCacheTimestamp = System.currentTimeMillis();
        }
    }
    
    public static synchronized List<UserDTO> getCachedUsers() {
        if (isUsersCacheValid()) {
            return new CopyOnWriteArrayList<>(usersCache);
        }
        return null;
    }
    
    public static synchronized boolean isUsersCacheValid() {
        return (System.currentTimeMillis() - usersCacheTimestamp) < TTL_MILLIS && !usersCache.isEmpty();
    }
    
    public static synchronized void invalidateUsers() {
        usersCacheTimestamp = 0;
    }
    
    // ==================== RESERVATIONS ====================
    
    public static synchronized void cacheReservations(List<ReservationDTO> reservations) {
        if (reservations != null) {
            reservationsCache = new CopyOnWriteArrayList<>(reservations);
            reservationsCacheTimestamp = System.currentTimeMillis();
        }
    }
    
    public static synchronized List<ReservationDTO> getCachedReservations() {
        if (isReservationsCacheValid()) {
            return new CopyOnWriteArrayList<>(reservationsCache);
        }
        return null;
    }
    
    public static synchronized boolean isReservationsCacheValid() {
        return (System.currentTimeMillis() - reservationsCacheTimestamp) < TTL_MILLIS && !reservationsCache.isEmpty();
    }
    
    public static synchronized void invalidateReservations() {
        reservationsCacheTimestamp = 0;
    }
    
    // ==================== SPACES ====================
    
    public static synchronized void cacheSpaces(List<SpaceDTO> spaces) {
        if (spaces != null) {
            spacesCache = new CopyOnWriteArrayList<>(spaces);
            spacesCacheTimestamp = System.currentTimeMillis();
        }
    }
    
    public static synchronized List<SpaceDTO> getCachedSpaces() {
        if (isSpacesCacheValid()) {
            return new CopyOnWriteArrayList<>(spacesCache);
        }
        return null;
    }
    
    public static synchronized boolean isSpacesCacheValid() {
        return (System.currentTimeMillis() - spacesCacheTimestamp) < TTL_MILLIS && !spacesCache.isEmpty();
    }
    
    public static synchronized void invalidateSpaces() {
        spacesCacheTimestamp = 0;
    }
    
    // ==================== GLOBAL ====================
    
    public static synchronized void invalidateAll() {
        usersCacheTimestamp = 0;
        reservationsCacheTimestamp = 0;
        spacesCacheTimestamp = 0;
    }
    
    public static synchronized void clear() {
        usersCache.clear();
        reservationsCache.clear();
        spacesCache.clear();
        invalidateAll();
    }
    
    /**
     * Retorna estadísticas del cache para debugging
     */
    public static synchronized String getCacheStats() {
        long now = System.currentTimeMillis();
        return String.format(
            "Cache Stats:\n" +
            "  Users: %d items, age: %dms, valid: %s\n" +
            "  Reservations: %d items, age: %dms, valid: %s\n" +
            "  Spaces: %d items, age: %dms, valid: %s",
            usersCache.size(), now - usersCacheTimestamp, isUsersCacheValid(),
            reservationsCache.size(), now - reservationsCacheTimestamp, isReservationsCacheValid(),
            spacesCache.size(), now - spacesCacheTimestamp, isSpacesCacheValid()
        );
    }
}
