-- AI-written narratives, kept apart from the owner's original diary entries.
-- scope_type 'entry' is one event (scope_key = diary_entries.id), 'month' a calendar month (scope_key = YYYY-MM).
-- source_hash records the source the narrative was written from, and a changed source marks it stale.
CREATE TABLE IF NOT EXISTS diary_narratives (
    owner_subject VARCHAR(255) NOT NULL,
    scope_type VARCHAR(16) NOT NULL CHECK (scope_type IN ('entry','month')),
    scope_key VARCHAR(64) NOT NULL,
    narrative TEXT NOT NULL,
    source_hash VARCHAR(64) NOT NULL,
    model VARCHAR(64) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (owner_subject, scope_type, scope_key)
);
