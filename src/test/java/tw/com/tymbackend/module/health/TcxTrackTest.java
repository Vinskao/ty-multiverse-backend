package tw.com.tymbackend.module.health;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TcxTrackTest {
    /** Adds one point every second at a constant speed (m/s) starting at the current end of the track. */
    private static void run(TcxTrack t, int seconds, double speed, int hr, double alt) {
        double d = t.lastDistance();
        double time = t.lastTime();
        for (int i = 1; i <= seconds; i++) {
            t.add(time + i, d + speed * i, alt, hr);
        }
    }

    @Test
    void averagesHeartRateByTimeAndIgnoresDropouts() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 100);
        run(t, 60, 3, 100, 100);
        run(t, 60, 3, 0, 100); // sensor lost: must not drag the average down
        run(t, 60, 3, 140, 100);
        var s = t.summarize();
        assertEquals(120, s.avgHr(), 3.0);
        assertEquals(140, s.maxHr());
    }

    @Test
    void keepsSecondsPerFiveBeatBin() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 100, 3, 152, 100); // 150-154 bin
        run(t, 40, 3, 161, 100); // 160-164 bin
        var s = t.summarize();
        assertEquals(100, s.hrSeconds().get(150), 2);
        assertEquals(40, s.hrSeconds().get(160), 2);
        assertNull(s.hrSeconds().get(0));
    }

    @Test
    void noisyFlatGroundAddsNoClimb() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 120);
        for (int i = 1; i <= 600; i++) {
            t.add(i, i * 3.0, 100 + (i % 2 == 0 ? 1.0 : -1.0), 120);
        }
        var s = t.summarize();
        assertEquals(0, s.ascentM(), 1e-9);
        assertEquals(0, s.descentM(), 1e-9);
    }

    @Test
    void realClimbAndDescentAreCounted() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 130);
        for (int i = 1; i <= 100; i++) {
            t.add(i, i * 2.0, 100 + i, 130); // +100 m
        }
        for (int i = 1; i <= 60; i++) {
            t.add(100 + i, 200 + i * 2.0, 200 - i, 130); // -60 m
        }
        var s = t.summarize();
        assertEquals(100, s.ascentM(), 6);
        assertEquals(60, s.descentM(), 6);
    }

    @Test
    void fastestKilometreUsesRealMovementOnly() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 500, 2.5, 150, 100); // slow start: 1 km takes 400 s
        run(t, 400, 4.0, 150, 100); // fast stretch: 1 km takes 250 s
        run(t, 500, 2.5, 150, 100);
        var s = t.summarize();
        assertEquals(250, s.best1kS(), 3);
        assertNull(s.best5kS(), "only about 4.7 km were covered, so there is no 5 km effort");
    }

    @Test
    void aGpsJumpDoesNotBecomeAWorldRecord() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 300, 2.0, 150, 100);
        // The distance leaps by 800 m in one second (a GPS glitch or a pause that restarted the counter).
        t.add(t.lastTime() + 1, t.lastDistance() + 800, 100, 150);
        run(t, 300, 2.0, 150, 100);
        var s = t.summarize();
        assertEquals(500, s.best1kS(), 5, "the jump window must be skipped, the real best is 1 km at 2 m/s");
    }

    @Test
    void aLongPauseBreaksTheEffort() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 200, 4.0, 150, 100); // 800 m
        t.add(t.lastTime() + 600, t.lastDistance() + 200, 100, 150); // 10 minutes later, 200 m further
        var s = t.summarize();
        assertNull(s.best1kS());
    }

    @Test
    void emptyTrackHasNothingToReport() {
        var s = new TcxTrack().summarize();
        assertNull(s.avgHr());
        assertNull(s.ascentM());
        assertNull(s.best1kS());
        assertTrue(s.hrSeconds().isEmpty());
    }
}
