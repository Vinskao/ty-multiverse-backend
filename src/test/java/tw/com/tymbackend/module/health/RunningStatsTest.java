package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import tw.com.tymbackend.module.health.RunningStats.Run;

class RunningStatsTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 23);

    @Test
    void sumsRunsPerDayAndPerMondayWeek() {
        var s = RunningStats.of(List.of(new Run(MONDAY.minusDays(1), 10000, 3000), new Run(MONDAY, 4000, 1200),
                new Run(MONDAY, 7000, 2450)), MONDAY.plusDays(2));
        assertEquals(new RunningStats.DayDistance(MONDAY, 11.0), s.peakDay());
        assertEquals(11.0, s.thisWeekKm());
        assertEquals(new RunningStats.Week(MONDAY, 11.0), s.peakWeek());
        assertEquals(12, s.weeks().size());
        assertEquals(10.0, s.weeks().get(10).km());
        assertEquals(21.0, s.totalKm());
        assertEquals(MONDAY, s.lastRun());
    }

    @Test
    void fastestIgnoresShortSprints() {
        var s = RunningStats.of(List.of(new Run(MONDAY, 400, 60), new Run(MONDAY, 5000, 1500), new Run(MONDAY, 3000, 960)), MONDAY);
        assertEquals(300.0, s.fastest().secondsPerKm());
        assertEquals(5.0, s.fastest().km());
    }

    @Test
    void datesAreIsoStringsEvenWhenTheMapperWritesTimestamps() throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()
                .enable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var json = mapper.readTree(mapper.writeValueAsString(RunningStats.of(List.of(new Run(MONDAY, 5000, 1500)), MONDAY)));
        assertEquals("2026-11-23", json.at("/peakDay/day").asText());
        assertEquals("2026-11-23", json.at("/fastest/day").asText());
        assertEquals("2026-11-23", json.at("/peakWeek/start").asText());
        assertEquals("2026-11-23", json.at("/weeks/11/start").asText());
        assertEquals("2026-11-23", json.at("/lastRun").asText());
    }

    @Test
    void emptyHistoryHasNoRecords() {
        var s = RunningStats.of(List.of(), MONDAY);
        assertEquals(0, s.runs());
        assertNull(s.peakDay());
        assertNull(s.fastest());
        assertNull(s.peakWeek());
        assertNull(s.lastRun());
    }
}
