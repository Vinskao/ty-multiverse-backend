\set ON_ERROR_STOP on

SELECT
    current_database() AS database_name,
    current_setting('server_version') AS postgres_version,
    pg_size_pretty(pg_database_size(current_database())) AS database_size;

DO $$
DECLARE
    missing_tables text[];
    vector_version text;
    vector_column_count integer;
BEGIN
    SELECT extversion
      INTO vector_version
      FROM pg_extension
     WHERE extname = 'vector';

    IF vector_version IS NULL THEN
        RAISE EXCEPTION 'Required extension "vector" is not installed';
    END IF;

    SELECT array_agg(required_table ORDER BY required_table)
      INTO missing_tables
      FROM unnest(ARRAY[
          'ai_token_usage',
          'ckeditor',
          'gallery',
          'livestock',
          'people',
          'people_image',
          'weapon'
      ]) AS required_table
     WHERE to_regclass('public.' || required_table) IS NULL;

    IF missing_tables IS NOT NULL THEN
        RAISE EXCEPTION 'Missing required backend tables: %', missing_tables;
    END IF;

    SELECT count(*)
      INTO vector_column_count
      FROM information_schema.columns
     WHERE table_schema = 'public'
       AND udt_name = 'vector'
       AND (table_name, column_name) IN (
           ('maya_sawa_git_commits', 'embedding'),
           ('people', 'embedding'),
           ('weapon', 'embedding')
       );

    IF vector_column_count <> 3 THEN
        RAISE EXCEPTION
            'Expected 3 known vector columns, found %',
            vector_column_count;
    END IF;

    RAISE NOTICE
        'Database requirements verified (vector %, 3 vector columns)',
        vector_version;
END
$$;

SELECT count(*) AS public_table_count
  FROM information_schema.tables
 WHERE table_schema = 'public';

SELECT
    schemaname,
    relname AS table_name,
    n_live_tup AS estimated_rows
  FROM pg_stat_user_tables
 ORDER BY relname;

