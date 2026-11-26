package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.Test;

class DailyGuardTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 24);

    private static HealthDataStore.Daily daily(String metric, String day, String unit, double total) {
        return new HealthDataStore.Daily(metric, LocalDate.parse(day), unit, total, null, null, null, 1);
    }

    @Test
    void aPastDayThatSuddenlyDoublesIsNotOverwritten() {
        // The classic mistake: phone and watch both counted, so the shortcut sends about twice the real steps.
        var rows = List.of(daily("StepCount", "2026-11-16", "count", 10372), daily("StepCount", "2026-11-17", "count", 4300));
        var result = DailyGuard.check(rows, Map.of("StepCount|2026-11-16", 5432.0, "StepCount|2026-11-17", 4218.0), TODAY);
        assertEquals(1, result.accepted().size());
        assertEquals(LocalDate.parse("2026-11-17"), result.accepted().get(0).day());
        assertEquals(1, result.skipped().size());
        assertTrue(result.skipped().get(0).contains("StepCount") && result.skipped().get(0).contains("2026-11-16"));
    }

    @Test
    void valuesThatShrinkOrGrowALittleAreFine() {
        var rows = List.of(daily("StepCount", "2026-11-18", "count", 5000), daily("StepCount", "2026-11-19", "count", 6200));
        var result = DailyGuard.check(rows, Map.of("StepCount|2026-11-18", 8011.0, "StepCount|2026-11-19", 4559.0), TODAY);
        assertEquals(2, result.accepted().size());
        assertTrue(result.skipped().isEmpty());
    }

    @Test
    void todayIsAllowedToGrowBecauseTheDayIsNotOver() {
        var result = DailyGuard.check(List.of(daily("StepCount", "2026-11-24", "count", 9000)), Map.of("StepCount|2026-11-24", 2000.0), TODAY);
        assertEquals(1, result.accepted().size());
        assertTrue(result.skipped().isEmpty());
    }

    @Test
    void daysWeHaveNothingForAreAccepted() {
        var result = DailyGuard.check(List.of(daily("StepCount", "2026-11-10", "count", 99999)), Map.of(), TODAY);
        assertEquals(1, result.accepted().size());
    }

    @Test
    void smallCountsDoNotTriggerTheGuard() {
        // 3 flights vs 5 flights is normal disagreement between phone and watch, not a doubling.
        var result = DailyGuard.check(List.of(daily("FlightsClimbed", "2026-11-15", "count", 5)), Map.of("FlightsClimbed|2026-11-15", 3.0), TODAY);
        assertEquals(1, result.accepted().size());
    }

    @Test
    void distanceUsesKilometreScale() {
        var rows = List.of(daily("DistanceWalkingRunning", "2026-11-20", "km", 9.0), daily("DistanceWalkingRunning", "2026-11-21", "km", 1.6));
        var result = DailyGuard.check(rows, Map.of("DistanceWalkingRunning|2026-11-20", 4.3, "DistanceWalkingRunning|2026-11-21", 1.0), TODAY);
        assertEquals(1, result.accepted().size());
        assertEquals(LocalDate.parse("2026-11-21"), result.accepted().get(0).day());
        assertEquals(1, result.skipped().size());
    }

    @Test
    void aDayStoredPartWayThroughIsCompletedByTheNextRuns() {
        // Stored at 14:00 as 1.83 km, the real day was 10.1 km: the following runs must be allowed to fix it for a few days.
        var rows = List.of(daily("DistanceWalkingRunning", "2026-11-22", "km", 18.2), daily("DistanceWalkingRunning", "2026-11-21", "km", 18.2));
        var result = DailyGuard.check(rows, Map.of("DistanceWalkingRunning|2026-11-22", 5.0, "DistanceWalkingRunning|2026-11-21", 5.0), TODAY);
        assertEquals(2, result.accepted().size());
    }
}
