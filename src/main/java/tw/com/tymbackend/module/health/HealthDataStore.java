package tw.com.tymbackend.module.health;

import java.sql.Date;
import java.time.LocalDate;
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

    /** For files that carry no active energy (TCX): updates distance and time but keeps any energy the Apple Health import stored. */
    @Transactional
    void saveWorkoutsKeepingEnergy(List<Workout> rows) {
        db.batchUpdate("""
                insert into health_workout(source_id, activity, day, distance_m, duration_s, energy_kcal) values (?,?,?,?,?,?)
                on conflict (source_id) do update set activity = excluded.activity, day = excluded.day,
                  distance_m = excluded.distance_m, duration_s = excluded.duration_s
                """, rows.stream().map(r -> new Object[]{r.sourceId(), r.activity(), Date.valueOf(r.day()),
                        r.distanceM(), r.durationS(), r.energyKcal()}).toList());
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

    List<RunningStats.Run> runs() {
        return db.query("select day, distance_m, duration_s from health_workout where activity = 'Running' and distance_m > 0",
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
