package finalprojectprogramming.project.services.openWeather;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DailyForecastDtoTest {

    @Test
    void canInstantiate_emptyDto() {
        DailyForecastDto dto = new DailyForecastDto();
        assertThat(dto).isNotNull();
    }
}
