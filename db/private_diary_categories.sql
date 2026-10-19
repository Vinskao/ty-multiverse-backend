ALTER TABLE diary_entries ADD COLUMN IF NOT EXISTS category VARCHAR(32) NOT NULL DEFAULT 'general' CHECK (category IN ('general','relationship','investment','wellbeing'));
ALTER TABLE diary_entries ADD COLUMN IF NOT EXISTS entry_kind VARCHAR(16) NOT NULL DEFAULT 'document' CHECK (entry_kind IN ('document','event'));
ALTER TABLE diary_entries ADD COLUMN IF NOT EXISTS date_precision VARCHAR(32) NOT NULL DEFAULT 'day';
ALTER TABLE diary_entries ADD COLUMN IF NOT EXISTS source_event_id VARCHAR(128);
ALTER TABLE diary_entries ADD COLUMN IF NOT EXISTS metadata_json TEXT NOT NULL DEFAULT '{}';
ALTER TABLE diary_entries ALTER COLUMN entry_date DROP NOT NULL;
ALTER TABLE diary_entries DROP CONSTRAINT IF EXISTS diary_entry_dedup;
ALTER TABLE diary_entries ADD CONSTRAINT diary_entry_dedup UNIQUE (owner_subject, category, entry_date, content_hash);
CREATE UNIQUE INDEX IF NOT EXISTS diary_source_dedup ON diary_entries (owner_subject, source_event_id);
