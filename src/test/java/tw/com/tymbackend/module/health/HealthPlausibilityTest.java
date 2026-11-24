package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HealthPlausibilityTest {
    private static final List<Double> NO_HISTORY = List.of();

    /** Distance in km and moving time in hours give the moving speed; elapsed defaults to the same. */
    private static java.util.Optional<String> check(String activity, double km, double movingHours, double elapsedHours, List<Double> history) {
        return HealthPlausibility.check(activity, km * 1000, km * 1000, movingHours * 3600, elapsedHours * 3600, history);
    }

    @Test
    void cyclingAndRunningHaveTheirOwnLimits() {
        assertTrue(check("Cycling", 30, 1, 1, NO_HISTORY).isEmpty(), "30 km/h is a normal ride");
        assertTrue(check("Running", 30, 1, 1, NO_HISTORY).isPresent(), "30 km/h is not a possible run");
        assertTrue(check("Running", 10, 1, 1, NO_HISTORY).isEmpty());
        assertTrue(check("Cycling", 60, 1, 1, NO_HISTORY).isPresent(), "60 km/h average is a glitch even on a bike");
    }

    @Test
    void hikingAndWalkingAreSlowerThanRunning() {
        assertTrue(check("Hiking", 5, 1, 1, NO_HISTORY).isEmpty());
        assertTrue(check("Hiking", 12, 1, 1, NO_HISTORY).isPresent());
        assertTrue(check("Walking", 5, 1, 1, NO_HISTORY).isEmpty());
        assertTrue(check("Walking", 10, 1, 1, NO_HISTORY).isPresent());
    }

    @Test
    void aWatchLeftRunningStillCountsBecauseTheMovingSpeedIsNormal() {
        // 9 km run but the timer ran 19.4 hours: the elapsed speed is absurdly slow, the moving speed is fine.
        assertTrue(check("Running", 9.15, 1.1, 19.43, NO_HISTORY).isEmpty());
    }

    @Test
    void aSlowTrailRunWithLongStopsIsNotRejected() {
        // 10.5 km in 3:52 elapsed, 3 hours of it moving: 3.5 km/h, steep and legitimate.
        assertTrue(check("Running", 10.56, 3.0, 3.87, NO_HISTORY).isEmpty());
    }

    @Test
    void anImpossibleDistanceIsRejectedEvenWithoutAMovingTime() {
        // A file with laps only has no moving time; elapsed speed can never beat the moving speed, so the upper limit still holds.
        assertTrue(HealthPlausibility.check("Running", 50_000, null, null, 3600, NO_HISTORY).isPresent());
        assertTrue(HealthPlausibility.check("Running", 5_000, null, null, 3600, NO_HISTORY).isEmpty());
        assertTrue(HealthPlausibility.check("Running", 5_000, 0.0, 0.0, 3600, NO_HISTORY).isEmpty());
    }

    @Test
    void workoutsWithoutDistanceOrOfOtherTypesAreNotJudged() {
        assertTrue(HealthPlausibility.check("Running", 0, 50.0, 100.0, 100, NO_HISTORY).isEmpty());
        assertTrue(HealthPlausibility.check("CoreTraining", 0, 0.0, 3600.0, 3600, NO_HISTORY).isEmpty());
        assertTrue(HealthPlausibility.check("Swimming", 500_000, 500_000.0, 60.0, 60, NO_HISTORY).isEmpty());
    }

    private static List<Double> usualRunSpeeds(int count) {
        List<Double> speeds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            speeds.add(7.5 + (i % 5) * 0.25); // 7.5 to 8.5 km/h
        }
        return speeds;
    }

    @Test
    void aSpeedFarFromYourUsualOnesIsRejectedOnceThereIsEnoughHistory() {
        // 16 km/h is under the physical limit but twice anything this runner has ever done.
        assertTrue(check("Running", 16, 1, 1, usualRunSpeeds(25)).isPresent());
        assertTrue(check("Running", 8.5, 1, 1, usualRunSpeeds(25)).isEmpty());
    }

    @Test
    void withTooLittleHistoryOnlyThePhysicalLimitApplies() {
        assertTrue(check("Running", 16, 1, 1, usualRunSpeeds(5)).isEmpty());
    }

    @Test
    void theMessageSaysWhatWasWrong() {
        String message = check("Running", 30, 1, 1, NO_HISTORY).orElseThrow();
        assertTrue(message.contains("跑步") && message.contains("30"), message);
        assertTrue(check("Cycling", 60, 1, 1, NO_HISTORY).orElseThrow().contains("騎車"));
    }

    @Test
    void theMovingDistanceDecidesTheSpeedNotTheDriftInflatedTotal() {
        // The file says 9.15 km but only 6.4 km of it was real movement in 40 minutes (9.6 km/h): believable.
        assertTrue(HealthPlausibility.check("Running", 9_150, 6_400.0, 2_400.0, 70_000, NO_HISTORY).isEmpty());
        // The same total over the same moving time would look like 13.7 km/h; a stricter history would refuse that, the real one not.
        assertTrue(HealthPlausibility.check("Running", 9_150, 6_400.0, 2_400.0, 70_000, usualRunSpeeds(25)).isEmpty());
    }

    @Test
    void aMovingDistanceThatIsStillImpossibleIsRefused() {
        assertTrue(HealthPlausibility.check("Running", 30_000, 28_000.0, 3_600.0, 5 * 3600.0, NO_HISTORY).isPresent());
    }

    @Test
    void historyOfAnotherSportIsNotMixedIn() {
        // The caller passes only the history of the same activity; riding speeds must not make 16 km/h normal for a run.
        List<Double> ridingSpeeds = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            ridingSpeeds.add(15.0 + (i % 5) * 0.5);
        }
        assertTrue(check("Cycling", 16, 1, 1, ridingSpeeds).isEmpty());
        assertTrue(check("Running", 16, 1, 1, usualRunSpeeds(25)).isPresent());
    }
}
