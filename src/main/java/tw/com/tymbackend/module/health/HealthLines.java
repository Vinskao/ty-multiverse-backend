package tw.com.tymbackend.module.health;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the iPhone Shortcut's text: one "metric,date,value" line per day, for example
 * {@code StepCount,2026/11/15 00:04,8,912 count}. A Shortcut cannot easily produce clean JSON (typographic quotes, grouped
 * numbers, localized dates), so the date and value are read leniently and the unit is normalized here.
 */
final class HealthLines {
    static final int MAX_LINES = 200;
    private static final LocalDate EARLIEST = LocalDate.of(2000, 1, 1);
    private static final Pattern DAY = Pattern.compile("(\\d{4})\\D{1,3}(\\d{1,2})\\D{1,3}(\\d{1,2})");
    private static final Pattern VALUE = Pattern.compile("(-?\\d[\\d,]*(?:\\.\\d+)?)\\s*([^\\s\\d,.]\\S*)?");
    private static final Pattern METRIC = Pattern.compile("[A-Za-z0-9.]{1,120}");

    private record Unit(String name, double factor) {}

    private static final Map<String, Unit> UNITS = Map.ofEntries(
            Map.entry("count", new Unit("count", 1)), Map.entry("次", new Unit("count", 1)), Map.entry("步", new Unit("count", 1)),
            Map.entry("層", new Unit("count", 1)), Map.entry("km", new Unit("km", 1)), Map.entry("KM", new Unit("km", 1)),
            Map.entry("公里", new Unit("km", 1)), Map.entry("m", new Unit("km", 0.001)), Map.entry("公尺", new Unit("km", 0.001)),
            Map.entry("mi", new Unit("km", 1.609344)), Map.entry("英里", new Unit("km", 1.609344)),
            Map.entry("min", new Unit("min", 1)), Map.entry("分鐘", new Unit("min", 1)), Map.entry("分", new Unit("min", 1)),
            Map.entry("hr", new Unit("min", 60)), Map.entry("小時", new Unit("min", 60)),
            Map.entry("Cal", new Unit("Cal", 1)), Map.entry("kcal", new Unit("Cal", 1)), Map.entry("kCal", new Unit("Cal", 1)),
            Map.entry("大卡", new Unit("Cal", 1)), Map.entry("千卡", new Unit("Cal", 1)));
    private static final Map<String, String> DEFAULT_UNIT = Map.of("StepCount", "count", "FlightsClimbed", "count",
            "DistanceWalkingRunning", "km", "DistanceCycling", "km", "AppleExerciseTime", "min", "ActiveEnergyBurned", "Cal");

    private HealthLines() {}

    static List<HealthDataStore.Daily> parse(String text, LocalDate latest) {
        String[] lines = text.split("\\R");
        if (lines.length > MAX_LINES) {
            throw new IllegalArgumentException("Too many lines (limit " + MAX_LINES + ")");
        }
        List<HealthDataStore.Daily> rows = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            if (!line.isEmpty()) {
                try {
                    rows.add(row(line, latest));
                } catch (IllegalArgumentException invalid) {
                    throw new IllegalArgumentException("Line " + (i + 1) + ": " + invalid.getMessage());
                }
            }
        }
        return rows;
    }

    private static HealthDataStore.Daily row(String line, LocalDate latest) {
        String[] parts = line.split(",", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException("expected metric,date,value");
        }
        String metric = parts[0].strip();
        if (!METRIC.matcher(metric).matches()) {
            throw new IllegalArgumentException("invalid metric name");
        }
        Matcher day = DAY.matcher(parts[1]);
        if (!day.find()) {
            throw new IllegalArgumentException("no date found in '" + parts[1].strip() + "'");
        }
        LocalDate date;
        try {
            date = LocalDate.of(Integer.parseInt(day.group(1)), Integer.parseInt(day.group(2)), Integer.parseInt(day.group(3)));
        } catch (DateTimeException impossible) {
            throw new IllegalArgumentException("impossible date");
        }
        if (date.isBefore(EARLIEST) || date.isAfter(latest)) {
            throw new IllegalArgumentException("date out of range");
        }
        Matcher value = VALUE.matcher(parts[2]);
        if (!value.find()) {
            throw new IllegalArgumentException("no number found in '" + parts[2].strip() + "'");
        }
        double number = Double.parseDouble(value.group(1).replace(",", ""));
        String raw = value.group(2);
        String name = raw != null ? raw : DEFAULT_UNIT.get(metric);
        Unit unit = name == null ? null : UNITS.get(name);
        if (unit == null) {
            throw new IllegalArgumentException(raw == null ? "no unit for " + metric : "unknown unit '" + raw + "'");
        }
        double total = number * unit.factor();
        if (!Double.isFinite(total) || total < 0) {
            throw new IllegalArgumentException("value must be a non-negative number");
        }
        return new HealthDataStore.Daily(metric, date, unit.name(), total, null, null, null, 1);
    }
}
