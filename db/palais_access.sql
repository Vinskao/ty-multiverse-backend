CREATE TABLE IF NOT EXISTS palais_access_request (
    id uuid PRIMARY KEY,
    applicant text NOT NULL,
    display_name text NOT NULL,
    reason text NOT NULL,
    status text NOT NULL CHECK (status IN ('PENDING','WITHDRAWN','REJECTED','SYNCING','ACTIVE','FAILED','REVOKED')),
    approvals_required integer NOT NULL CHECK (approvals_required IN (1,2)),
    lease uuid,
    attempts integer NOT NULL DEFAULT 0,
    next_attempt timestamptz NOT NULL DEFAULT now(),
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS palais_access_one_open
    ON palais_access_request(applicant) WHERE status IN ('PENDING','SYNCING','FAILED');
CREATE TABLE IF NOT EXISTS palais_access_event (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id uuid NOT NULL REFERENCES palais_access_request(id),
    actor text NOT NULL,
    action text NOT NULL,
    detail text,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS palais_access_event_lookup ON palais_access_event(request_id, action);
CREATE INDEX IF NOT EXISTS palais_access_queue ON palais_access_request(status, created_at, id);