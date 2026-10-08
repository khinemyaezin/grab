# Security manifest consistency runbook

Use this runbook during release, retirement, restore, and conflict response.

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
