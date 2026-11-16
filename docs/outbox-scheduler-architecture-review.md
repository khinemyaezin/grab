# Outbox & Scheduler Architecture Review

> Scope: the transactional outbox, consumer inbox, scheduled jobs, cluster locking, and boot-time tasks as
> implemented for `docs/outbox-events-scheduling-plan.md` (Phases 1–5). Status as of 2026-11-14, branch
> `feature/outbox`. Companion docs: `bootstrap-and-probes.md`, `outbox-observability.md`.

---

## 1. Architecture at a glance

```
 Command handler (module tx)                       ┌────────────── every replica ──────────────┐
 ─────────────────────────────                     │                                           │
 aggregate change ──┐                              │  hot queue  (ids, after commit)           │
                    ├── same transaction ──►  outbox_record ──►  claim (SKIP LOCKED) ──► dispatch│
 DomainEventProducer┘  (module schema)             │  cold queue (poller batch, every 5s)      │
                                                   │        │                                  │
                                                   │        ▼                                  │
                                                   │  ApplicationEventPublisher (in-process)   │
                                                   │        │                                  │
                                                   │  @EventListener ─► CommandBus ─► handler   │
                                                   │                     └─ processed_event    │
                                                   │                        (inbox, same tx)   │
                                                   └───────────────────────────────────────────┘
```

| Concern | Implementation | Location |
|---|---|---|
| Write | `JpaOutboxDomainEventProducer` inserts rows in the caller's transaction and stamps `event_id` + `traceId` headers | `outbox-infrastructure/.../jpa` |
| Delivery | `AbstractOutboxProcessor` (one subclass per module, 10 modules) | `outbox-infrastructure`, `*-adapter-persistence/outbox` |
| Hand-off | `DualQueueOutboxRelay`: hot queue (after-commit ids) + cold queue (poller claims), N worker threads | `outbox-infrastructure` |
| Claiming | `FOR UPDATE SKIP LOCKED` batch claim; `claim_token` + `claimed_at`; stale claims reclaimed after `claimTimeout` (2 min) | `JpaOutboxStore` |
| Outcome | Re-read under row lock, apply only if still `isClaimedBy(token)`; otherwise `claim.lost` | `AbstractOutboxProcessor` |
| Retry | Exponential backoff (30 s base, 15 min cap), `DEAD` after 10 attempts or poison payload | `OutboxRetryPolicy` |
| Consumer dedupe | `processed_event (consumer, event_id)` inserted in the handler transaction before the side effect | `ProcessedEventRepository`, `JpaProcessedEventRepository` |
| Cluster singletons | `SingletonJobExecutor` → ShedLock (`JdbcTemplateLockProvider`, DB time), one `shedlock` table per schema | `app/shared/scheduling` |
| Job classification | `@CompetingWorkerJob` (class A) vs `@ClusterSingletonJob` (class B), enforced by ArchUnit | `framework/scheduling`, `SchedulingArchitectureTest` |
| Boot-time work | `BootstrapTask` + `BootstrapRunner` (class C): lock, retry, readiness gating | `framework/bootstrap`, `app/shared/bootstrap` |
| Observability | Micrometer observer, backlog gauges, inbox/publication decorators, bootstrap gauges, `eventId` in MDC | `app/shared/outbox`, `app/shared/bootstrap` |
| Replay | `OutboxReplayer` (`DEAD → NEW` by module + id range), not exposed over HTTP | `app/shared/outbox` |

### Job classes

| Class | Meaning | Examples | Guard |
|---|---|---|---|
| A — competing worker | Runs on every replica; work is partitioned by row locks | outbox pollers | `SKIP LOCKED` claims |
| B — cluster singleton | At most one replica per interval | outbox cleanup, inbox retention, manifest repair publish | ShedLock (`lockAtMostFor` / `lockAtLeastFor`) |
| C — bootstrap | Once per rollout, must succeed before the pod is "really" ready | marketplace channel ensure, security manifests, role declaration | ShedLock `bootstrap:<key>` + idempotent body + readiness |

---

## 2. Use cases walked through

### UC1 — A command emits an integration event (happy path)
1. Handler mutates the aggregate; `DomainEventProducer.produce` inserts `outbox_record(status=NEW, event_id)` in the
   **same** module transaction.
2. After commit, the row id is offered to the hot queue → a relay worker claims it (`SKIP LOCKED`), dispatches through
   `ApplicationEventPublisher`, then marks it `PUBLISHED` in a new transaction.
3. Typical latency: milliseconds. The cold poller (5 s) is only the safety net.

**Guarantee:** event exists iff the business change committed (no dual-write).

