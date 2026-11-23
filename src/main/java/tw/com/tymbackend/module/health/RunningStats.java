package tw.com.tymbackend.module.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Pure running statistics. Weeks start on Monday; a fastest run needs a minimum distance so a sprint does not win. */
final class RunningStats {
    static final double FASTEST_MIN_METERS = 3000;
    static final int WEEKS = 12;

    private RunningStats() {}

    record Run(LocalDate day, double meters, double seconds) {}
    record DayDistance(@JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate day, double km) {}
    record Fastest(@JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate day, double km, double secondsPerKm) {}
    record Week(@JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate start, double km) {}
    record Summary(int runs, double totalKm, DayDistance peakDay, Fastest fastest, double thisWeekKm, Week peakWeek,
                   List<Week> weeks, @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate lastRun) {}

    static Summary of(List<Run> all, LocalDate today) {
        List<Run> runs = all.stream().filter(run -> run.meters() > 0 && run.seconds() > 0).toList();
        Map<LocalDate, Double> days = new TreeMap<>();
        Map<LocalDate, Double> weeks = new TreeMap<>();
        for (Run run : runs) {
            days.merge(run.day(), run.meters() / 1000, Double::sum);
            weeks.merge(monday(run.day()), run.meters() / 1000, Double::sum);
        }
        DayDistance peakDay = days.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(e -> new DayDistance(e.getKey(), e.getValue())).orElse(null);
        Week peakWeek = weeks.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(e -> new Week(e.getKey(), e.getValue())).orElse(null);
        Fastest fastest = runs.stream().filter(run -> run.meters() >= FASTEST_MIN_METERS)
                .map(run -> new Fastest(run.day(), run.meters() / 1000, run.seconds() / (run.meters() / 1000)))
                .min(Comparator.comparingDouble(Fastest::secondsPerKm)).orElse(null);
        LocalDate thisWeek = monday(today);
        List<Week> recent = new ArrayList<>();
        for (int i = WEEKS - 1; i >= 0; i--) {
            LocalDate start = thisWeek.minusWeeks(i);
            recent.add(new Week(start, weeks.getOrDefault(start, 0.0)));
        }
        return new Summary(runs.size(), days.values().stream().mapToDouble(Double::doubleValue).sum(), peakDay, fastest,
                weeks.getOrDefault(thisWeek, 0.0), peakWeek, recent,
                runs.stream().map(Run::day).max(Comparator.naturalOrder()).orElse(null));
    }

    private static LocalDate monday(LocalDate day) {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
