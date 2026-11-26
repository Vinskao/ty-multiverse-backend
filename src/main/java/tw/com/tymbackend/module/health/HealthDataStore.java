package tw.com.tymbackend.module.health;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class HealthDataStore {
    record Workout(String sourceId, String activity, LocalDate day, Double distanceM, double durationS, Double energyKcal) {}
    record Daily(String metric, LocalDate day, String unit, Double total, Double average, Double minimum, Double maximum, int samples) {}

    private final JdbcTemplate db;

    HealthDataStore(JdbcTemplate db) {
        this.db = db;
    }

    @Transactional
    void saveWorkouts(List<Workout> rows) {
        db.batchUpdate("""
                insert into health_workout(source_id, activity, day, distance_m, duration_s, energy_kcal) values (?,?,?,?,?,?)
                on conflict (source_id) do update set activity = excluded.activity, day = excluded.day,
                  distance_m = excluded.distance_m, duration_s = excluded.duration_s, energy_kcal = excluded.energy_kcal
                """, rows.stream().map(r -> new Object[]{r.sourceId(), r.activity(), Date.valueOf(r.day()),
                        r.distanceM(), r.durationS(), r.energyKcal()}).toList());
    }

    /**
     * For files that carry no active energy (TCX): updates distance and time but keeps any energy the Apple Health import stored.
     * A workout that matches one already stored under another key (same day and activity, nearly equal distance and time)
     * updates that row, so the same run is never counted twice (see HealthWorkoutMatch).
     */
    @Transactional
    void saveWorkoutsKeepingEnergy(List<Workout> rows) {
        rows.forEach(this::saveOneKeepingEnergy);
    }

    /** Returns the key of the row that now holds the workout (an existing row when the workout matches one already stored). */
    private String saveOneKeepingEnergy(Workout r) {
        List<HealthWorkoutMatch.Known> known = db.query(
                "select source_id, distance_m, duration_s from health_workout where day = ? and activity = ?",
                (row, n) -> new HealthWorkoutMatch.Known(row.getString(1), (Double) row.getObject(2), row.getDouble(3)),
                Date.valueOf(r.day()), r.activity());
        String key = HealthWorkoutMatch.keyFor(r, known);
        db.update("""
                insert into health_workout(source_id, activity, day, distance_m, duration_s, energy_kcal) values (?,?,?,?,?,?)
                on conflict (source_id) do update set activity = excluded.activity, day = excluded.day,
                  distance_m = excluded.distance_m, duration_s = excluded.duration_s
                """, key, r.activity(), Date.valueOf(r.day()), r.distanceM(), r.durationS(), r.energyKcal());
        return key;
    }

    /**
     * Stores TCX workouts: distance and time as above, then the detail the track adds (kind, climb, heart rate, best efforts) on
     * the same row. The detail columns need db/health_data.sql applied first; without them this fails loudly instead of quietly
     * dropping the heart rate of an upload.
     */
    @Transactional
    void saveTcx(List<HealthTcx.Parsed> rows) {
        for (HealthTcx.Parsed p : rows) {
            String key = saveOneKeepingEnergy(p.workout());
            var t = p.detail().track();
            db.update("""
                    update health_workout set kind = ?, ascent_m = ?, descent_m = ?, avg_hr = ?, max_hr = ?,
                      hr_seconds = ?::jsonb, best_1k_s = ?, best_5k_s = ?, moving_s = ?, moving_m = ? where source_id = ?
                    """, p.detail().kind(), t.ascentM(), t.descentM(), t.avgHr(), t.maxHr(),
                    t.hrSeconds().isEmpty() ? null : hrJson(t.hrSeconds()), t.best1kS(), t.best5kS(), t.movingS(), t.movingM(), key);
        }
    }

    /** Earlier moving speeds (km/h) of one sport, the history HealthPlausibility compares a new workout with. */
    List<Double> movingSpeedsKmh(String activity) {
        return db.query("select coalesce(moving_m, distance_m) / moving_s * 3.6 from health_workout where activity = ? and moving_s > 0 and coalesce(moving_m, distance_m) > 0",
                (row, n) -> row.getDouble(1), activity);
    }

    private static String hrJson(Map<Integer, Integer> seconds) {
        StringBuilder json = new StringBuilder("{");
        seconds.forEach((bin, s) -> json.append(json.length() > 1 ? "," : "").append('"').append(bin).append("\":").append(s));
        return json.append('}').toString();
    }

    /** Stored totals for the metrics and days in the batch, keyed "metric|day" (see DailyGuard). */
    Map<String, Double> dailyTotals(List<Daily> rows) {
        Map<String, Double> totals = new HashMap<>();
        for (String metric : rows.stream().map(Daily::metric).distinct().toList()) {
            LocalDate from = rows.stream().filter(r -> r.metric().equals(metric)).map(Daily::day).min(LocalDate::compareTo).orElseThrow();
            LocalDate to = rows.stream().filter(r -> r.metric().equals(metric)).map(Daily::day).max(LocalDate::compareTo).orElseThrow();
            db.query("select day, total from health_daily where metric = ? and day between ? and ? and total is not null",
                    row -> {
                        totals.put(DailyGuard.key(metric, row.getDate(1).toLocalDate()), row.getDouble(2));
                    }, metric, Date.valueOf(from), Date.valueOf(to));
        }
        return totals;
    }

    /**
     * Remembers when each source last delivered data. It is only a heartbeat, so a failure here must never fail an upload
     * (the table may not exist yet right after a deploy).
     */
    void recordIngest(String source, int items, String detail) {
        try {
            db.update("""
                    insert into health_ingest_status(source, received_at, items, detail) values (?, now(), ?, ?)
                    on conflict (source) do update set received_at = excluded.received_at, items = excluded.items, detail = excluded.detail
                    """, source, items, detail);
        } catch (RuntimeException ignored) {
            // heartbeat only
        }
    }

    List<Map<String, Object>> ingestStatus() {
        try {
            return db.queryForList("""
                    select source, received_at::text as received_at, items, detail,
                           round((extract(epoch from (now() - received_at)) / 3600)::numeric, 1) as age_hours
                    from health_ingest_status order by source
                    """);
        } catch (RuntimeException missing) {
            return List.of();
        }
    }

    @Transactional
    void saveDaily(List<Daily> rows) {
        db.batchUpdate("""
                insert into health_daily(metric, day, unit, total, average, minimum, maximum, samples) values (?,?,?,?,?,?,?,?)
                on conflict (metric, day) do update set unit = excluded.unit, total = excluded.total, average = excluded.average,
                  minimum = excluded.minimum, maximum = excluded.maximum, samples = excluded.samples
                """, rows.stream().map(r -> new Object[]{r.metric(), Date.valueOf(r.day()), r.unit(), r.total(), r.average(),
                        r.minimum(), r.maximum(), r.samples()}).toList());
    }

    /**
     * Running workouts for the public statistics. When the recorded time is more than 5 times the moving time (a watch left
     * running, so the file's distance also carries hours of GPS drift) the moving distance and moving time are used, so neither the
     * pace nor the distance totals are inflated; otherwise the recorded numbers, which match what the tracking app shows.
     */
    List<RunningStats.Run> runs() {
        return db.query("""
                select day,
                       case when moving_s is not null and moving_m is not null and duration_s > 5 * moving_s then moving_m else distance_m end,
                       case when moving_s is not null and moving_m is not null and duration_s > 5 * moving_s then moving_s else duration_s end
                from health_workout where activity = 'Running' and distance_m > 0
                """, (row, n) -> new RunningStats.Run(row.getDate(1).toLocalDate(), row.getDouble(2), row.getDouble(3)));
    }

    /**
     * Workouts of one sport with climb and best efforts, for the personal records. Running uses the same rule as {@link #runs()}
     * (moving distance and time when the watch was left running).
     */
    List<RunningRecords.Entry> recordEntries(String activity) {
        return db.query("""
                select day,
                       case when moving_s is not null and moving_m is not null and duration_s > 5 * moving_s then moving_m else distance_m end,
                       case when moving_s is not null and moving_m is not null and duration_s > 5 * moving_s then moving_s else duration_s end,
                       ascent_m, best_1k_s, best_5k_s
                from health_workout where activity = ? and distance_m > 0
                """, (row, n) -> new RunningRecords.Entry(row.getDate(1).toLocalDate(), row.getDouble(2), row.getDouble(3),
                        (Double) row.getObject(4), (Double) row.getObject(5), (Double) row.getObject(6)), activity);
    }

    /** Hiking workouts, which the weekly distance counts together with the runs. */
    List<RunningStats.Run> hikes() {
        return db.query("select day, distance_m, duration_s from health_workout where activity = 'Hiking' and distance_m > 0",
                (row, n) -> new RunningStats.Run(row.getDate(1).toLocalDate(), row.getDouble(2), row.getDouble(3)));
    }

    List<Map<String, Object>> activities() {
        return db.queryForList("""
                select activity, count(*) as sessions, coalesce(sum(distance_m), 0) / 1000 as km, sum(duration_s) / 3600 as hours
                from health_workout group by activity order by sessions desc, activity
                """);
    }

    /** One row per metric: latest value, 30-day average and all-time peak (daily total, or daily average when there is none). */
    List<Map<String, Object>> overview(LocalDate today) {
        return db.queryForList("""
                select metric, max(unit) as unit, max(day)::text as last_day,
                       (array_agg(coalesce(total, average) order by day desc))[1] as latest,
                       avg(coalesce(total, average)) filter (where day >= ? and day < ?) as average30,
                       max(coalesce(total, average)) as peak,
                       (array_agg(day order by coalesce(total, average) desc nulls last, day))[1]::text as peak_day
                from health_daily group by metric order by metric
                """, Date.valueOf(today.minusDays(30)), Date.valueOf(today));
    }

    /** Daily values (total, or average when there is none) of the given metrics since a day, oldest first, for the charts. */
    List<Map<String, Object>> series(List<String> metrics, LocalDate from) {
        return db.queryForList("""
                select metric, day::text as day, coalesce(total, average) as value from health_daily
                where metric = any (?::text[]) and day >= ? and coalesce(total, average) is not null order by metric, day
                """, "{" + String.join(",", metrics) + "}", Date.valueOf(from));
    }

    List<Map<String, Object>> metrics() {
        return db.queryForList("""
                select metric, max(unit) as unit, count(*) as days, min(day)::text as first_day, max(day)::text as last_day
                from health_daily group by metric order by metric
                """);
    }

    List<Map<String, Object>> daily(String metric) {
        return db.queryForList("""
                select day::text as day, unit, total, average, minimum, maximum, samples from health_daily
                where metric = ? order by health_daily.day desc limit 1000
                """, metric);
    }
}
