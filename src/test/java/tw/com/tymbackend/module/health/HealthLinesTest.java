package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HealthLinesTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 23);

    private static List<HealthDataStore.Daily> parse(String text) {
        return HealthLines.parse(text, TODAY);
    }

    private static String refused(String text) {
        return assertThrows(IllegalArgumentException.class, () -> parse(text)).getMessage();
    }

    @Test
    void readsLocalizedDatesGroupedNumbersAndBlankLines() {
        var rows = parse("StepCount,2026/11/15 00:04,8912 count\r\nStepCount,2026年11月16日 上午1:14,10,372 count\n\n"
                + "DistanceWalkingRunning,2026-11-17,5.2 公里\n");
        assertEquals(3, rows.size());
        assertEquals(LocalDate.of(2026, 11, 15), rows.get(0).day());
        assertEquals(8912, rows.get(0).total());
        assertEquals(LocalDate.of(2026, 11, 16), rows.get(1).day());
        assertEquals(10372, rows.get(1).total());
        assertEquals("km", rows.get(2).unit());
        assertEquals(1, rows.get(0).samples());
        assertNull(rows.get(0).average());
    }

    @Test
    void normalizesUnitsToTheOnesTheImportUses() {
        assertEquals(4.989, parse("DistanceWalkingRunning,2026-11-17,3.1 mi").get(0).total(), 0.001);
        assertEquals(0.5, parse("DistanceWalkingRunning,2026-11-17,500 m").get(0).total(), 1e-9);
        assertEquals("Cal", parse("ActiveEnergyBurned,2026-11-17,380 kcal").get(0).unit());
        assertEquals(90, parse("AppleExerciseTime,2026-11-17,1.5 hr").get(0).total());
        assertEquals("min", parse("AppleExerciseTime,2026-11-17,30 分鐘").get(0).unit());
        assertEquals("count", parse("FlightsClimbed,2026-11-17,12").get(0).unit());
    }

    @Test
    void refusesWhatItCannotUnderstandAndNamesTheLine() {
        assertTrue(refused("StepCount,2026-11-17,5 count\nStepCount,昨天,100 count").startsWith("Line 2: no date"));
        assertTrue(refused("StepCount,2026-11-17,5 furlongs").contains("unknown unit 'furlongs'"));
        assertTrue(refused("Mystery,2026-11-17,5").contains("no unit"));
        assertTrue(refused("StepCount,2026-11-17").contains("expected"));
        assertTrue(refused("StepCount,2026-11-17,-5 count").contains("no number") || refused("StepCount,2026-11-17,-5 count").contains("non-negative"));
        assertTrue(refused("StepCount,2026-11-31,5 count").contains("impossible"));
        assertTrue(refused("StepCount,2026-11-24,5 count").contains("out of range"));
        assertTrue(refused("Step Count,2026-11-17,5 count").contains("metric"));
    }

    @Test
    void emptyTextHasNoRowsAndHugeTextIsRefused() {
        assertTrue(parse("  \n\n").isEmpty());
        assertTrue(refused("StepCount,2026-11-17,5 count\n".repeat(HealthLines.MAX_LINES + 1)).contains("Too many"));
    }
}
