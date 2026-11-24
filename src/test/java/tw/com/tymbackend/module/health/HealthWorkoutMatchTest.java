package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HealthWorkoutMatchTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 21);

    private static HealthDataStore.Workout incoming(String key, Double meters, double seconds) {
        return new HealthDataStore.Workout(key, "Running", DAY, meters, seconds, null);
    }

    private static HealthWorkoutMatch.Known known(String key, Double meters, double seconds) {
        return new HealthWorkoutMatch.Known(key, meters, seconds);
    }

    @Test
    void sameKeyIsTheSameWorkout() {
        assertEquals("a", HealthWorkoutMatch.keyFor(incoming("a", 5000.0, 1800), List.of(known("a", 4000.0, 1000))));
    }

    @Test
    void aSecondRecordOfTheSameRunUpdatesTheExistingRowInsteadOfAddingADouble() {
        // The Apple export and the TCX upload can disagree on the start second, which changes the key; the numbers still match.
        assertEquals("export", HealthWorkoutMatch.keyFor(incoming("tcx", 5010.0, 1805), List.of(known("export", 5000.0, 1800))));
    }

    @Test
    void twoDifferentRunsOnTheSameDayStaySeparate() {
        assertEquals("tcx", HealthWorkoutMatch.keyFor(incoming("tcx", 5000.0, 1800), List.of(known("morning", 3000.0, 1000))));
        // Same distance but clearly different time is a different session.
        assertEquals("tcx", HealthWorkoutMatch.keyFor(incoming("tcx", 5000.0, 1800), List.of(known("other", 5000.0, 2600))));
    }

    @Test
    void closeButNotEqualNumbersAreNotMerged() {
        assertEquals("tcx", HealthWorkoutMatch.keyFor(incoming("tcx", 5000.0, 1800), List.of(known("other", 5400.0, 1800))));
    }

    @Test
    void picksTheClosestWhenSeveralLookAlike() {
        assertEquals("close", HealthWorkoutMatch.keyFor(incoming("tcx", 5000.0, 1800),
                List.of(known("far", 5100.0, 1830), known("close", 5001.0, 1800))));
    }

    @Test
    void workoutsWithoutDistanceMatchOnTimeAlone() {
        assertEquals("gym", HealthWorkoutMatch.keyFor(incoming("tcx", 0.0, 3600), List.of(known("gym", null, 3590))));
        assertEquals("tcx", HealthWorkoutMatch.keyFor(incoming("tcx", 0.0, 3600), List.of(known("run", 5000.0, 3590))));
    }

    @Test
    void nothingKnownKeepsTheNewKey() {
        assertEquals("tcx", HealthWorkoutMatch.keyFor(incoming("tcx", 5000.0, 1800), List.of()));
    }
}
