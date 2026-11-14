# Bootstrap tasks & Kubernetes probes (plan Phase 4)

## Bootstrap tasks (scheduling class C)

Implement `com.grab.framework.bootstrap.BootstrapTask` as a `@Component` in the owning module's
`internal/adapter/inbound/lifecycle/` package; order with `@Order`. `BootstrapRunner` (`app/shared/bootstrap`) runs
all tasks once after `ApplicationReadyEvent` on the `bootstrap-runner` thread:

* lock `bootstrap:<key>` through the task's module `SingletonJobExecutor` (keep keys ≤ 54 chars: ShedLock `name` is
  `varchar(64)`);
* a replica that loses the lock polls (`bootstrap.lock-poll-interval`) and then executes the task itself, so
  `execute()` must be idempotent;
* transient failures retry with exponential backoff (`bootstrap.max-attempts`, `initial-backoff`, `max-backoff`)
  within `timeout()`; `BootstrapTaskException.permanent(..)` stops immediately (e.g. manifest digest `CONFLICT`);
* required tasks that are not `SATISFIED` turn the `bootstrap` health contributor DOWN when
  `bootstrap.gate-readiness=true`.

| Task key | Module | Order |
|---|---|---|
| `saleschannel.ensure-marketplace` | saleschannel | 10 |
| `catalog.security-manifest` | catalog | 100 |
| `inventory.security-manifest` | inventory | 110 |
| `identity.security-manifest` | identity | 120 |
| `merchant.security-manifest` | merchant | 130 |
| `merchant.role-declaration` | merchant | 131 |
| `saleschannel.security-manifest` | saleschannel | 140 |

ArchUnit forbids `ApplicationReadyEvent` listeners outside `app/shared/bootstrap` and `@Async` on `@EventListener`s.

## Rollout

1. Deploy with `BOOTSTRAP_GATE_READINESS=false` (default). Check `GET /actuator/health/readiness` details (authenticated)
   and `bootstrap-runner` logs.
2. Set `BOOTSTRAP_GATE_READINESS=true`.

## Probes

```yaml
startupProbe:            # covers JVM start + Flyway; bootstrap runs after startup and is gated by readiness
  httpGet: { path: /actuator/health/liveness, port: 8080 }
  periodSeconds: 5
  failureThreshold: 60   # 5 min
livenessProbe:
  httpGet: { path: /actuator/health/liveness, port: 8080 }
  periodSeconds: 10
  failureThreshold: 3
readinessProbe:          # readinessState + bootstrap
  httpGet: { path: /actuator/health/readiness, port: 8080 }
  periodSeconds: 5
  failureThreshold: 3
```

Give the Deployment `progressDeadlineSeconds` larger than the longest required task timeout (2 min) plus startup, so a
release whose bootstrap fails (e.g. manifest `CONFLICT`) pauses the rollout instead of crash-looping.

## Discovery rebuild (one-shot job)

Not run on startup. Run the same image as a Kubernetes Job with `JOBS_DISCOVERY_REBUILD_ENABLED=true`; it loops
resumable batches (`STOREFRONTQUERY_REBUILD_BATCH_SIZE`) until complete and exits 0/1.

## Manifest digest snapshot

`app/src/test/resources/security-manifest-digests.properties` records the digest per published revision. Changing a
manifest or role declaration without bumping its revision fails `SecurityManifestDigestSnapshotTest`; after bumping,
append the line the test prints. Never edit an existing line.
