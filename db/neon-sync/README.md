# K8s to Neon people/weapon sync

This is a one-way mirror. The in-cluster `peoplesystem` database is authoritative;
Neon receives exact copies of only `public.people` and `public.weapon`.

The Deployment keeps one internal PostgreSQL `LISTEN` connection open. Statement
triggers on `people` and `weapon` emit `NOTIFY` after insert, update, delete, or
truncate while also updating a durable outbox row. There is no polling and no
idle Neon traffic. A five-second debounce combines bursts of writes into one
sync.

The CronJob also runs every six hours at minute 17 in `Asia/Taipei` as a
reconciliation fallback. Each sync:

1. verifies the source, Neon target, and pgvector extension;
2. stores the previous Neon rows in `sync_backup.people_previous` and
   `sync_backup.weapon_previous`;
3. replaces both Neon tables in one transaction;
4. compares row counts and deterministic content hashes;
5. retains pre-sync dumps for 14 days.

## Install

Create the credential Secret without committing its values:

```bash
kubectl create secret generic neon-sync-credentials -n default \
  --from-literal=host='<neon-host>' \
  --from-literal=username='<neon-user>' \
  --from-literal=password='<neon-password>' \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl apply -f db/neon-sync/k8s-cronjob.yaml
```

Applying the manifest starts the watcher and installs the triggers. The initial
outbox marker causes one immediate synchronization.

## Run immediately

```bash
JOB="people-weapon-neon-sync-manual-$(date +%s)"
kubectl create job -n default \
  --from=cronjob/people-weapon-neon-sync "$JOB"
kubectl wait -n default --for=condition=complete "job/$JOB" --timeout=1800s
kubectl logs -n default "job/$JOB"
```

From the workstation:

```powershell
ssh oke-node 'JOB="people-weapon-neon-sync-manual-$(date +%s)"; kubectl create job -n default --from=cronjob/people-weapon-neon-sync "$JOB"; kubectl wait -n default --for=condition=complete "job/$JOB" --timeout=1800s; kubectl logs -n default "job/$JOB"'
```

## Watch the bridge

```bash
kubectl get deployment,pod -n default -l app=people-weapon-neon-sync
kubectl logs -n default deployment/people-weapon-neon-sync -f
```

## Restore the previous Neon copy

The immediately previous Neon copy is stored in schema `sync_backup`. Stop the
watcher and CronJob before a manual restore:

```bash
kubectl patch cronjob people-weapon-neon-sync -n default \
  --type=merge -p '{"spec":{"suspend":true}}'
kubectl scale deployment people-weapon-neon-sync -n default --replicas=0
```

Restore `sync_backup.people_previous` and `sync_backup.weapon_previous` into the
public tables in a transaction. Resume the Deployment and CronJob only after
verifying the restored data.