### UC2 — Consumer listener fails
1. Listener throws → the processor records `FAILED`, `available_at = now + backoff(attempt)`.
2. The cold poller reclaims it later; after 10 attempts → `DEAD`, `outbox.dead` increments, log at ERROR.
3. Operator fixes the cause and calls `OutboxReplayer.replayDead(module, from, to, limit)`.

### UC3 — Multiple listeners, one fails
Listener A succeeded, B threw. The whole event is retried, so A runs again. A's handler finds its
`processed_event` row and returns early; B eventually succeeds. **Correct only for consumers that use the inbox.**

### UC4 — Pod dies mid-dispatch
The row stays `PROCESSING` with an old `claimed_at`. After `claimTimeout` another replica reclaims it and redelivers.
If the first pod had actually finished the side effect, the inbox absorbs the duplicate. If the dead pod comes
back mid-flight, its outcome update sees a foreign claim token and records `claim.lost` instead of overwriting.

### UC5 — Graceful shutdown
`destroy()` stops the relay, drains the cold queue, and releases undispatched claims to `FAILED / available_at=now`
(attempt count restored), so nothing waits for the 2-minute claim timeout.

### UC6 — Burst of writes overflows the hot queue
`enqueueHot` returns false, `outbox.hot_queue.dropped` increments; the poller picks the rows up within one interval.
Latency degrades, correctness does not.

### UC7 — Three replicas start on an empty database
1. Flyway pre-creates manifest/role state rows (no insert race).
2. Each pod's `BootstrapRunner` tries `bootstrap:saleschannel.ensure-marketplace`; one wins, the others poll the lock,
   then run the (idempotent) task themselves. A unique-index race is retried in a fresh transaction (`@EnsureRetryable`).
3. Manifest tasks: first publisher gets `ENQUEUED`, the others `NOT_DUE`. Readiness flips UP when all required tasks
   are `SATISFIED` (with `bootstrap.gate-readiness=true`).

### UC8 — Rolling deploy with a manifest revision bump
New pods publish revision N+1 (`ENQUEUED`); old pods' repair job later gets `SUPERSEDED` (logged WARN, counted).
Identity keeps N+1 because `SecurityCatalog.consider` is monotonic.

### UC9 — Manifest edited without bumping the revision
Caught in CI by `SecurityManifestDigestSnapshotTest`. If it slips through, publication returns `CONFLICT`, the
bootstrap task fails permanently, readiness stays DOWN, and `security.manifest.publication.result{result="CONFLICT"}`
fires — the rollout stalls instead of serving with wrong permissions.

### UC10 — Housekeeping
Outbox cleanup (published rows > 7 days, 5 000 per batch) and inbox retention (14 days) run as class B jobs: one
replica per interval, others record `scheduler.job.skipped_locked`.

### UC11 — Rebuilding the discovery read model
Not a startup concern any more: run the image as a Kubernetes Job with `JOBS_DISCOVERY_REBUILD_ENABLED=true`; it
loops resumable batches and exits.

---

## 3. Pros

| # | Strength | Why it matters |
|---|---|---|
| P1 | **Atomic write of state + event** in the module's own schema | No lost or phantom events; no XA / 2PC |
| P2 | **Low latency without hammering the DB** (hot queue after commit, cold poll as backstop) | Near-synchronous feel; poll interval only bounds recovery time |
| P3 | **Horizontal scale of delivery** via `SKIP LOCKED` | Replicas partition the backlog instead of queueing on the same rows |
| P4 | **Fencing by claim token** on every outcome update | A slow / resurrected worker cannot overwrite a newer claim |
| P5 | **Bounded retries + DEAD + replay** | Poison events stop looping; operators have a recovery lever |
| P6 | **Inbox in the consumer's transaction** | Turns at-least-once delivery into effectively-once side effects |
| P7 | **Schema isolation preserved** — outbox, inbox and `shedlock` tables live per schema | Fits the multi-schema / per-pool invariant; a module can be extracted with its tables |
| P8 | **Broker-ready seam**: only the `OutboxEventDispatcher` bean changes to adopt Kafka/SNS | `event_id`, inbox, versioning and ShedLock carry over unchanged |
| P9 | **Explicit job taxonomy enforced by ArchUnit** | New `@Scheduled` methods cannot silently run N times across replicas |
| P10 | **Boot work is observable and gated** | Failed bootstrap keeps a pod out of rotation instead of serving half-initialised |
| P11 | **No extra infrastructure** — PostgreSQL only | Simple ops and local dev; Testcontainers covers it |
| P12 | **Good telemetry** — backlog age, dead, claim lost, duplicates, lock skips, bootstrap status | Each failure mode in §2 has a metric to alert on |

---

## 4. Cons and risks

