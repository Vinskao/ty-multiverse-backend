# PeopleSystem Database Runbook

This directory records the requirements and recovery procedure for the
`peoplesystem` primary database. It is the starting point when provisioning a
new PostgreSQL instance or moving the database again.

Never commit database passwords, connection URLs containing credentials, or
production dumps.

## Required database platform

| Requirement | Value |
| --- | --- |
| Database | PostgreSQL 17 |
| Required extension | `vector` (pgvector), currently verified with 0.8.4 |
| Encoding | UTF-8 |
| Database name | `peoplesystem` |
| Application service address in Kubernetes | `postgres:5432` |
| Kubernetes workload | StatefulSet `postgres`, namespace `default` |
| Kubernetes image | `pgvector/pgvector:pg17` |
| Persistent storage | PVC mounted at `/var/lib/postgresql/data` |

Do not replace the PostgreSQL image with plain `postgres:17`. The schema uses
`vector(1536)` columns and an IVFFlat vector index, so a plain image cannot
restore or operate the database.

## Application configuration

The backend reads the primary datasource from these environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/peoplesystem
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<from Kubernetes Secret>
```

In Kubernetes, store these values in `ty-multiverse-backend-secrets` and expose
them to the backend Deployment. The PostgreSQL administrator password is stored
in Secret `postgres`, key `postgres-password`.

The backend uses Hibernate `hbm2ddl.auto=update`. This can create or adjust
backend entity tables, but it is not a database migration system and cannot:

- install pgvector;
- reproduce data, indexes, functions, triggers, or every historical table;
- restore tables owned by the Django/Maya services sharing this database;
- guarantee an exact copy of production.

For a full rebuild, restore a PostgreSQL dump.

## Required initialization order

For a new, empty database:

1. Deploy PostgreSQL using `pgvector/pgvector:pg17`.
2. Create database `peoplesystem`.
3. Run [`init/00_extensions.sql`](init/00_extensions.sql) as a database owner.
4. Restore the latest custom-format dump, when one exists.
5. Start the backend only after restore completes.
6. Run [`verify.sql`](verify.sql).
7. Verify `/tymb/actuator/health` reports `db.status=UP`.

Example:

```bash
export PGPASSWORD='<secret>'
createdb -h postgres -U postgres peoplesystem
psql -h postgres -U postgres -d peoplesystem \
  -f db/init/00_extensions.sql
pg_restore -h postgres -U postgres -d peoplesystem \
  --no-owner --no-acl --exit-on-error peoplesystem.dump
psql -h postgres -U postgres -d peoplesystem -f db/verify.sql
```

## Full migration procedure

Use PostgreSQL 17 client tools. Keep the backend scaled to zero during the
target backup, database replacement, and restore so no writes are lost.

```bash
# Run on oke-node.
export SOURCE_HOST='<source-host>'
export SOURCE_PORT='5432'
export SOURCE_DB='peoplesystem'
export SOURCE_USER='<source-user>'
export SOURCE_PASSWORD='<source-password>'

mkdir -p "$HOME/db-migration-backups"
TS=$(date +%Y%m%d-%H%M%S)
OLD="$HOME/db-migration-backups/peoplesystem-before-$TS.dump"
NEW="$HOME/db-migration-backups/peoplesystem-source-$TS.dump"

TARGET_PASSWORD=$(
  kubectl get secret postgres -n default \
    -o jsonpath='{.data.postgres-password}' | base64 -d
)

kubectl scale deployment/ty-multiverse-backend -n default --replicas=0
kubectl rollout status deployment/ty-multiverse-backend \
  -n default --timeout=120s

# Preserve the current target before replacing it.
kubectl exec -n default postgres-0 -- \
  env PGPASSWORD="$TARGET_PASSWORD" \
  pg_dump -U postgres -d peoplesystem -Fc --no-owner --no-acl > "$OLD"

