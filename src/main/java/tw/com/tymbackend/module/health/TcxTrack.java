package tw.com.tymbackend.module.health;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The points of one workout, reduced to the numbers worth keeping. Nothing about where the workout happened is kept: the
 * latitude and longitude are never read, only time, cumulative distance, altitude and heart rate.
 *
 * <p>Calibrated on a real WorkOutDoors hike whose screen showed 336 m up, 459 m down, 103 average and 153 maximum heart rate:
 * a 2.5 m hysteresis on altitude gives 335 and 459, and a time-weighted average that skips dropouts gives 103.
 */
final class TcxTrack {
    static final double CLIMB_HYSTERESIS_M = 2.5;
    static final int HR_BIN = 5;
    private static final int HR_MIN = 30;
    private static final int HR_MAX = 230;
    private static final double MAX_GAP_S = 30;
    private static final double MAX_SPEED_MS = 8;
    private static final double MAX_HR_WEIGHT_S = 10;
    /** Slower than this between two points counts as standing still (about 1 km/h). */
    private static final double MIN_MOVING_MS = 0.3;
    /**
     * When the recorded time is longer than this many times the moving time (5: real stops and slow climbs stay well below it, a forgotten timer is far above it) (a watch left running), heart rate is taken from
     * the moving part only, and the public statistics use the moving distance and time instead of the recorded ones.
     */
    static final double PAUSE_RATIO = 5;

    record Summary(Double avgHr, Integer maxHr, Map<Integer, Integer> hrSeconds, Double ascentM, Double descentM,
                   Double best1kS, Double best5kS, Double movingS, Double movingM) {}

    private final List<double[]> points = new ArrayList<>(); // time s, distance m, altitude m (NaN if none), heart rate

    void add(double timeS, double distanceM, double altitudeM, int heartRate) {
        points.add(new double[]{timeS, distanceM, altitudeM, heartRate});
    }

    double lastTime() {
        return points.isEmpty() ? 0 : points.get(points.size() - 1)[0];
    }

    double lastDistance() {
        return points.isEmpty() ? 0 : points.get(points.size() - 1)[1];
    }

    Summary summarize() {
        double[] moving = moving();
        double elapsed = points.isEmpty() ? 0 : lastTime() - points.get(0)[0];
        boolean movingOnly = moving != null && elapsed > PAUSE_RATIO * moving[0];
        double weighted = 0;
        double weight = 0;
        int max = 0;
        TreeMap<Integer, Double> bins = new TreeMap<>();
        for (int i = 0; i < points.size() - 1; i++) {
            int hr = (int) points.get(i)[3];
            if (hr < HR_MIN || hr > HR_MAX || (movingOnly && !movingStep(i))) {
                continue; // sensor dropout, or standing still while the timer ran on
            }
            double dt = Math.min(points.get(i + 1)[0] - points.get(i)[0], MAX_HR_WEIGHT_S);
            if (dt <= 0) {
                continue;
            }
            weighted += hr * dt;
            weight += dt;
            max = Math.max(max, hr);
            bins.merge(hr / HR_BIN * HR_BIN, dt, Double::sum);
        }
        Map<Integer, Integer> seconds = new TreeMap<>();
        bins.forEach((bin, s) -> {
            if (Math.round(s) > 0) {
                seconds.put(bin, (int) Math.round(s));
            }
        });
        double[] climb = climb();
        return new Summary(weight > 0 ? weighted / weight : null, weight > 0 ? max : null, seconds,
                climb == null ? null : climb[0], climb == null ? null : climb[1], best(1000), best(5000),
                moving == null ? null : moving[0], moving == null ? null : moving[1]);
    }

    /** Whether the step from point i to point i + 1 is real movement: at most 30 s long, between about 1 km/h and 8 m/s. */
    private boolean movingStep(int i) {
        double dt = points.get(i + 1)[0] - points.get(i)[0];
        double speed = dt > 0 ? (points.get(i + 1)[1] - points.get(i)[1]) / dt : 0;
        return dt > 0 && dt <= MAX_GAP_S && speed >= MIN_MOVING_MS && speed <= MAX_SPEED_MS;
    }

    /**
     * Seconds and metres spent actually moving, or null when there are no steps. A watch left running after the workout, or a long
     * stop, adds elapsed time; GPS drift while standing still adds distance too, which is why the distance is summed over moving
     * steps only instead of being taken from the file's total.
     */
    private double[] moving() {
        if (points.size() < 2) {
            return null;
        }
        double seconds = 0;
        double meters = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            if (movingStep(i)) {
                seconds += points.get(i + 1)[0] - points.get(i)[0];
                meters += points.get(i + 1)[1] - points.get(i)[1];
            }
        }
        return new double[]{seconds, meters};
    }

    /** Ascent and descent in metres, ignoring wobble smaller than the hysteresis. Null when there is no altitude. */
    private double[] climb() {
        double up = 0;
        double down = 0;
        Double ref = null;
        int seen = 0;
        for (double[] p : points) {
            double alt = p[2];
            if (Double.isNaN(alt)) {
                continue;
            }
            seen++;
            if (ref == null) {
                ref = alt;
            } else if (alt - ref >= CLIMB_HYSTERESIS_M) {
                up += alt - ref;
                ref = alt;
            } else if (ref - alt >= CLIMB_HYSTERESIS_M) {
                down += ref - alt;
                ref = alt;
            }
        }
        return seen < 2 ? null : new double[]{up, down};
    }

    /**
     * Fastest time over the given distance, or null if the workout never covered it. A step that is implausible (a pause longer
     * than 30 s, or faster than 8 m/s, which is a GPS jump) contributes no distance but its time still counts, so a glitch can
     * only make an effort slower, never produce an impossible record.
     */
    private Double best(double window) {
        int n = points.size();
        if (n < 2) {
            return null;
        }
        double[] repaired = new double[n];
        for (int i = 1; i < n; i++) {
            double dt = points.get(i)[0] - points.get(i - 1)[0];
            double dd = points.get(i)[1] - points.get(i - 1)[1];
            boolean plausible = dt > 0 && dt <= MAX_GAP_S && dd >= 0 && dd / dt <= MAX_SPEED_MS;
            repaired[i] = repaired[i - 1] + (plausible ? dd : 0);
        }
        Double best = null;
        int j = 0;
        for (int i = 0; i < n; i++) {
            while (j < n && repaired[j] - repaired[i] < window) {
                j++;
            }
            if (j >= n) {
                break;
            }
            double covered = repaired[j] - repaired[i];
            double seconds = (points.get(j)[0] - points.get(i)[0]) * window / covered;
            if (best == null || seconds < best) {
                best = seconds;
            }
        }
        return best;
    }
}
