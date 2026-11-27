package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import tw.com.tymbackend.module.health.RunningRecords.Entry;
import tw.com.tymbackend.module.health.RunningRecords.Pr;

class RunningRecordsTest {
    /** A Thursday; its week starts Monday 2026-11-23. */
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 26);

    private static Entry run(String day, double km, double minutes) {
        return new Entry(LocalDate.parse(day), km * 1000, minutes * 60, null, null, null);
    }

    private static Pr pr(RunningRecords.Records records, String key) {
        return records.prs().stream().filter(p -> p.key().equals(key)).findFirst().orElse(null);
    }

    @Test
    void of_Should_IgnoreMissingHeartRateAndHikes_When_SelectingLowestRunningHeartRate() {
        var runs = List.of(run("2026-11-20", 20, 120),
                new Entry(TODAY.minusDays(2), 7000, 0, 0.0, null, null, 115.0),
                new Entry(TODAY.minusDays(1), 4000, 1200, null, null, null, 125.0));
        var hikes = List.of(new Entry(TODAY, 9000, 5400, 0.0, null, null, 90.0));
        var records = RunningRecords.of(runs, hikes, TODAY);
        var lowest = pr(records, "lowestAvgHr");
        assertEquals(115.0, lowest.value());
        assertEquals(7.0, lowest.longest().value());
        assertNull(lowest.fastest());
        assertNull(pr(records, "mostAscentM"));
    }

    @Test
    void of_Should_ExposeSeparateDistanceAndPaceWinners_When_AscentTies() {
        var runs = List.of(new Entry(TODAY.minusDays(2), 5000, 1500, 700.0, null, null),
                new Entry(TODAY, 20_000, 4800, null, null, null));
        var hikes = List.of(new Entry(TODAY.minusDays(1), 9000, 5400, 700.0, null, null));
        com.fasterxml.jackson.databind.JsonNode climb = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()
                .valueToTree(pr(RunningRecords.of(runs, hikes, TODAY), "mostAscentM"));
        assertEquals(9.0, climb.path("longest").path("value").asDouble());
        assertEquals(300.0, climb.path("fastest").path("value").asDouble());
        assertEquals("2026-11-25", climb.path("longest").path("day").asText());
        assertEquals("2026-11-24", climb.path("fastest").path("day").asText());
    }

    @Test
    void picksTheBestSingleEffortsAndKeepsPaceToRunsOnly() {
        var runs = List.of(run("2026-10-01", 5, 30), run("2026-10-08", 12, 84), run("2026-10-15", 21.5, 150));
        var hikes = List.of(new Entry(LocalDate.parse("2026-11-01"), 30_000, 36_000, 1800.0, null, null));
        var r = RunningRecords.of(runs, hikes, TODAY);
        assertEquals(21.5, pr(r, "longestRunKm").value());
        assertEquals(30.0, pr(r, "longestHikeKm").value());
        // 5 km is too short to count; 12 km is 7:00/km and 21.5 km is 6:58/km, the faster.
        assertEquals(418.6, pr(r, "fastest10kPace").value(), 0.1);
        assertEquals(LocalDate.parse("2026-10-15"), pr(r, "fastest10kPace").day());
        assertEquals(418.6, pr(r, "fastestHalfPace").value(), 0.1);
        assertEquals(1800.0, pr(r, "mostAscentM").value());
        assertNull(pr(r, "fastest5kS"), "best efforts exist only when a track was uploaded");
    }

    @Test
    void bestSplitsComeFromTheTrackWhenThereIsOne() {
        var runs = List.of(new Entry(LocalDate.parse("2026-10-01"), 8000, 3000, 40.0, 285.0, 1560.0),
                new Entry(LocalDate.parse("2026-10-08"), 6000, 2400, null, 270.0, null));
        var r = RunningRecords.of(runs, List.of(), TODAY);
        assertEquals(270.0, pr(r, "fastest1kS").value());
        assertEquals(1560.0, pr(r, "fastest5kS").value());
    }

    @Test
    void runningAndHikingTogetherMakeTheBestWeekAndMonth() {
        var runs = List.of(run("2026-11-02", 10, 60), run("2026-11-04", 5, 30));
        var hikes = List.of(new Entry(LocalDate.parse("2026-11-07"), 16_000, 20_000, null, null, null));
        var r = RunningRecords.of(runs, hikes, TODAY);
        assertEquals(31.0, pr(r, "bestWeekKm").value());
        assertEquals(31.0, pr(r, "bestMonthKm").value());
        assertEquals(LocalDate.parse("2026-11-02"), pr(r, "bestWeekKm").day());
    }

    @Test
    void weekStreakCountsConsecutiveWeeksAboveTheDistance() {
        // Weeks starting 11-02, 11-09, 11-16 reach 20 km; this week (11-23) has only 6 so far but is still in progress.
        var runs = List.of(run("2026-11-03", 20, 120), run("2026-11-10", 25, 150), run("2026-11-17", 21, 126), run("2026-11-23", 6, 40));
        var r = RunningRecords.of(runs, List.of(), TODAY);
        var twenty = r.weekStreaks().stream().filter(s -> s.km() == 20).findFirst().orElseThrow();
        assertEquals(3, twenty.current(), "this week is unfinished, so the streak ends with last week");
        assertEquals(3, twenty.best());
        var thirty = r.weekStreaks().stream().filter(s -> s.km() == 30).findFirst().orElseThrow();
        assertEquals(0, thirty.current());
        assertEquals(0, thirty.best());
    }

    @Test
    void reachingTheDistanceThisWeekExtendsTheStreakAndAGapBreaksIt() {
        var runs = List.of(run("2026-10-20", 12, 70), run("2026-11-10", 12, 70), run("2026-11-17", 12, 70), run("2026-11-23", 11, 66));
        var ten = RunningRecords.of(runs, List.of(), TODAY).weekStreaks().get(0);
        assertEquals(10, ten.km());
        assertEquals(3, ten.current(), "11-10, 11-17 and the finished 11-23 week");
        assertEquals(3, ten.best());
    }

    @Test
    void dayStreakStaysAliveUntilTheDayIsOver() {
        var runs = List.of(run("2026-11-23", 5, 30), run("2026-11-24", 5, 30), run("2026-11-25", 5, 30), run("2026-11-01", 5, 30));
        var r = RunningRecords.of(runs, List.of(), TODAY);
        assertEquals(3, r.dayStreak(), "nothing yet today, but yesterday still counts");
        assertEquals(3, r.bestDayStreak());
    }

    @Test
    void monthsListTwelveMonthsWithRunningAndHikingSeparate() {
        var runs = List.of(run("2026-11-03", 10, 60), run("2026-11-20", 5, 30), run("2025-10-01", 99, 600));
        var hikes = List.of(new Entry(LocalDate.parse("2026-11-07"), 16_000, 20_000, null, null, null));
        var months = RunningRecords.of(runs, hikes, TODAY).months();
        assertEquals(12, months.size());
        assertEquals("2025-12", months.get(0).month());
        assertEquals("2026-11", months.get(11).month());
        assertEquals(15.0, months.get(11).runKm());
        assertEquals(16.0, months.get(11).hikeKm());
        assertEquals(0.0, months.get(0).runKm(), "a run from before the window is not shown");
    }

    @Test
    void emptyHistoryGivesEmptyRecords() {
        var r = RunningRecords.of(List.of(), List.of(), TODAY);
        assertTrue(r.prs().isEmpty());
        assertEquals(0, r.dayStreak());
        assertEquals(12, r.months().size());
        assertTrue(r.weekStreaks().stream().allMatch(s -> s.current() == 0 && s.best() == 0));
    }

    @Test
    void fewestBeatsPerKmIsNotWonByASlowJogOrAShortRun() {
        var runs = List.of(new Entry(LocalDate.parse("2026-11-01"), 6000, 3600, null, null, null, 110.0),   // slow: 10:00/km, 1100 beats/km
                new Entry(LocalDate.parse("2026-11-08"), 10_000, 3000, null, null, null, 150.0),            // 5:00/km, 750 beats/km
                new Entry(LocalDate.parse("2026-11-15"), 3000, 600, null, null, null, 100.0));              // under 5 km
        var best = pr(RunningRecords.of(runs, List.of(), TODAY), "fewestBeatsPerKm");
        assertEquals(750.0, best.value(), 0.01);
        assertEquals(LocalDate.parse("2026-11-08"), best.day());
        assertEquals(10.0, best.longest().value());
        assertEquals(300.0, best.fastest().value());
    }
}
