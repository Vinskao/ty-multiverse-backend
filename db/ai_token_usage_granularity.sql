ALTER TABLE ai_token_usage
    ADD COLUMN IF NOT EXISTS granularity VARCHAR(16) NOT NULL DEFAULT 'per_call';

CREATE INDEX IF NOT EXISTS idx_ai_token_usage_granularity
    ON ai_token_usage (granularity);

CREATE UNIQUE INDEX IF NOT EXISTS uq_ai_token_usage_daily_aggregate
    ON ai_token_usage (
        ((called_at AT TIME ZONE 'UTC')::date),
        model_name,
        ai_provider,
        granularity
    )
    WHERE granularity = 'daily_aggregate';
