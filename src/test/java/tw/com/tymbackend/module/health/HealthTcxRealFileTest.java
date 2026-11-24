package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Checks the parser against a real WorkOutDoors export of a hike whose screen showed 16.55 km, 5:48:18, 336 m up, 459 m down and
 * heart rate 103 average / 153 maximum. The file holds a GPS route, so it is never committed: run with
 * -Dtcx.sample=&lt;path to the .tcx&gt; and the test is skipped otherwise.
 */
class HealthTcxRealFileTest {
    @Test
    void matchesWhatWorkOutDoorsShowedOnScreen() throws IOException {
        String sample = System.getProperty("tcx.sample");
        assumeTrue(sample != null && Files.exists(Path.of(sample)), "no -Dtcx.sample given");
        try (var in = Files.newInputStream(Path.of(sample))) {
            var parsed = HealthTcx.parseDetailed(in);
            var w = parsed.get(0).workout();
            var t = parsed.get(0).detail().track();
            System.out.printf("REAL %s %s dist=%.0f dur=%.0f avgHr=%.1f maxHr=%d up=%.0f down=%.0f best1k=%.0f best5k=%s kind=%s hrBins=%d moving=%.0f movingM=%.0f%n",
                    w.activity(), w.day(), w.distanceM(), w.durationS(), t.avgHr(), t.maxHr(), t.ascentM(), t.descentM(),
                    t.best1kS(), t.best5kS() == null ? "null" : String.format("%.0f", t.best5kS()), parsed.get(0).detail().kind(),
                    t.hrSeconds().size(), t.movingS(), t.movingM());
            assertEquals(16.55, w.distanceM() / 1000, 0.02);
            assertEquals(20898, w.durationS(), 2);
            assertEquals(103, t.avgHr(), 2.5);
            assertEquals(153, t.maxHr());
            assertEquals(336, t.ascentM(), 20);
            assertEquals(459, t.descentM(), 20);
            // A hike is nowhere near 2:04 per kilometre; the glitch filter must stop that.
            assertTrue(t.best1kS() > 300, "best 1 km looks like a GPS glitch: " + t.best1kS());
            assertEquals("Hike", parsed.get(0).detail().kind());
        }
    }
}
