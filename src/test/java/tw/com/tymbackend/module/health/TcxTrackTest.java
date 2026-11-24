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
    void movingTimeLeavesOutStopsAndLongGaps() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 100, 3, 150, 100); // moving 100 s
        run(t, 100, 0, 90, 100); // standing still 100 s
        run(t, 100, 3, 150, 100); // moving 100 s
        t.add(t.lastTime() + 1000, t.lastDistance() + 50, 100, 150); // a gap of 1000 s
        assertEquals(200, t.summarize().movingS(), 2);
    }

    @Test
    void aWatchLeftRunningAfterTheRunHasLittleMovingTime() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 3600, 2.5, 150, 100); // one hour of real running, 9 km
        run(t, 50_000, 0, 70, 100); // then the timer keeps going while the watch sits still
        var s = t.summarize();
        assertEquals(3600, s.movingS(), 5);
        assertTrue(t.lastTime() > 50_000);
    }

    @Test
    void movingDistanceLeavesOutGpsDriftWhileStandingStill() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 200, 3, 150, 100); // 600 m of real movement
        // The watch sits still but the GPS wanders: the counter keeps growing 0.1 m a second, below the moving threshold.
        double d = t.lastDistance();
        double time = t.lastTime();
        for (int i = 1; i <= 10_000; i++) {
            t.add(time + i, d + 0.1 * i, 100, 70);
        }
        var s = t.summarize();
        assertEquals(600, s.movingM(), 5);
        assertEquals(200, s.movingS(), 3);
        assertTrue(t.lastDistance() > 1500, "the raw distance is inflated by the drift");
    }

    @Test
    void whenTheTimerRanOnHeartRateIsOnlyTakenFromTheMovingPart() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 600, 3, 150, 100); // 10 minutes of running at 150
        run(t, 20_000, 0, 70, 100); // then hours of standing at 70: far more than 5 times the moving time
        var s = t.summarize();
        assertEquals(150, s.avgHr(), 1.5);
        assertNull(s.hrSeconds().get(70), "resting seconds must not appear in the histogram");
    }

    @Test
    void normalRestsStayInTheHeartRateAverage() {
        var t = new TcxTrack();
        t.add(0, 0, 100, 150);
        run(t, 300, 3, 150, 100);
        run(t, 200, 0, 100, 100); // a rest, but the elapsed time is only 1.7 times the moving time
        var s = t.summarize();
        assertTrue(s.avgHr() < 135, "the rest at 100 bpm is part of the average: " + s.avgHr());
    }

    @Test
    void emptyTrackHasNothingToReport() {
        var s = new TcxTrack().summarize();
        assertNull(s.avgHr());
        assertNull(s.ascentM());
        assertNull(s.best1kS());
        assertNull(s.movingS());
        assertNull(s.movingM());
        assertTrue(s.hrSeconds().isEmpty());
    }
}
