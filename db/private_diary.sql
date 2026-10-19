-- Private runtime data only. Never seed personal Markdown into source control.
CREATE TABLE IF NOT EXISTS diary_entries (
    id VARCHAR(36) PRIMARY KEY,
    owner_subject VARCHAR(255) NOT NULL,
    entry_date DATE,
    category VARCHAR(32) NOT NULL DEFAULT 'general' CHECK (category IN ('general','relationship','investment','wellbeing')),
    entry_kind VARCHAR(16) NOT NULL DEFAULT 'document' CHECK (entry_kind IN ('document','event')),
    date_precision VARCHAR(32) NOT NULL DEFAULT 'day',
    source_event_id VARCHAR(128),
    metadata_json TEXT NOT NULL DEFAULT '{}',
    title VARCHAR(200) NOT NULL,
    markdown TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT diary_entry_dedup UNIQUE (owner_subject, category, entry_date, content_hash),
    CONSTRAINT diary_source_dedup UNIQUE (owner_subject, source_event_id)
);
CREATE INDEX IF NOT EXISTS diary_owner_date ON diary_entries (owner_subject, entry_date DESC);
CREATE TABLE IF NOT EXISTS diary_workflows (
    owner_subject VARCHAR(255) PRIMARY KEY,
    next_due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    queued_since TIMESTAMP WITH TIME ZONE,
    triggered_at TIMESTAMP WITH TIME ZONE,
    pending BOOLEAN NOT NULL DEFAULT FALSE
);
