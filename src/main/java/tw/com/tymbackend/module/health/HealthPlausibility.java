package tw.com.tymbackend.module.health;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Refuses a workout whose speed cannot be real, such as a GPS file whose distance jumped, while keeping the real ones that look
 * odd by total time (a trail run with long stops, or a watch left running for 19 hours after the run).
 *
 * <p>Two layers, both on the moving speed and separate for each sport so a ride is never judged by running limits:
 * <ol>
 *   <li>physical limits per sport. Without a moving time only the upper limit is used, because the elapsed speed can never beat
 *       the moving speed, so an elapsed speed over the limit is certainly wrong.</li>
 *   <li>the owner's own history: the modified z-score (median and MAD of the log of the speed, which outliers cannot stretch the
 *       way they stretch a standard deviation) above 4 is refused, but only once the sport has 20 speeds on record.</li>
 * </ol>
 */
final class HealthPlausibility {
    static final int MIN_HISTORY = 20;
    static final double Z_LIMIT = 4;

    record Limits(String label, double minKmh, double maxKmh) {}

    private static final Map<String, Limits> LIMITS = Map.of(
            "Running", new Limits("跑步", 2, 22),
            "Hiking", new Limits("健行", 0.8, 9),
            "Walking", new Limits("步行", 1, 8),
            "Cycling", new Limits("騎車", 3, 45));

    private HealthPlausibility() {}

    /**
     * Empty when the workout is believable, otherwise why it was refused. The moving speed is the moving distance over the moving
     * time (the file's total distance can be inflated by GPS drift while the watch stood still); {@code historyKmh} holds this
     * sport's earlier moving speeds.
     */
    static Optional<String> check(String activity, double distanceM, Double movingM, Double movingS, double elapsedS, List<Double> historyKmh) {
        Limits limits = LIMITS.get(activity);
        if (limits == null || distanceM <= 0 || elapsedS <= 0) {
            return Optional.empty();
        }
        double elapsedKmh = distanceM / elapsedS * 3.6;
        if (elapsedKmh > limits.maxKmh()) {
            return Optional.of(too(limits, elapsedKmh, "超過上限 " + format(limits.maxKmh()) + " km/h"));
        }
        if (movingS == null || movingS <= 0) {
            return Optional.empty();
        }
        double movingKmh = (movingM != null && movingM > 0 ? movingM : distanceM) / movingS * 3.6;
        if (movingKmh > limits.maxKmh() || movingKmh < limits.minKmh()) {
            return Optional.of(too(limits, movingKmh, "合理範圍是 " + format(limits.minKmh()) + "–" + format(limits.maxKmh()) + " km/h"));
        }
        return outlier(limits, movingKmh, historyKmh);
    }

    private static Optional<String> outlier(Limits limits, double kmh, List<Double> history) {
        if (history.size() < MIN_HISTORY) {
            return Optional.empty();
        }
        double[] logs = history.stream().mapToDouble(Math::log).toArray();
        double median = median(logs);
        double mad = median(Arrays.stream(logs).map(v -> Math.abs(v - median)).toArray());
        if (mad <= 0) {
            return Optional.empty();
        }
        double z = 0.6745 * (Math.log(kmh) - median) / mad;
        return Math.abs(z) > Z_LIMIT
                ? Optional.of(limits.label() + "移動速率 " + format(kmh) + " km/h 與你過去的紀錄差太多（修正 z 分數 " + format(z) + "，上限 " + format(Z_LIMIT) + "）")
                : Optional.empty();
    }

    private static String too(Limits limits, double kmh, String detail) {
        return limits.label() + "平均速率 " + format(kmh) + " km/h 不合理，" + detail;
    }

    private static double median(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int n = sorted.length;
        return n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2;
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value).replaceAll("\\.0$", "");
    }
}
