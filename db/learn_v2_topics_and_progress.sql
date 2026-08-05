-- Learn module v2: split the single TOEIC mock into Part 5 / 6 / 7 topics,
-- add difficulty tiers, per-option rationales, and resumable attempt progress.
--
-- Idempotent: safe to run more than once. Run before deploying the v2 backend
-- on any environment where hibernate ddl-auto is not "update".

BEGIN;

-- learn_quiz: sidebar grouping + ordering
ALTER TABLE learn_quiz ADD COLUMN IF NOT EXISTS part_code varchar(40);
ALTER TABLE learn_quiz ADD COLUMN IF NOT EXISTS sort_order integer NOT NULL DEFAULT 0;

-- learn_question: difficulty tier, provenance, and the tested point
ALTER TABLE learn_question ADD COLUMN IF NOT EXISTS difficulty integer NOT NULL DEFAULT 1;
ALTER TABLE learn_question ADD COLUMN IF NOT EXISTS derived_from integer;
ALTER TABLE learn_question ADD COLUMN IF NOT EXISTS focus_point varchar(120);

-- learn_option: why this option is right or wrong
ALTER TABLE learn_option ADD COLUMN IF NOT EXISTS rationale text;

-- learn_attempt: resumable, shuffled sessions
ALTER TABLE learn_attempt ADD COLUMN IF NOT EXISTS status varchar(20);
ALTER TABLE learn_attempt ADD COLUMN IF NOT EXISTS question_order text;
ALTER TABLE learn_attempt ADD COLUMN IF NOT EXISTS started_at timestamptz;

-- Existing rows predate the status column and are all completed attempts.
UPDATE learn_attempt SET status = 'SUBMITTED' WHERE status IS NULL;
UPDATE learn_attempt SET started_at = submitted_at WHERE started_at IS NULL;

ALTER TABLE learn_attempt ALTER COLUMN status SET NOT NULL;
ALTER TABLE learn_attempt ALTER COLUMN started_at SET NOT NULL;
-- submitted_at is only set once an attempt is finished
ALTER TABLE learn_attempt ALTER COLUMN submitted_at DROP NOT NULL;

-- At most one open attempt per user per quiz.
CREATE UNIQUE INDEX IF NOT EXISTS learn_attempt_open_uidx
    ON learn_attempt (user_id, quiz_id)
    WHERE status = 'IN_PROGRESS';

CREATE INDEX IF NOT EXISTS learn_answer_attempt_idx ON learn_answer (attempt_id);
CREATE INDEX IF NOT EXISTS learn_question_quiz_idx ON learn_question (quiz_id);

COMMIT;
