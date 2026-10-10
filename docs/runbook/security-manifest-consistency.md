# Security manifest consistency runbook

Use this runbook during release, retirement, restore, and conflict response.

## Identity catalog initialization and lock contention

Catalog-dependent writes create `security_catalog_state` on first use. A new catalog starts at revision zero; the initializer commits in its own identity transaction before registration, access, invitation, or role work begins. GET status remains read-only and reports revision zero if the singleton is absent.

Concurrent first initialization is accepted only when PostgreSQL identifies the duplicate on `security_catalog_state_pkey`; the failed initializer transaction rolls back before a fresh transaction verifies the winner. Investigate and propagate any other constraint failure. If an initialized catalog loses its singleton row, treat it as a recovery incident and restore from a valid database backup or approved baseline. Do not recreate it at revision zero when catalog data already exists.

Activation writers wait on the `PESSIMISTIC_WRITE` row lock using the database-configured lock timeout. A timeout or deadlock propagates through the identity outbox path for its existing recovery behavior. The database does not guarantee FIFO lock order or a fixed wait duration. Allow identity pool capacity for one additional connection when an identity transaction is suspended around initialization.

The PostgreSQL integration tests create clean schemas through Hibernate. Other database engines have not been verified. Migration-specific checks are tagged separately because the Flyway source folders are unavailable in the current checkout.

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
