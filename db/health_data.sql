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
