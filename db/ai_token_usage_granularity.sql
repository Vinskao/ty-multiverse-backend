ALTER TABLE ai_token_usage
    ADD COLUMN IF NOT EXISTS granularity VARCHAR(16) NOT NULL DEFAULT 'per_call';

ALTER TABLE ai_token_usage
    ADD COLUMN IF NOT EXISTS source_device VARCHAR(128) NOT NULL DEFAULT 'unknown';

CREATE INDEX IF NOT EXISTS idx_ai_token_usage_granularity
    ON ai_token_usage (granularity);

DROP INDEX IF EXISTS uq_ai_token_usage_daily_aggregate;
DROP INDEX IF EXISTS idx_ai_token_usage_daily_agg_unique;

CREATE UNIQUE INDEX IF NOT EXISTS uq_ai_token_usage_daily_aggregate_device
    ON ai_token_usage (
        ((called_at AT TIME ZONE 'UTC')::date),
        model_name,
        ai_provider,
        granularity,
        source_device
    )
    WHERE granularity = 'daily_aggregate';
