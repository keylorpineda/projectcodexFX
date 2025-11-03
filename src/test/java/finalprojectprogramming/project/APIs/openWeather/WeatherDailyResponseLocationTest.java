package finalprojectprogramming.project.APIs.openWeather;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherDailyResponseLocationTest {

    @Test
    void getters_and_setters_work_for_location() {
        WeatherDailyResponse.Location loc = new WeatherDailyResponse.Location();
        loc.setLat(9.9);
        loc.setLon(-84.1);
        assertThat(loc.getLat()).isEqualTo(9.9);
        assertThat(loc.getLon()).isEqualTo(-84.1);

        WeatherDailyResponse.Location loc2 = new WeatherDailyResponse.Location(10.0, -85.0);
        assertThat(loc2.getLat()).isEqualTo(10.0);
        assertThat(loc2.getLon()).isEqualTo(-85.0);
    }
}
