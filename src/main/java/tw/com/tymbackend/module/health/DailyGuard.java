package tw.com.tymbackend.module.health;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Stops a past day's total from being overwritten by a value that jumped by half or more, which is what a double count looks
 * like (phone and watch both counted, or a missing source filter). A smaller value always goes through, and so does today,
 * because the last few days are still being completed by later runs. The jump must also exceed a small amount per metric so normal phone-versus-watch
 * disagreement on tiny counts (3 versus 5 flights) is not blocked.
 */
final class DailyGuard {
    private static final double RATIO = 1.5;
    /**
     * The shortcut runs every second day, so a day is first stored part-way (about 14:00) and only completed by a later run.
     * Days this recent are therefore allowed to grow by any amount; only older, settled days are protected.
     */
    private static final int SETTLE_DAYS = 3;
    /** Smallest increase worth blocking, in the unit each metric is stored in. */
    private static final Map<String, Double> FLOOR = Map.of("StepCount", 500.0, "DistanceWalkingRunning", 1.0,
            "ActiveEnergyBurned", 50.0, "AppleExerciseTime", 10.0, "FlightsClimbed", 5.0);

    record Result(List<HealthDataStore.Daily> accepted, List<String> skipped) {}

    private DailyGuard() {}

    static String key(String metric, LocalDate day) {
        return metric + "|" + day;
    }

    static Result check(List<HealthDataStore.Daily> incoming, Map<String, Double> existing, LocalDate today) {
        List<HealthDataStore.Daily> accepted = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (var row : incoming) {
            Double old = existing.get(key(row.metric(), row.day()));
            if (blocked(row, old, today)) {
                skipped.add(String.format(Locale.ROOT, "%s %s: 收到 %.1f，已有 %.1f（超過 1.5 倍，未覆寫）",
                        row.metric(), row.day(), row.total(), old));
            } else {
                accepted.add(row);
            }
        }
        return new Result(accepted, skipped);
    }

    private static boolean blocked(HealthDataStore.Daily row, Double old, LocalDate today) {
        if (old == null || old <= 0 || row.total() == null || !row.day().isBefore(today.minusDays(SETTLE_DAYS))) {
            return false;
        }
        double floor = FLOOR.getOrDefault(row.metric(), Double.MAX_VALUE);
        return row.total() > old * RATIO && row.total() - old >= floor;
    }
}