# Capture the source.
kubectl exec -n default postgres-0 -- \
  env PGPASSWORD="$SOURCE_PASSWORD" \
  pg_dump \
    "host=$SOURCE_HOST port=$SOURCE_PORT dbname=$SOURCE_DB user=$SOURCE_USER sslmode=require" \
    -Fc --no-owner --no-acl > "$NEW"

test -s "$OLD"
test -s "$NEW"

kubectl exec -n default postgres-0 -- \
  env PGPASSWORD="$TARGET_PASSWORD" \
  psql -U postgres -d postgres -v ON_ERROR_STOP=1 -c \
  "SELECT pg_terminate_backend(pid)
   FROM pg_stat_activity
   WHERE datname = 'peoplesystem' AND pid <> pg_backend_pid();"

kubectl exec -n default postgres-0 -- \
  env PGPASSWORD="$TARGET_PASSWORD" \
  dropdb -U postgres --if-exists peoplesystem
kubectl exec -n default postgres-0 -- \
  env PGPASSWORD="$TARGET_PASSWORD" \
  createdb -U postgres peoplesystem

# --exit-on-error is mandatory. Do not start the app after a partial restore.
cat "$NEW" | kubectl exec -i -n default postgres-0 -- \
  env PGPASSWORD="$TARGET_PASSWORD" \
  pg_restore -U postgres -d peoplesystem \
    --no-owner --no-acl --exit-on-error

kubectl exec -n default postgres-0 -- \
  env PGPASSWORD="$TARGET_PASSWORD" \
  vacuumdb -U postgres -d peoplesystem --analyze

kubectl scale deployment/ty-multiverse-backend -n default --replicas=1
kubectl rollout status deployment/ty-multiverse-backend \
  -n default --timeout=300s
```

If restore fails, leave the backend stopped, recreate `peoplesystem`, and
restore `$OLD` with the same `pg_restore` command.

## Verification

Database verification:

```bash
kubectl exec -i -n default postgres-0 -- \
  env PGPASSWORD="$TARGET_PASSWORD" \
  psql -U postgres -d peoplesystem -v ON_ERROR_STOP=1 \
  < db/verify.sql
```

Application verification:

```bash
kubectl run database-healthcheck -n default --rm -i --restart=Never \
  --image=curlimages/curl:8.10.1 -- \
  curl -fsS --max-time 30 \
  http://ty-multiverse-backend/tymb/actuator/health
```

The response must report `UP` for the application and the `db` component.

## Current production schema inventory

The database restored from Neon on 2026-07-04 contained 32 public tables:

```text
account_emailaddress
account_emailconfirmation
ai_token_usage
articles
auth_group
auth_group_permissions
auth_permission
ckeditor
damage_calculation_result
django_admin_log
django_content_type
django_migrations
django_session
django_site
gallery
livestock
maya_sawa_git_commits
maya_sawa_v2_ai_processing_aimodel
maya_sawa_v2_ai_processing_processingtask
maya_sawa_v2_conversations_conversation
maya_sawa_v2_conversations_message
mfa_authenticator
people
people_image
socialaccount_socialaccount
socialaccount_socialapp
socialaccount_socialapp_sites
socialaccount_socialtoken
users_user
users_user_groups
users_user_user_permissions
weapon
```

Vector columns:

```text
maya_sawa_git_commits.embedding vector(1536)
people.embedding                 vector(1536)
weapon.embedding                 vector(1536)
```

This is a shared database. Django/Maya owns many `auth_*`, `account_*`,
`socialaccount_*`, `users_*`, and `maya_sawa_*` tables. Restoring only the Java
entities is not a complete recovery.

## Backup retention and security

- Keep at least one verified dump outside the PostgreSQL PVC.
- Encrypt dumps when moving them off the node.
- Restrict dump file permissions and delete obsolete copies deliberately.
- Test restore periodically; a successful `pg_dump` alone is not proof that a
  backup is recoverable.
- Rotate any credential that is accidentally printed or committed.

