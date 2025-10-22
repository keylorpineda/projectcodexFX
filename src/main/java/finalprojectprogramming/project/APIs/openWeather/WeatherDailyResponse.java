package finalprojectprogramming.project.APIs.openWeather;


import java.util.List;

import finalprojectprogramming.project.dtos.openWeatherDTOs.DailyForecastDto;

public class WeatherDailyResponse {

    private Location location;
    private List<DailyForecastDto> daily;

    public WeatherDailyResponse() {
    }

    public WeatherDailyResponse(Location location, List<DailyForecastDto> daily) {
        this.location = location;
        this.daily = daily;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<DailyForecastDto> getDaily() {
        return daily;
    }

    public void setDaily(List<DailyForecastDto> daily) {
        this.daily = daily;
    }

    public static class Location {
        private double lat;
        private double lon;

        public Location() {
        }

        public Location(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }
    }
}