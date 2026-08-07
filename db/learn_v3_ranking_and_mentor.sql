-- Learn module v3: cohort ranking + mentor mode.
--
-- Attempts key off the token subject (a UUID), which is useless on a leaderboard,
-- so each attempt now also records the display name it was taken under.
--
-- Idempotent: safe to run more than once. Run before deploying the v3 backend on
-- any environment where hibernate ddl-auto is not "update".

BEGIN;

ALTER TABLE learn_attempt ADD COLUMN IF NOT EXISTS display_name varchar(160);

-- Ranking and mentor mode group submitted attempts by learner and by topic.
CREATE INDEX IF NOT EXISTS idx_learn_attempt_quiz_status
    ON learn_attempt (quiz_id, status);
CREATE INDEX IF NOT EXISTS idx_learn_attempt_status_user
    ON learn_attempt (status, user_id);

COMMIT;
