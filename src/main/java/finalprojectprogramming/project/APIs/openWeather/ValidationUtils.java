package finalprojectprogramming.project.APIs.openWeather;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void validateLatLon(double lat, double lon) {
        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            throw new IllegalArgumentException("Las coordenadas no pueden ser NaN");
        }
        if (Double.isInfinite(lat) || Double.isInfinite(lon)) {
            throw new IllegalArgumentException("Las coordenadas no pueden ser infinitas");
        }
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Latitud fuera de rango valido (-90 a 90)");
        }
        if (lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Longitud fuera de rango valido (-180 a 180)");
        }
    }
}