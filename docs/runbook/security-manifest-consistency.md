# Security manifest consistency runbook

Use this runbook during release, retirement, restore, and conflict response.

## Owner publication concurrency upgrade

The optimistic publication release requires a `version BIGINT NOT NULL DEFAULT 0` column on each merchant, catalog, inventory, saleschannel, and identity `security_manifest_publication` table. Apply an additive change that preserves existing state rows and database guards. Fresh tables need no seed rows.

For each owner module, stop and drain every legacy publisher before applying the version-column change. Deploy and start the optimistic publisher only after the schema is ready. Do not overlap legacy and optimistic publishers: older binaries do not update the version column and cannot participate in optimistic conflict detection. Startup publication remains asynchronous; the scheduled publication sweep repairs missed enqueue attempts.

The owner command retries optimistic-lock and first-row insertion conflicts up to three total attempts. Each attempt uses a fresh transaction and rechecks the committed revision, digest, and publication lease. If all attempts fail, investigate the owner database and retry through the normal publication sweep after correcting the cause.

## Release

1. Publish the complete owner manifest at the next revision and verify its digest.
2. Confirm the identity catalog reports the owner revision and digest as `APPLIED`.
3. Enable the dependent feature only after required authority and scope references are present.

## Dependency wait or quarantine

`WAITING_DEPENDENCY` is retried by the identity revalidation command every 30 seconds. Verify the dependency owner revision and its active scope before intervening. `QUARANTINED` requires a corrected higher revision; do not replay the same payload indefinitely.

## Retirement

Publish explicit `RETIRED` tombstones. Keep role and assignment rows for audit; effective authorization rejects the retired authority or scope. Remove runtime references only after the identity catalog reports the retirement applied.

## Restore or rollback

Treat restored watermarks as stale until approved revision/digest baselines are checked externally. Republish complete snapshots from every owner. Corrective rollback uses a higher revision and never decrements a catalog watermark or reactivates a retired key.