| # | Weakness | Impact | Mitigation / next step |
|---|---|---|---|
| C1 | **In-process dispatch**: all listeners of an event run on the delivering pod's thread, synchronously | One slow listener delays every other event in that worker; a listener failure retries *all* listeners | Keep listeners thin (dispatch a command). Broker phase gives per-consumer groups |
| C2 | **Inbox coverage is partial** — only customer registration, merchant lifecycle and workflow steps record `processed_event` | Other listeners may apply side effects twice on retry/reclaim (UC3/UC4) | Make inbox use a checklist item for every cross-module listener; consider an ArchUnit rule |
| C3 | **No ordering guarantee** per aggregate | Two events for the same aggregate can be delivered out of order across workers or after a retry | Consumers must be order-tolerant (versions/monotonic checks) or add per-aggregate sequencing later |
| C4 | **Polling cost scales with modules × replicas** (10 pollers per pod every 5 s, plus backlog gauge queries) | Steady DB load and connection usage per Hikari pool even when idle | Tune `fixed-delay-ms` per module; claims use the `(status, available_at)` and `claimed_at` indexes. As published rows pile up between cleanups, consider a partial index `WHERE status IN ('NEW','FAILED','PROCESSING')` |
| C5 | **Claim timeout is a fixed 2 min** | Handlers longer than that are redelivered while still running (`claim.lost`) | Keep handlers short; raise per-module `claim-timeout-ms` if `claim.lost` > 0 |
| C6 | **Hot queue is memory-only** | Dropped/never-enqueued ids wait for the poller (≤ 5 s) | Acceptable by design; watch `outbox.hot_queue.dropped` |
| C7 | **Bootstrap readiness = "enqueued", not "accepted by identity"** | A pod can be ready before identity has applied its manifest | Add a query port into identity for accepted revision (plan 4.1 `isSatisfied`) |
| C8 | **Bootstrap tasks run sequentially** with 2 min timeouts each | Worst-case readiness delay grows with every new task | Parallelise independent tasks or shorten timeouts once metrics show real durations |
| C9 | **ShedLock relies on `lockAtMostFor`** | A job outliving it can run concurrently on two replicas | Alert on `scheduler.job.duration > lockAtMostFor/2`; keep jobs batch-bounded (they are) |
| C10 | **Replay has no HTTP/ops surface** | Recovery needs code or a one-off job | Decision deferred until an ops permission model exists |
| C11 | **Boilerplate per module** — 10 near-identical `*ModuleOutboxConfig`, processor and event classes | Easy to drift (e.g. a module missing inbox retention) | Consider a module descriptor + generated/auto-config beans |
| C12 | **Cross-module consistency is eventual** | UI may briefly show stale cross-module data | Expected for a modular monolith; surface `outbox.oldest_pending_age_seconds` SLO |
| C13 | **Mixed-version rollout window** (old pods without ShedLock/inbox/claim fencing coexist with new pods) | Duplicate class B runs and duplicate deliveries are possible during the first rollout | Follow plan §6 ordering; first rollout with `bootstrap.gate-readiness=false`; see §6 below |

---

## 5. Alternatives considered

| Option | Pros | Cons | Verdict |
|---|---|---|---|
| Spring Modulith event publication registry | Built-in, less code | Single shared table (breaks schema isolation), weaker claim/fencing, no inbox | Rejected for isolation |
| Debezium / CDC from outbox tables | No pollers, ordered per partition | Kafka + Connect infrastructure, ops overhead | Good fit for the broker phase |
| Direct `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` | Simplest | Events lost on crash, no retry, no dedupe — the original problem | Removed (ArchUnit forbids `@Async` listeners) |
| Quartz clustered scheduler instead of ShedLock | Rich scheduling, misfire handling | Heavy schema, overkill for fixed-delay jobs | ShedLock is sufficient |
| Leader election (K8s Lease) for singletons | One leader for everything | Single point of slowness, extra RBAC | Per-job locks are finer grained |

---

## 6. Open items

1. Extend inbox recording to every cross-module listener (C2) and add an ArchUnit check.
2. Identity-accepted-revision check for manifest bootstrap tasks (C7).
3. Ops permission model, then expose `OutboxReplayer` (C10).
4. First-rollout verification against the deployed release: confirm that old pods' `PublicationEligibilityPolicy`
   treats the Flyway pre-created state rows (`revision 0`, empty digest) as "publish due", and that the
   `timestamp → timestamptz` `ALTER` (table rewrite, exclusive lock) is acceptable for current outbox table sizes.
5. Two-context integration tests for the boot races (plan §5) — today they are covered by unit tests with a fake lock.
6. Broker phase: swap `OutboxEventDispatcher` for a producer; consumer groups then remove C1.
