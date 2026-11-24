package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HealthKindTest {
    @Test
    void aRunWithLittleClimbingIsARoadRun() {
        assertEquals("RoadRun", HealthKind.of("Running", 10_000.0, 50.0));
    }

    @Test
    void aRunThatClimbsALotIsATrailRun() {
        assertEquals("TrailRun", HealthKind.of("Running", 10_000.0, 400.0));
        // Steep but short still counts when the climb per kilometre is high enough.
        assertEquals("TrailRun", HealthKind.of("Running", 6_000.0, 180.0));
    }

    @Test
    void aShortSteepBlipIsNotATrailRun() {
        assertEquals("RoadRun", HealthKind.of("Running", 1_500.0, 60.0));
    }

    @Test
    void theSportTheOwnerChoseWinsForOtherTypes() {
        assertEquals("Hike", HealthKind.of("Hiking", 16_500.0, 340.0));
        assertEquals("Walk", HealthKind.of("Walking", 3_000.0, 10.0));
        assertEquals("Ride", HealthKind.of("Cycling", 20_000.0, 100.0));
        assertEquals("Other", HealthKind.of("CoreTraining", null, null));
    }

    @Test
    void aRunWithoutElevationDataIsOnlyKnownAsARun() {
        // Workouts imported from the Apple Health export carry no climb, so they cannot be split into road and trail yet.
        assertEquals("Run", HealthKind.of("Running", 8_000.0, null));
    }
}
