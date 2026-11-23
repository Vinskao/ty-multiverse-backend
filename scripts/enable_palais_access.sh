#!/usr/bin/env bash
# Enables Palais access (docs/PALAIS_ACCESS.md). Runs from a workstation; secrets stay on the operator host and in the cluster.
#   scripts/enable_palais_access.sh <checker-username>            read-only check
#   scripts/enable_palais_access.sh <checker-username> --apply    change database, Keycloak, Kubernetes; restart backend
#   scripts/enable_palais_access.sh <checker-username> --disable  switch the feature off again (data and roles kept)
# Keycloak 26.1 without fine-grained permissions needs `manage-users` for the service account. That role can also map OTHER
# non-admin realm roles, so it does not meet the "palais-checker only" check in the docs. --apply means accepting that.
set -euo pipefail
user="${1:-}"; mode="${2:---check}"
[[ "$user" =~ ^[A-Za-z0-9._@-]+$ && "$mode" =~ ^--(check|apply|disable)$ ]] || { sed -n 2,5p "$0"; exit 2; }
here="$(cd "$(dirname "$0")" && pwd)"
sql="$(base64 < "$here/../db/palais_access.sql" | tr -d '\n')"
ssh oke-node bash -s -- "$user" "$mode" "$sql" <<'REMOTE'
set -euo pipefail
user="$1"; mode="$2"; sql="$(base64 -d <<<"$3")"
k() { kubectl -n default "$@" </dev/null; }
restart() { k rollout restart deploy/ty-multiverse-backend; k rollout status deploy/ty-multiverse-backend --timeout=10m; }
if [ "$mode" = --disable ]; then
  k patch cm palais-access-config --type merge -p '{"data":{"PALAIS_ACCESS_ENABLED":"false"}}'; restart; exit
fi
sec() { k get secret "$1" -o json | jq -er --arg k "$2" '.data[$k]|@base64d'; }
umask 077; tmp="$(mktemp -d)"; pf=""
trap '[ -z "$pf" ] || kill "$pf" 2>/dev/null; rm -rf "$tmp"' EXIT

kubectl -n default port-forward deploy/keycloak 18080:8080 >/dev/null 2>&1 </dev/null & pf=$!
base=http://127.0.0.1:18080/sso
for _ in $(seq 30); do curl -fsS -o /dev/null "$base/realms/master" 2>/dev/null && break; sleep 1; done
realm="$(sec ty-multiverse-backend-secrets PUBLIC_REALM)"
login="$(sec ty-multiverse-backend-secrets PUBLIC_CLIENT_ID)"
sec keycloak-admin username | tr -d '\n' >"$tmp/u"; sec keycloak-admin password | tr -d '\n' >"$tmp/p"
curl -fsS "$base/realms/master/protocol/openid-connect/token" -d grant_type=password -d client_id=admin-cli \
  --data-urlencode "username@$tmp/u" --data-urlencode "password@$tmp/p" \
  | jq -er '"Authorization: Bearer " + .access_token' >"$tmp/h"
api() { curl -fsS -H @"$tmp/h" -H 'Content-Type: application/json' "$@"; }
A="$base/admin/realms/$realm"
issuer="$(curl -fsS https://tymultiverse.cc/sso/realms/$realm/.well-known/openid-configuration | jq -er .issuer)"
aud=ty-multiverse-api; svc=palais-access-service
cid() { api -G "$A/clients" --data-urlencode "clientId=$1" | jq -r '.[0].id // empty'; }
uid="$(api -G "$A/users" --data-urlencode "username=$user" -d exact=true | jq -r '.[0].id // empty')"
lid="$(cid "$login")"; sid="$(cid "$svc")"
url="$(sec ty-multiverse-backend-secrets SPRING_DATASOURCE_URL)"; dbuser="$(sec ty-multiverse-backend-secrets SPRING_DATASOURCE_USERNAME)"
rest="${url#jdbc:postgresql://}"; host="${rest%%[:/]*}"; db="${rest#*/}"; db="${db%%\?*}"
schema=public; case "$url" in *currentSchema=*) schema="${url#*currentSchema=}"; schema="${schema%%[&,]*}";; esac
psql() { kubectl -n default exec -i postgres-0 -- psql -X -v ON_ERROR_STOP=1 -U postgres -d "$db" "$@"; }
echo "realm=$realm issuer=$issuer login-client=$login"
echo "checker=$user uuid=${uid:-MISSING}; login client ${lid:+found}; service client: ${sid:-not created}"
echo "database host=$host db=$db schema=$schema"
[ -n "$uid" ] && [ -n "$lid" ] || { echo "Checker account or login client not found; nothing changed."; exit 1; }
case "$host" in postgres|postgres.default*) ;; *) echo "Datasource is not the in-cluster postgres; nothing changed."; exit 1;; esac
[[ "$dbuser" =~ ^[A-Za-z0-9_]+$ && "$schema" =~ ^[A-Za-z0-9_]+$ ]] || { echo "Unexpected database names; nothing changed."; exit 1; }
echo "existing tables: $(psql -Atc "select count(*) from pg_tables where schemaname='$schema' and tablename in ('palais_access_request','palais_access_event')" </dev/null)/2"
[ "$mode" = --apply ] || { echo "Check only; rerun with --apply."; exit; }

