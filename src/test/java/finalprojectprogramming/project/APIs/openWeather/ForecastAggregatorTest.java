package finalprojectprogramming.project.APIs.openWeather;

import static org.assertj.core.api.Assertions.assertThat;

import finalprojectprogramming.project.dtos.openWeatherDTOs.DailyForecastDto;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto.Item;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto.Main;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto.Weather;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto.Wind;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ForecastAggregatorTest {

    private static Item item(long epochSeconds, Double tempMin, Double tempMax, Double temp,
            Integer humidity, Double windSpeed, Double pop, String desc, String icon) {
        Item it = new Forecast5Dto.Item();
        it.setDt(epochSeconds);
        if (tempMin != null || tempMax != null || temp != null || humidity != null) {
            Main m = new Forecast5Dto.Main();
            m.setTempMin(tempMin);
            m.setTempMax(tempMax);
            m.setTemp(temp);
            m.setHumidity(humidity);
            it.setMain(m);
        }
        if (windSpeed != null) {
            Wind w = new Forecast5Dto.Wind();
            w.setSpeed(windSpeed);
            it.setWind(w);
        }
        if (desc != null || icon != null) {
            Weather w = new Forecast5Dto.Weather();
            w.setDescription(desc);
            w.setIcon(icon);
            it.setWeather(List.of(w));
        }
        it.setPop(pop);
        return it;
    }

    @Test
    void aggregate_skips_null_items_in_list() {
        ForecastAggregator agg = new ForecastAggregator("UTC");
        Forecast5Dto dto = new Forecast5Dto();
        List<Item> items = new ArrayList<>();
        // Insert a null item that should be skipped
        items.add(null);
        // And a valid item so we still get one aggregated day
        long ts = Instant.parse("2025-04-01T00:00:00Z").getEpochSecond();
        items.add(item(ts, 10.0, 12.0, null, 50, 2.0, null, null, null));
        dto.setList(items);

        var out = agg.aggregate(dto);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getTempMin()).isEqualTo(10.0);
        assertThat(out.get(0).getTempMax()).isEqualTo(12.0);
    }

    @Test
    void aggregate_returns_empty_for_null_or_empty_input() {
        ForecastAggregator agg = new ForecastAggregator("UTC");
        assertThat(agg.aggregate(null)).isEmpty();
        Forecast5Dto dto = new Forecast5Dto();
        dto.setList(List.of());
        assertThat(agg.aggregate(dto)).isEmpty();
    }

    @Test
    void aggregate_single_day_computes_min_max_avg_and_frequencies() {
        ForecastAggregator agg = new ForecastAggregator("UTC");
        Forecast5Dto dto = new Forecast5Dto();
        List<Item> items = new ArrayList<>();
        long day = Instant.parse("2025-01-10T12:00:00Z").getEpochSecond();

        // Use explicit min/max and also fallback via temp
        items.add(item(day, 10.0, 20.0, 15.0, 40, 3.0, null, "Clouds", "10d"));
        items.add(item(day + 3 * 3600, null, null, 25.0, 50, 5.0, 0.30, "clouds ", "09d"));
        items.add(item(day + 6 * 3600, 8.0, 22.0, null, null, null, 0.80, "Rain", "09d"));
        // include null main/wind/weather sanity
        Item withNulls = new Forecast5Dto.Item();
        withNulls.setDt(day + 9 * 3600);
        withNulls.setPop(null);
        items.add(withNulls);

        dto.setList(items);

        List<DailyForecastDto> out = agg.aggregate(dto);
        assertThat(out).hasSize(1);
        DailyForecastDto d = out.get(0);
        assertThat(d.getDate()).isEqualTo(LocalDate.of(2025, 1, 10));
        assertThat(d.getTempMin()).isEqualTo(8.0);
        assertThat(d.getTempMax()).isEqualTo(25.0);
        // humidity average rounded
        assertThat(d.getHumidity()).isEqualTo(45);
        // wind speed average
        assertThat(d.getWindSpeed()).isEqualTo((3.0 + 5.0) / 2.0);
        // description frequency should normalize to same key and keep original case of first seen
        assertThat(d.getDescription()).isEqualTo("Clouds");
        // icon frequency should pick most frequent (09d)
        assertThat(d.getIcon()).isEqualTo("09d");
        // pop registered as max when any non-null present
        assertThat(d.getPop()).isEqualTo(0.80);
    }

    @Test
    void aggregate_limits_to_5_days_sorted_and_tie_breaks_by_alpha_on_equal_frequency() {
        ForecastAggregator agg = new ForecastAggregator("UTC");
        Forecast5Dto dto = new Forecast5Dto();
        List<Item> items = new ArrayList<>();
        // Create 6 days; only first 5 should remain
        for (int i = 0; i < 6; i++) {
            long ts = Instant.parse("2025-02-0" + (i + 1) + "T00:00:00Z").getEpochSecond();
            // For each day craft two weather entries to test tie-break: Alpha vs zeta
            Item a = item(ts, null, null, 10.0 + i, 50, 1.0, null, "Alpha", "a" + i);
            Item b = item(ts + 3600, null, null, 10.0 + i, 50, 1.0, null, "zeta", "z" + i);
            items.add(a);
            items.add(b);
            // add another Alpha to tie with zeta, but also tie-break lexicographically (Alpha wins)
            items.add(item(ts + 7200, null, null, 10.0 + i, null, null, null, "alpha ", null));
        }
        dto.setList(items);

        List<DailyForecastDto> out = agg.aggregate(dto);
        assertThat(out).hasSize(5);
        // Sorted ascending by date, ensure first 5 days included
        for (int i = 0; i < 5; i++) {
            LocalDate expectedDate = Instant.parse("2025-02-0" + (i + 1) + "T00:00:00Z")
                    .atZone(ZoneOffset.UTC).toLocalDate();
            assertThat(out.get(i).getDate()).isEqualTo(expectedDate);
            // tie-break on description should be "Alpha" (original of first seen with that key)
            assertThat(out.get(i).getDescription()).isEqualTo("Alpha");
        }
    }

        @Test
        void aggregate_when_all_pop_values_null_sets_pop_to_null() {
            ForecastAggregator agg = new ForecastAggregator("UTC");
            Forecast5Dto dto = new Forecast5Dto();
            List<Item> items = new ArrayList<>();
            long day = Instant.parse("2025-03-10T00:00:00Z").getEpochSecond();

            // Tres mediciones para el mismo día, todas con pop = null
            items.add(item(day, 12.0, 18.0, 15.0, 60, 3.0, null, "Clouds", "10d"));
            items.add(item(day + 3 * 3600, null, null, 16.0, 55, 4.0, null, "clouds", "10d"));
            items.add(item(day + 6 * 3600, null, null, 14.0, 65, null, null, null, null));
            dto.setList(items);

            List<DailyForecastDto> out = agg.aggregate(dto);
            assertThat(out).hasSize(1);
            DailyForecastDto d = out.get(0);
            // Como no se registró ningún POP no nulo, el valor debe ser null
            assertThat(d.getPop()).isNull();
        }
}
