package finalprojectprogramming.project.APIs.openWeather;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import finalprojectprogramming.project.dtos.openWeatherDTOs.DailyForecastDto;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto.Item;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto.Main;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto.Weather;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto.Wind;

public class ForecastAggregator {

    private final ZoneId zoneId;

    public ForecastAggregator(String zoneId) {
        this.zoneId = ZoneId.of(zoneId);
    }

    public List<DailyForecastDto> aggregate(Forecast5Dto forecast5Dto) {
        if (forecast5Dto == null || forecast5Dto.getList().isEmpty()) {
            return List.of();
        }
        Map<LocalDate, Aggregation> perDay = new TreeMap<>();
        for (Item item : forecast5Dto.getList()) {
            if (item == null) {
                continue;
            }
            Instant instant = Instant.ofEpochSecond(item.getDt());
            LocalDate date = instant.atZone(zoneId).toLocalDate();
            Aggregation aggregation = perDay.computeIfAbsent(date, key -> new Aggregation());
            aggregation.accept(item);
        }
        return perDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(5)
                .map(entry -> entry.getValue().toDto(entry.getKey()))
                .collect(Collectors.toList());
    }

    private static final class Aggregation {
        private Double tempMin;
        private Double tempMax;
        private double humiditySum;
        private int humidityCount;
        private double windSpeedSum;
        private int windSpeedCount;
        private double popMax;
        private boolean popRegistered;
        private final Map<String, Frequency> descriptionFrequency = new HashMap<>();
        private final Map<String, Frequency> iconFrequency = new HashMap<>();

        private void accept(Item item) {
            Main main = item.getMain();
            if (main != null) {
                updateMin(main.getTempMin());
                updateMax(main.getTempMax());
                updateWithFallbackTemp(main.getTemp());
                updateHumidity(main.getHumidity());
            }
            Wind wind = item.getWind();
            if (wind != null && wind.getSpeed() != null) {
                windSpeedSum += wind.getSpeed();
                windSpeedCount++;
            }
            Double pop = item.getPop();
            double popValue = pop != null ? pop : 0;
            if (pop != null) {
                popRegistered = true;
            }
            if (popValue > popMax) {
                popMax = popValue;
            }
            List<Weather> weathers = item.getWeather();
            if (weathers != null && !weathers.isEmpty()) {
                Weather weather = weathers.get(0);
                if (weather.getDescription() != null) {
                    registerFrequency(descriptionFrequency, weather.getDescription());
                }
                if (weather.getIcon() != null) {
                    registerFrequency(iconFrequency, weather.getIcon());
                }
            }
        }

        private void updateMin(Double candidate) {
            if (candidate == null) {
                return;
            }
            if (tempMin == null || candidate < tempMin) {
                tempMin = candidate;
            }
        }

        private void updateMax(Double candidate) {
            if (candidate == null) {
                return;
            }
            if (tempMax == null || candidate > tempMax) {
                tempMax = candidate;
            }
        }

        private void updateWithFallbackTemp(Double temp) {
            if (temp == null) {
                return;
            }
            updateMin(temp);
            updateMax(temp);
        }

        private void updateHumidity(Integer humidity) {
            if (humidity == null) {
                return;
            }
            humiditySum += humidity;
            humidityCount++;
        }

        private void registerFrequency(Map<String, Frequency> frequencyMap, String rawValue) {
            String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
            frequencyMap.compute(normalized, (key, current) -> {
                if (current == null) {
                    return new Frequency(rawValue, 1);
                }
                current.increment();
                return current;
            });
        }

        private DailyForecastDto toDto(LocalDate date) {
            DailyForecastDto dto = new DailyForecastDto();
            dto.setDate(date);
            dto.setTempMin(tempMin);
            dto.setTempMax(tempMax);
            dto.setHumidity(humidityCount == 0 ? null : (int) Math.round(humiditySum / humidityCount));
            dto.setWindSpeed(windSpeedCount == 0 ? null : windSpeedSum / windSpeedCount);
            dto.setDescription(resolveMostFrequent(descriptionFrequency));
            dto.setIcon(resolveMostFrequent(iconFrequency));
            dto.setPop(popRegistered ? popMax : null);
            return dto;
        }

        private String resolveMostFrequent(Map<String, Frequency> frequencyMap) {
            return frequencyMap.values().stream()
                    .sorted((a, b) -> {
                        int compare = Integer.compare(b.count, a.count);
                        if (compare != 0) {
                            return compare;
                        }
                        return a.value.compareToIgnoreCase(b.value);
                    })
                    .map(freq -> freq.value)
                    .findFirst()
                    .orElse(null);
        }
    }

    private static final class Frequency {
        private final String value;
        private int count;

        private Frequency(String value, int count) {
            this.value = value;
            this.count = count;
        }

        private void increment() {
            count++;
        }
    }
}