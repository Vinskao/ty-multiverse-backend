package tw.com.tymbackend.module.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Personal records in the way Strava shows them: best single efforts, streaks of weeks above a distance, and each month's running and
 * hiking distance. Running and hiking together make up a trail runner's volume (weeks, months, streaks, most climb); the pace records
 * come from runs only. Pure, so it is easy to test.
 */
final class RunningRecords {
    static final int[] STREAK_KM = {10, 20, 30, 40};
    static final int MONTHS = 12;
    static final double HALF_MARATHON_M = 21_097.5;

    private RunningRecords() {}

    record Entry(LocalDate day, double meters, double seconds, Double ascentM, Double best1kS, Double best5kS) {}
    /** {@code key} names the record; value is km, seconds, seconds per km or metres depending on the key. */
    record Pr(String key, double value, @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate day) {}
    record Streak(int km, int current, int best) {}
    record Month(String month, double runKm, double hikeKm) {}
    record Records(List<Pr> prs, List<Streak> weekStreaks, int dayStreak, int bestDayStreak, List<Month> months) {}

    static Records of(List<Entry> runs, List<Entry> hikes, LocalDate today) {
        List<Entry> all = new ArrayList<>(runs);
        all.addAll(hikes);
        return new Records(prs(runs, hikes, all), weekStreaks(all, today), currentDayStreak(all, today), bestDayStreak(all), months(runs, hikes, today));
    }

    private static List<Pr> prs(List<Entry> runs, List<Entry> hikes, List<Entry> all) {
        List<Pr> prs = new ArrayList<>();
        runs.stream().max(Comparator.comparingDouble(Entry::meters)).ifPresent(e -> prs.add(new Pr("longestRunKm", e.meters() / 1000, e.day())));
        fastest(runs, Entry::best1kS, "fastest1kS", prs);
        fastest(runs, Entry::best5kS, "fastest5kS", prs);
        pace(runs, 10_000, "fastest10kPace", prs);
        pace(runs, HALF_MARATHON_M, "fastestHalfPace", prs);
        all.stream().filter(e -> e.ascentM() != null).max(Comparator.comparingDouble(Entry::ascentM))
                .ifPresent(e -> prs.add(new Pr("mostAscentM", e.ascentM(), e.day())));
        hikes.stream().max(Comparator.comparingDouble(Entry::meters)).ifPresent(e -> prs.add(new Pr("longestHikeKm", e.meters() / 1000, e.day())));
        weeks(all).entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(e -> prs.add(new Pr("bestWeekKm", e.getValue(), e.getKey())));
        monthTotals(all).entrySet().stream().max(Map.Entry.comparingByValue())
                .ifPresent(e -> prs.add(new Pr("bestMonthKm", e.getValue(), e.getKey().atDay(1))));
        return prs;
    }

    private static void fastest(List<Entry> runs, java.util.function.Function<Entry, Double> seconds, String key, List<Pr> prs) {
        runs.stream().filter(e -> seconds.apply(e) != null && seconds.apply(e) > 0)
                .min(Comparator.comparingDouble(e -> seconds.apply(e))).ifPresent(e -> prs.add(new Pr(key, seconds.apply(e), e.day())));
    }

    /** Fastest average pace over a whole run of at least the given distance. */
    private static void pace(List<Entry> runs, double minMeters, String key, List<Pr> prs) {
        runs.stream().filter(e -> e.meters() >= minMeters && e.seconds() > 0)
                .min(Comparator.comparingDouble(e -> e.seconds() / (e.meters() / 1000)))
                .ifPresent(e -> prs.add(new Pr(key, e.seconds() / (e.meters() / 1000), e.day())));
    }

    private static Map<LocalDate, Double> weeks(List<Entry> all) {
        Map<LocalDate, Double> weeks = new TreeMap<>();
        all.stream().filter(e -> e.meters() > 0).forEach(e -> weeks.merge(monday(e.day()), e.meters() / 1000, Double::sum));
        return weeks;
    }

    private static Map<YearMonth, Double> monthTotals(List<Entry> all) {
        Map<YearMonth, Double> months = new TreeMap<>();
        all.stream().filter(e -> e.meters() > 0).forEach(e -> months.merge(YearMonth.from(e.day()), e.meters() / 1000, Double::sum));
        return months;
    }

    /**
     * Consecutive weeks (Monday start) with at least the given distance. The current streak stays alive while this week is still in
     * progress: it counts this week once it reaches the distance, otherwise it ends with last week's result.
     */
    private static List<Streak> weekStreaks(List<Entry> all, LocalDate today) {
        Map<LocalDate, Double> weeks = weeks(all);
        LocalDate thisWeek = monday(today);
        List<Streak> streaks = new ArrayList<>();
        for (int km : STREAK_KM) {
            int best = 0;
            int run = 0;
            if (!weeks.isEmpty()) {
                for (LocalDate w = weeks.keySet().iterator().next(); !w.isAfter(thisWeek); w = w.plusWeeks(1)) {
                    run = weeks.getOrDefault(w, 0.0) >= km ? run + 1 : 0;
                    best = Math.max(best, run);
                }
            }
            int current = 0;
            LocalDate w = weeks.getOrDefault(thisWeek, 0.0) >= km ? thisWeek : thisWeek.minusWeeks(1);
            while (weeks.getOrDefault(w, 0.0) >= km) {
                current++;
                w = w.minusWeeks(1);
            }
            streaks.add(new Streak(km, current, best));
        }
        return streaks;
    }

    private static Set<LocalDate> activeDays(List<Entry> all) {
        Set<LocalDate> days = new HashSet<>();
        all.stream().filter(e -> e.meters() > 0).forEach(e -> days.add(e.day()));
        return days;
    }

    private static int currentDayStreak(List<Entry> all, LocalDate today) {
        Set<LocalDate> days = activeDays(all);
        LocalDate d = days.contains(today) ? today : today.minusDays(1);
        int count = 0;
        while (days.contains(d)) {
            count++;
            d = d.minusDays(1);
        }
        return count;
    }

    private static int bestDayStreak(List<Entry> all) {
        Set<LocalDate> days = activeDays(all);
        int best = 0;
        for (LocalDate d : days) {
            if (days.contains(d.minusDays(1))) {
                continue; // only count from the start of a streak
            }
            int length = 0;
            for (LocalDate x = d; days.contains(x); x = x.plusDays(1)) {
                length++;
            }
            best = Math.max(best, length);
        }
        return best;
    }

    private static List<Month> months(List<Entry> runs, List<Entry> hikes, LocalDate today) {
        Map<YearMonth, Double> run = monthTotals(runs);
        Map<YearMonth, Double> hike = monthTotals(hikes);
        List<Month> months = new ArrayList<>();
        for (int i = MONTHS - 1; i >= 0; i--) {
            YearMonth month = YearMonth.from(today).minusMonths(i);
            months.add(new Month(month.toString(), run.getOrDefault(month, 0.0), hike.getOrDefault(month, 0.0)));
        }
        return months;
    }

    private static LocalDate monday(LocalDate day) {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
