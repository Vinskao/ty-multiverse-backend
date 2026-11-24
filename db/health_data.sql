-- Apple Health import (docs/HEALTH_DATA.md). No start times, routes or device names are stored.
CREATE TABLE IF NOT EXISTS health_workout (
    source_id  text PRIMARY KEY,
    activity   text NOT NULL,
    day        date NOT NULL,
    distance_m double precision CHECK (distance_m >= 0),
    duration_s double precision NOT NULL CHECK (duration_s > 0),
    energy_kcal double precision CHECK (energy_kcal >= 0)
);
CREATE INDEX IF NOT EXISTS health_workout_day ON health_workout(activity, day);
CREATE TABLE IF NOT EXISTS health_daily (
    metric  text NOT NULL,
    day     date NOT NULL,
    unit    text NOT NULL,
    total   double precision,
    average double precision,
    minimum double precision,
    maximum double precision,
    samples integer NOT NULL CHECK (samples > 0),
    PRIMARY KEY (metric, day)
);
-- Heartbeat: when each source last delivered data (daily-lines, workout-tcx, apple-export). Shown in /health-data/overview.
CREATE TABLE IF NOT EXISTS health_ingest_status (
    source      text PRIMARY KEY,
    received_at timestamptz NOT NULL,
    items       integer NOT NULL,
    detail      text
);
-- Per-workout detail from TCX uploads (docs/HEALTH_DATA.md): strict kind, climb, heart rate, best efforts. Never a route.
-- hr_seconds is seconds per 5 bpm bin, e.g. {"150": 3200, "155": 410}, so zones can be recomputed for any maximum heart rate.
ALTER TABLE health_workout
    ADD COLUMN IF NOT EXISTS kind        text,
    ADD COLUMN IF NOT EXISTS ascent_m    double precision CHECK (ascent_m >= 0),
    ADD COLUMN IF NOT EXISTS descent_m   double precision CHECK (descent_m >= 0),
    ADD COLUMN IF NOT EXISTS avg_hr      double precision CHECK (avg_hr > 0),
    ADD COLUMN IF NOT EXISTS max_hr      integer CHECK (max_hr > 0),
    ADD COLUMN IF NOT EXISTS hr_seconds  jsonb,
    ADD COLUMN IF NOT EXISTS best_1k_s   double precision CHECK (best_1k_s > 0),
    ADD COLUMN IF NOT EXISTS best_5k_s   double precision CHECK (best_5k_s > 0);
CREATE INDEX IF NOT EXISTS health_workout_kind ON health_workout(kind, day);
-- Seconds actually spent moving (TCX uploads). The recorded duration_s can be far longer when a watch was left running; the public
-- fastest pace uses moving_s whenever duration_s is more than twice as long.
ALTER TABLE health_workout ADD COLUMN IF NOT EXISTS moving_s double precision CHECK (moving_s >= 0);
