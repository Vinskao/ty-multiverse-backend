-- Transactional Outbox：業務事件待發布佇列
--
-- 由 ty-multiverse-consumer 在寫入 people／weapon 業務資料的「同一個 transaction」內
-- 插入一筆 outbox；背景 publisher 再把未發布的資料送到 tymb-event-exchange
-- （routing key = event.<eventType>），確認成功後回填 published_at。
--
-- 手動執行（本專案未使用 Flyway，schema 沿用 db/*.sql 慣例）：
--   psql "$DATABASE_URL" -f db/business_event_outbox.sql

CREATE TABLE IF NOT EXISTS business_event_outbox (
    id              BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id        UUID         NOT NULL,
    event_type      VARCHAR(128) NOT NULL,
    aggregate_type  VARCHAR(64)  NOT NULL,
    aggregate_id    VARCHAR(255),
    request_id      VARCHAR(128),
    payload         JSONB        NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ,
    attempt_count   INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_error      TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- eventId 是下游的去重鍵，這裡同時保證 publisher 不會重複建立同一筆事件
CREATE UNIQUE INDEX IF NOT EXISTS uq_business_event_outbox_event_id
    ON business_event_outbox (event_id);

-- publisher 的主要查詢：撈出到期且尚未發布的事件，依發生順序送出。
-- 發布失敗時 next_attempt_at 會往後推（指數退避），避免壞掉的事件把輪詢塞滿。
CREATE INDEX IF NOT EXISTS idx_business_event_outbox_pending
    ON business_event_outbox (next_attempt_at, occurred_at)
    WHERE published_at IS NULL;

-- 稽核查詢：用 requestId 串起 requested / succeeded / failed
CREATE INDEX IF NOT EXISTS idx_business_event_outbox_request_id
    ON business_event_outbox (request_id);

-- 稽核查詢：依 aggregate 追某一筆資料的變更歷程
CREATE INDEX IF NOT EXISTS idx_business_event_outbox_aggregate
    ON business_event_outbox (aggregate_type, aggregate_id, occurred_at);
