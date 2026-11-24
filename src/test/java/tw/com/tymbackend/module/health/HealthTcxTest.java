package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HealthTcxTest {
    /** Lap numbers copied from a real WorkOutDoors export (a hike); coordinates are fake. */
    private static final String HIKE = tcx("Hiking", "2026-11-21T02:00:48.304Z",
            lap("2026-11-21T02:00:48.304Z", 2982, 2140, 259)
                    + lap("2026-11-21T02:50:48.000Z", 1, 0, 1)
                    + lap("2026-11-21T02:50:55.000Z", 1, 0, 0)
                    + lap("2026-11-21T02:51:01.000Z", 2154, 1353, 138)
                    + lap("2026-11-21T03:26:30.000Z", 15760, 13056, 935));

    private static String lap(String start, int seconds, int meters, int calories) {
        return "<Lap StartTime=\"" + start + "\"><TotalTimeSeconds>" + seconds + "</TotalTimeSeconds>"
                + "<DistanceMeters>" + meters + "</DistanceMeters><Calories>" + calories + "</Calories>"
                + "<Track><Trackpoint><Time>" + start + "</Time><Position><LatitudeDegrees>0.0</LatitudeDegrees>"
                + "<LongitudeDegrees>0.0</LongitudeDegrees></Position><DistanceMeters>99999</DistanceMeters></Trackpoint></Track></Lap>";
    }

    private static String tcx(String sport, String id, String laps) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TrainingCenterDatabase xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\">"
                + "<Activities><Activity Sport=\"" + sport + "\"><Id>" + id + "</Id>" + laps + "</Activity></Activities></TrainingCenterDatabase>";
    }

    private static List<HealthDataStore.Workout> parse(String xml) {
        return HealthTcx.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String refused(String xml) {
        return assertThrows(IllegalArgumentException.class, () -> parse(xml)).getMessage();
    }

    @Test
    void sumsTheLapsAndIgnoresTrackpointDistances() {
        var rows = parse(HIKE);
        assertEquals(1, rows.size());
        var w = rows.get(0);
        assertEquals("Hiking", w.activity());
        assertEquals(16549, w.distanceM(), 1e-9);
        assertEquals(20898, w.durationS(), 1e-9);
        assertEquals(LocalDate.of(2026, 11, 21), w.day());
    }

    @Test
    void usesTheSameKeyAsTheExportImportSoReuploadsOverwriteInsteadOfDuplicating() {
        // import_apple_health.py: sha256("<activity>|<start epoch seconds>"); this hike already exists in the database under this key.
        assertEquals("69f9765cb9cbce339948af3eca3c941e0c2f740fba91a48b469a491c3bd94e90", parse(HIKE).get(0).sourceId());
        assertEquals(parse(HIKE).get(0).sourceId(), parse(HIKE).get(0).sourceId());
    }

    @Test
    void doesNotStoreEnergyBecauseTcxCaloriesIncludeBasalEnergy() {
        assertNull(parse(HIKE).get(0).energyKcal());
    }

    @Test
    void mapsCyclingAndKeepsRunning() {
        assertEquals("Cycling", parse(tcx("Biking", "2026-11-21T02:00:00Z", lap("2026-11-21T02:00:00Z", 600, 3000, 50))).get(0).activity());
        assertEquals("Running", parse(tcx("Running", "2026-11-21T02:00:00Z", lap("2026-11-21T02:00:00Z", 600, 3000, 50))).get(0).activity());
    }

    @Test
    void dayIsTheTaipeiCalendarDate() {
        var late = parse(tcx("Running", "2026-11-20T17:30:00Z", lap("2026-11-20T17:30:00Z", 600, 3000, 50)));
        assertEquals(LocalDate.of(2026, 11, 21), late.get(0).day());
    }

    @Test
    void refusesFilesThatAreNotAWorkout() {
        assertTrue(refused("hello").length() > 0);
        assertTrue(refused(tcx("Running", "2026-11-21T02:00:00Z", "")).contains("Lap"));
        assertTrue(refused(tcx("Running", "not-a-time", lap("2026-11-21T02:00:00Z", 600, 3000, 50))).contains("Id"));
        assertTrue(refused(tcx("Running", "2026-11-21T02:00:00Z", lap("2026-11-21T02:00:00Z", 0, 0, 0))).contains("時間"));
    }

    @Test
    void refusesDoctypeSoExternalEntitiesCannotBeRead() {
        String evil = "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]>" + HIKE.substring(HIKE.indexOf("<TrainingCenterDatabase"));
        assertThrows(IllegalArgumentException.class, () -> parse(evil));
    }
}
