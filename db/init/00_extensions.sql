\set ON_ERROR_STOP on

-- Must run before Hibernate or pg_restore creates vector columns.
CREATE EXTENSION IF NOT EXISTS vector;

DO $$
BEGIN
    IF current_setting('server_version_num')::integer < 170000 THEN
        RAISE EXCEPTION
            'PostgreSQL 17 or newer is required; server_version is %',
            current_setting('server_version');
    END IF;
END
$$;

