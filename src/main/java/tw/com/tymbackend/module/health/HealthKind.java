package tw.com.tymbackend.module.health;

/**
 * The strict class of a workout, used for statistics. The sport the owner chose in the tracking app decides everything except
 * running, which is split into road and trail by how much it climbs: a trail run climbs at least 300 m, or at least 25 m per
 * kilometre over 3 km or more. A run whose climb is unknown (imported from the Apple Health export, which has no elevation) is
 * only "Run" until a TCX upload supplies it.
 */
final class HealthKind {
    static final double TRAIL_ASCENT_M = 300;
    static final double TRAIL_ASCENT_PER_KM = 25;
    static final double TRAIL_MIN_DISTANCE_M = 3000;

    private HealthKind() {}

    static String of(String activity, Double distanceM, Double ascentM) {
        return switch (activity) {
            case "Running" -> run(distanceM, ascentM);
            case "Hiking" -> "Hike";
            case "Walking" -> "Walk";
            case "Cycling" -> "Ride";
            default -> "Other";
        };
    }

    private static String run(Double distanceM, Double ascentM) {
        if (ascentM == null) {
            return "Run";
        }
        boolean steepEnough = distanceM != null && distanceM >= TRAIL_MIN_DISTANCE_M
                && ascentM / (distanceM / 1000) >= TRAIL_ASCENT_PER_KM;
        return ascentM >= TRAIL_ASCENT_M || steepEnough ? "TrailRun" : "RoadRun";
    }
}