echo "1/4 database"
{ echo "begin; set local search_path to \"$schema\";"; printf '%s\n' "$sql" | sed '$s/[^;]$/&;/'
  echo "grant usage on schema \"$schema\" to \"$dbuser\";
grant select,insert,update,delete on palais_access_request to \"$dbuser\";
grant select,insert on palais_access_event to \"$dbuser\";
grant usage,select on sequence palais_access_event_id_seq to \"$dbuser\"; commit;"; } | psql -q

echo "2/4 Keycloak"
api "$A/roles/palais-checker" >"$tmp/role" 2>/dev/null || {
  api -X POST "$A/roles" -d '{"name":"palais-checker"}'; api "$A/roles/palais-checker" >"$tmp/role"; }
jq '[.]' "$tmp/role" | api -X POST "$A/users/$uid/role-mappings/realm" -d @-
if ! api "$A/clients/$lid/protocol-mappers/models" | jq -e 'any(.name=="palais-api-audience")' >/dev/null; then
  api -X POST "$A/clients/$lid/protocol-mappers/models" -d '{"name":"palais-api-audience","protocol":"openid-connect","protocolMapper":"oidc-audience-mapper","config":{"included.custom.audience":"'$aud'","access.token.claim":"true","id.token.claim":"false"}}'
fi
[ -n "$sid" ] || { api -X POST "$A/clients" -d '{"clientId":"'$svc'","protocol":"openid-connect","publicClient":false,"serviceAccountsEnabled":true,"standardFlowEnabled":false,"directAccessGrantsEnabled":false,"implicitFlowEnabled":false}'; sid="$(cid "$svc")"; }
sa="$(api "$A/clients/$sid/service-account-user" | jq -er .id)"; rm_id="$(cid realm-management)"
api "$A/clients/$rm_id/roles" | jq '[.[]|select(.name=="manage-users" or .name=="view-users" or .name=="view-realm")]' \
  | api -X POST "$A/users/$sa/role-mappings/clients/$rm_id" -d @-
api "$A/clients/$sid/client-secret" | jq -ej .value >"$tmp/PALAIS_ACCESS_CLIENT_SECRET"

echo "3/4 Kubernetes"
k create secret generic palais-access-credentials --from-file="$tmp/PALAIS_ACCESS_CLIENT_SECRET" --dry-run=client -o yaml >"$tmp/s.yaml"
k apply -f "$tmp/s.yaml"
k create cm palais-access-config --from-literal=PALAIS_ACCESS_ENABLED=true --from-literal=PALAIS_ACCESS_APPROVALS=1 \
  --from-literal=PALAIS_ACCESS_INITIAL_CHECKER_ID="$uid" --from-literal=PALAIS_ACCESS_ISSUER="$issuer" \
  --from-literal=PALAIS_ACCESS_AUDIENCE=$aud --from-literal=PALAIS_ACCESS_CLIENT_ID=$svc --dry-run=client -o yaml >"$tmp/c.yaml"
k apply -f "$tmp/c.yaml"

echo "4/4 restart"
restart
echo "Done. Sign out and in again, then open https://tymultiverse.cc/account-settings. Undo: --disable"
REMOTE
