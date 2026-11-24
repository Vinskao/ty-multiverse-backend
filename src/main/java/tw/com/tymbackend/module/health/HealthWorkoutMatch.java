package tw.com.tymbackend.module.health;

import java.util.List;

/**
 * Decides whether an incoming workout is one the database already holds under another key. The Apple Health export and a TCX
 * upload describe the same run but can disagree on the start second, which changes the key; without this the run would be
 * counted twice. Two records are the same workout when the day and activity match (the caller already narrows to those) and
 * both the distance (3%) and the time (5%) are close. Different sessions on one day differ by far more than that.
 */
final class HealthWorkoutMatch {
    private static final double DISTANCE_TOLERANCE = 0.03;
    private static final double DURATION_TOLERANCE = 0.05;

    record Known(String sourceId, Double distanceM, double durationS) {}

    private HealthWorkoutMatch() {}

    static String keyFor(HealthDataStore.Workout incoming, List<Known> sameDayRows) {
        String best = incoming.sourceId();
        double bestScore = Double.MAX_VALUE;
        for (Known row : sameDayRows) {
            if (row.sourceId().equals(incoming.sourceId())) {
                return incoming.sourceId();
            }
            double time = relative(incoming.durationS(), row.durationS());
            double distance = distance(incoming.distanceM(), row.distanceM());
            if (time <= DURATION_TOLERANCE && distance <= DISTANCE_TOLERANCE && time + distance < bestScore) {
                bestScore = time + distance;
                best = row.sourceId();
            }
        }
        return best;
    }

    /** Relative difference; 0 when neither has a distance (gym work), infinite when only one has. */
    private static double distance(Double a, Double b) {
        double x = a == null ? 0 : a;
        double y = b == null ? 0 : b;
        if (x <= 0 && y <= 0) {
            return 0;
        }
        return x <= 0 || y <= 0 ? Double.POSITIVE_INFINITY : relative(x, y);
    }

    private static double relative(double a, double b) {
        return Math.abs(a - b) / Math.max(a, b);
    }
}
