# Plan: Outbox, Events, Scheduling & Boot-time Consistency

| | |
|---|---|
| **Date** | 2026-11-13 |
| **Status** | Proposed |
| **Parent doc** | [Architecture Review & Production Readiness](architecture-review-production-readiness.md), §3.3 |
| **Goal** | Make outbox delivery, integration-event consumption, scheduled jobs and boot-time tasks (security-manifest publication, default channel provisioning, seeds) **correct when N replicas run concurrently** and during rolling deploys. |
| **Rules honoured** | AGENTS.md R7 (transactions on handlers), R11 (no native queries in Spring Data repos), R16, R17 (outbox + `@EventListener`), R22 (Testcontainers). |

---

## 1. Current State (as found in code)

### 1.1 Outbox pipeline

```
 Handler TX (module TM)                     after commit                       relay worker / poller
 ─────────────────────                      ────────────                       ─────────────────────
 aggregate change ─► outbox_record(NEW) ──► SpringOutboxCommitHook ──► hotQueue ─► claimById (FOR UPDATE)
                                                                                    │ status=PROCESSING, claim_token
 @Scheduled poller (5s) ─► findBatchForProcessing (FOR UPDATE, no SKIP LOCKED) ─► coldQueue
                                                                                    ▼
                                         publishEvent  (REQUIRES_NEW on source module TM)
                                           ├─ findById (no lock) + claim_token check
                                           ├─ ApplicationEventPublisher.publishEvent(event)   ← all listeners, synchronously
                                           └─ markPublished  |  markFailed(retryDelay)        ← no max attempts
```

Key code: `outbox-infrastructure/.../AbstractOutboxProcessor.java`, `DualQueueOutboxRelay.java`, `jpa/JpaOutboxStore.java`, and one `*OutboxEventProcessor` per module (10 modules, each with 2 `@Scheduled` methods).

### 1.2 Boot-time and periodic tasks

| Task | Trigger | File |
|---|---|---|
| Publish security manifest (catalog, inventory, identity, merchant, saleschannel) | `ApplicationReadyEvent` + `@Async`, then `@Scheduled` republish every 5 min | `*/internal/adapter/inbound/lifecycle/*SecurityManifestLifecycleTrigger.java` |
| Publish merchant role declaration | `ApplicationReadyEvent` + `@Async`, on `IdentitySecurityCatalogActivatedIntegrationEvent`, `@Scheduled` repair | `MerchantRoleDeclarationStartupPublisher.java` |
| Identity manifest revalidation | `@Scheduled` every 30s | `IdentitySecurityManifestRevalidationScheduler.java` |
| Ensure default marketplace sales channel | `ApplicationReadyEvent` (sync) | `DefaultSalesChannelStartupListener.java` → `EnsureMarketplaceChannelService` |
| Workflow sweeper | `@Scheduled` 30s | `WorkflowSweeper.java` |
| Outbox poll + cleanup | `@Scheduled` (×10 modules) | `*OutboxEventProcessor.java` |
| Seeds (`*_SEED_ENABLED`) | startup | per-module config |

### 1.3 What is already good

* **Manifest publication is already concurrency-aware.** `SecurityManifestPublicationAdapter` has:
  * a per-module state row with `@Version`;
  * `PublicationEligibilityPolicy`, which returns `SUPERSEDED`, `CONFLICT`, `NOT_DUE` or `ENQUEUED` based on revision, digest and `nextPublicationAt`;
  * `@SecurityManifestPublicationRetryable`, which retries on optimistic-lock and unique-key errors;
  * `REQUIRES_NEW` handler transactions.

  The race is therefore *detected*. The work below is mostly about making it cheap, observable and loss-free.
* **The marketplace channel has a DB guard:** the partial unique index `uq_sales_channels_marketplace`.
* **The outbox has a claim token.** The claim timeout recovers events claimed by dead pods.
* **Identity has a revalidation loop** (`ListWaitingSecurityManifestCandidatesQuery`) that repairs manifests stuck in "waiting".

---

## 2. Problems to Solve

| ID | Problem | Evidence | Impact |
|---|---|---|---|
| **P1** | Batch claim uses `FOR UPDATE` without `SKIP LOCKED` | `JpaOutboxStore.findBatchForProcessing` | Replicas serialize on the same rows. Throughput does not scale and lock waits pile up. |
| **P2** | `publishEvent` re-reads the row without a lock and `markPublished` has no version check | `AbstractOutboxProcessor.publishEvent`, no `@Version` on `*OutboxEvent` | If a listener runs longer than `claim-timeout` (120s), another pod reclaims the row. Both dispatch, and the last writer wins the status. |
| **P3** | No max attempts or dead-letter state | `markFailed(now, error, retryDelay)` forever | A poison event retries every 30s forever, cluttering logs and the claim scan. |
| **P4** | One outbox row fans out to all listeners synchronously. One failure marks the whole event FAILED. | `dispatcher = applicationEventPublisher::publishEvent` | On retry, listeners that already succeeded run **again**, giving duplicate side effects unless every consumer is idempotent. |
| **P5** | No stable `event_id` on the outbox row (only `bigint identity`). Consumers can't dedupe. | `V1__initial_schema.sql` `outbox_record` | Idempotency is impossible to implement generically. |
| **P6** | `@Async @EventListener` on integration-event consumers | `IdentitySecurityManifestRegistrationListener`, `IdentityRoleDeclarationRegistrationListener` | The outbox marks the event PUBLISHED as soon as the async task is *submitted*. If the handler fails or the pod dies, the event is **lost** (at-most-once). Only the revalidation loop and the periodic republish hide this. |
| **P7** | Every `@Scheduled` job runs on every replica | 28 `@Scheduled` methods, no lock | N× manifest republish attempts (optimistic-lock retry storms), N× cleanup deletes, N× workflow sweeps (risk of double-advancing a workflow). |
| **P8** | Scheduler pool of 5 threads for ~28 jobs | `spring.task.scheduling.pool.size: 5` | A slow cleanup or sweep delays outbox pollers. Fixed-delay jobs starve. |
| **P9** | Boot tasks race across replicas | `ApplicationReadyEvent` handlers | (a) `EnsureMarketplaceChannel`: both pods see "missing", both insert, one gets a unique violation. The exception in an `ApplicationReadyEvent` listener **fails startup** of that pod (crash loop on first deploy). (b) Manifest state row: first-insert race resolved only by retry. |
| **P10** | Readiness does not wait for required boot tasks | `@Async` startup triggers, no readiness gate | A pod takes traffic before its permission manifest is registered, so authorization checks fail with 403 during deploys. |
| **P11** | Ordering not guaranteed per aggregate | 2 hot workers + cold path + retries | Consumers may see `revision N+1` before `N`. This is fine for manifests (revision-checked) but not guaranteed for other events. |
| **P12** | `LocalDateTime.now()` (JVM-zone) for claim/available/published times | `AbstractOutboxProcessor` | Pods in different time zones, or a DST change, give wrong staleness and retention calculations. |
| **P13** | Shutdown leaves claimed-but-queued cold items PROCESSING | `DualQueueOutboxRelay.stop()` | Up to 120s delivery delay after every rolling deploy. |
| **P14** | `JavaSerializationOutboxEventSerializer` still exists | `framework/outbox` | Deserialization-gadget risk and brittle versioning. |
| **P15** | No outbox or scheduler metrics | — | You can't alert on lag, dead letters or stuck workflows. |

---

## 3. Target Design

### 3.1 Delivery guarantee

**At-least-once delivery + idempotent consumers + version-aware state.** Exactly-once is not a goal. All three parts are required:

1. **Producer:** a row is written in the business TX. Claiming is concurrency-safe (P1, P2). Retries are bounded, then the row moves to DEAD (P3).
2. **Transport:** in-process today. Later a broker, where the outbox relay or Debezium publishes. The contract stays the same.
3. **Consumer:** the consumer records `(consumer, event_id)` in its own schema **in the same transaction as its side effect**. A duplicate becomes a no-op (P4, P5). Consumers that need ordering compare an aggregate `revision`/`version` and ignore stale events (P11).

### 3.2 Outbox state machine

```
          claim (SKIP LOCKED)         dispatch ok
  NEW ───────────────────────► PROCESSING ──────────► PUBLISHED ──(retention)──► deleted
   ▲                              │  │
   │ availableAt = now+backoff    │  │ claim expired (pod died) → reclaimable
   └──────────── FAILED ◄─────────┘  │
                   │ attempt_count ≥ max
                   ▼
                 DEAD  ──(manual replay: admin command)──► NEW
```

* Backoff: `retryDelay × 2^(attempt-1)` with ±20% jitter, capped (e.g. 15 min).
* `max-attempts` is configurable per module (default 10).
* Status writes after dispatch are **conditional**: `where id = :id and claim_token = :token and status = 'PROCESSING'`. If 0 rows change, the claim was lost and the outcome is discarded (and logged).

### 3.3 Per-consumer idempotency (inbox)

One table per **consumer schema** (each group already owns its schema):

```sql
-- Flyway, per consuming module (e.g. identity)
CREATE TABLE processed_event (
    consumer      varchar(150) not null,   -- e.g. 'identity.RegisterSecurityManifest'
    event_id      uuid         not null,
    processed_at  timestamptz  not null default now(),
    PRIMARY KEY (consumer, event_id)
);
```

* A framework port, `ProcessedEventRepository.markProcessed(consumer, eventId) → boolean` (JPA: `persist` and catch the unique violation), is called **inside the consumer's command handler transaction**, before the side effect. If it returns false, the handler returns early.
* Listeners pass `eventId` into the command. `RegisterSecurityManifestCommand` already carries `eventId`, so that pattern extends to other commands.
* Retention: a ShedLock'd job deletes rows older than the outbox retention plus a safety margin (e.g. 14 days).

### 3.4 Listener execution model

* Integration-event listeners are **synchronous** (`@EventListener`, no `@Async`). They run inside the outbox publish step, so a failure means FAILED and a retry, and nothing is lost.
* Each listener dispatches a command with `REQUIRES_NEW` on its own group transaction manager (already the convention). It commits independently, which is why the inbox (3.3) is needed.
* If a consumer is slow or heavy and must not block the source outbox worker, it **persists the work first**. Options: a command that writes to the consumer's own outbox or workflow, or the inbox row with status `PENDING` processed by its own poller. It must not use `@Async`.
* **Later (broker phase):** each consumer becomes its own consumer group, which removes P4 entirely. Until then the inbox makes re-running listeners harmless.

### 3.5 Scheduling model

Every scheduled job falls into one of three classes:

| Class | Rule | Jobs |
|---|---|---|
| **A: Competing workers** (safe on all replicas) | Work is partitioned by row locks (`SKIP LOCKED`). No global lock. | Outbox pollers (×10) |
| **B: Singleton** (one replica at a time) | `@SchedulerLock` (ShedLock, JDBC provider, `shedlock` table in each group schema) | Outbox cleanup (×10), `WorkflowSweeper`, manifest republish (×5), merchant role repair, identity revalidation, inbox cleanup |
| **C: Boot task** (once per deploy, cluster-wide) | `BootstrapTask` runner (3.6) | Ensure marketplace channel, initial manifest publication, role declaration, seeds |

ShedLock details:

* Use the **JDBC table provider** (`JdbcTemplateLockProvider`, `usingDbTime()`), *not* advisory locks, because advisory locks don't survive PgBouncer transaction mode.
* ShedLock issues its own SQL through `JdbcTemplate`. This is infrastructure and not a Spring Data repository, so it doesn't conflict with R11's native-query ban. Record that as an explicit ArchUnit exception for `net.javacrumbs.shedlock..`.
* Use one `LockProvider` per group datasource. Each module's job locks in its own schema, so no cross-schema coupling.
* Set `lockAtMostFor` to about 3× the expected run time and `lockAtLeastFor` to about 50–80% of the interval, which prevents back-to-back runs on different nodes.

Thread pools:

* `spring.task.scheduling.pool.size`: at least the number of class A jobs plus 4 (≈ 14). Alternatively, enable virtual threads (`spring.threads.virtual.enabled=true`, which Boot 3.2+ also applies to the scheduler) **after** PgBouncer and pool budgets are in place.
* Define an explicit bounded `@Async` executor (`ThreadPoolTaskExecutor` with a bounded queue and `CallerRunsPolicy`) for whatever `@Async` remains. Today the Boot default queue is unbounded.

### 3.6 Boot-time task framework (`BootstrapTask`)

The problem: several replicas start together (first deploy, scale-out, rolling restart). Boot tasks must be **idempotent, race-safe, non-fatal, and must gate readiness when they are required**.

```
ApplicationReadyEvent
   └─ BootstrapRunner (app/shared, runs on a dedicated thread, NOT @Async fire-and-forget)
        for task in tasks ordered by @Order:
           ├─ try ShedLock "bootstrap:<task.key()>"   (lockAtMostFor = task.timeout)
           │     ├─ acquired  → run task.execute()   (dispatches a Command; handler is idempotent)
           │     └─ not acquired → another pod is running it → poll task.isSatisfied() until done/timeout
           ├─ on failure: retry with backoff (bounded); if task.required() and still failing → stay NOT READY
           └─ record outcome + metric
        when all required tasks satisfied → AvailabilityChangeEvent.publish(ReadinessState.ACCEPTING_TRAFFIC)
```

```java
// framework (pure Java, no Spring)
public interface BootstrapTask {
    String key();                 // "saleschannel.ensure-marketplace"
    boolean required();           // gates readiness?
    Duration timeout();
    void execute();               // MUST be idempotent; dispatches a Command via the group bus
    boolean isSatisfied();        // cheap read check (dispatches a Query) — used by non-leader pods
}
```

* Each module contributes its tasks as adapters in `internal/adapter/inbound/lifecycle/`, replacing the current `@EventListener(ApplicationReadyEvent)` + `@Async` triggers. This keeps the existing hexagonal shape, because adapters dispatch to the group bus.
* **Liveness stays UP and readiness stays DOWN** until required tasks pass. Kubernetes holds traffic, rolling deploys pause on a bad release, and nothing crash-loops.
* Rules for every boot task's handler (DB-level idempotency, independent of the lock):
  1. **Ensure pattern:** find → create → on unique violation, re-read and return the existing row. The lock is an optimisation; the unique constraint is the guarantee.
  2. **Monotonic writes:** only move state forward (revision, digest). An older pod during a rolling deploy must lose (`SUPERSEDED`).
  3. **No `ddl-auto` reliance:** every table and unique index a boot task needs comes from Flyway.

### 3.7 Security manifest publication: concurrency matrix

| Scenario | Today | Target |
|---|---|---|
| Two pods boot at once and no state row exists | Both `persist`, one gets a PK violation, `@Retryable` re-runs, re-read gives `NOT_DUE` | Pre-create the state row per module key **in Flyway** (`INSERT … ON CONFLICT DO NOTHING` in the migration, which is allowed because R11 only bans native queries in repositories). The insert race disappears, and the bootstrap lock means only one pod publishes. |
| N pods' 5-min republish timers fire together | N optimistic-lock attempts, N−1 get `NOT_DUE` or retry | ShedLock class B with `lockAtLeastFor` ≈ 4 min. Exactly one attempt per interval. |
| Rolling deploy: old pod (rev N) and new pod (rev N+1) | Policy returns `SUPERSEDED` for the old pod. ✔ | Keep it, and add a metric/log at WARN for `SUPERSEDED` so stale pods are visible. |
| Same revision, different digest (developer changed the manifest without bumping the revision) | `CONFLICT` returned, then silently ignored? | **Fail the bootstrap task** (readiness DOWN, alert). Also add a CI test: a stored digest snapshot per revision must match, or the revision must be bumped. |
| Identity consumer receives rev N+1 then a late rev N | Identity `@Version` entities plus revision compare | Make it explicit: `RegisterSecurityManifest` ignores `revision < current`, no-ops on `revision == current && digest == current`, and rejects (DEAD, alert) on `revision == current && digest != current`. Use the inbox for `eventId` dedupe. |
| Identity handler fails after the async listener was accepted | **Lost.** Repaired only by the next republish and revalidation | Listener becomes synchronous (P6), so the outbox retries it. The revalidation loop stays as a safety net. |
| Identity activates catalog, so merchant must re-publish its role declaration | Event-triggered publish runs on every pod | It's an outbox-delivered event, so it is handled once (per delivery), and the publish is idempotent through the eligibility policy. No lock needed. |
| Pod takes traffic before its manifest is registered | 403s possible during deploy | Readiness gated on the `security-manifest.<module>` bootstrap task, with `isSatisfied()` = "identity has accepted revision ≥ mine" (a query through the identity port). |

### 3.8 Other boot-time use cases covered by the same framework

| Task | Idempotency guard | Required for readiness? |
|---|---|---|
| Ensure marketplace sales channel | `uq_sales_channels_marketplace` plus the ensure pattern (catch `DataIntegrityViolationException`, re-read) | Yes |
| Security manifest publication (×5) | State row + `@Version` + eligibility policy | Yes, for the module's own manifest |
| Merchant role declaration | `RoleDeclarationPublicationState` `@Version` + eligibility | Yes |
| Reference data (categories, regions, currencies) | **Move to Flyway** versioned/repeatable migrations, not app seeds | n/a (migration job) |
| Dev/demo seeds | Profile `dev` only. Business-key unique constraints plus the ensure pattern. | No |
| Discovery projection rebuild (`STOREFRONTQUERY_REBUILD_ON_STARTUP`) | Turn it into an **admin command or K8s Job**, not a boot task. If kept, run it under a bootstrap lock with a "rebuild generation" marker row. | No |
| Default workflow definitions (`WORKFLOW_SEED_ENABLED`) | Upsert by `(definition_key, version)` unique | Yes, if workflows are used at request time |

General rule: **schema and reference data belong to the migration job; runtime-derived state (manifests, provisioning) belongs to bootstrap tasks; heavy rebuilds belong to on-demand jobs.**

---

## 4. Work Breakdown

### Phase 1: Outbox correctness (no behaviour change for callers)

| # | Task | Files / modules | Notes |
|---|---|---|---|
| 1.1 | Add `SKIP LOCKED` to batch claim | `JpaOutboxStore.findBatchForProcessing` | `query.setHint("jakarta.persistence.lock.timeout", -2)` (Hibernate 6 → `FOR UPDATE SKIP LOCKED` on PG). JPQL only, so R11-compliant. |
| 1.2 | Hot-path claim must not block | `JpaOutboxStore.findByIdForUpdate` | Use the same hint. If the row is locked, skip it and the poller picks it up. |
| 1.3 | Conditional status transitions | `OutboxStore` + `JpaOutboxStore` + `AbstractOutboxProcessor.publishEvent` | Add `markPublished(id, token, at)` and `markFailed(id, token, …)` as JPQL bulk `update … where id=:id and claimToken=:token and status=PROCESSING`, returning the row count. Log at WARN if 0. Alternative: `@Version` on `*OutboxEvent`. |
| 1.4 | Stable `event_id` | All 10 `*OutboxEvent` entities, `OutboxRowFactory`, Flyway `V{n}__outbox_event_id.sql` per module | `uuid not null unique`, backfilled with `gen_random_uuid()`. Put it in the headers (`OutboxEventHeaders.EVENT_ID_KEY`) and expose it on `Event`/integration events. |
| 1.5 | Bounded retries + DEAD status | `OutboxStatus`, `OutboxEntry.markFailed`, Flyway migration (status check constraint), module config `*.outbox.max-attempts`, `*.outbox.max-backoff-ms` | Exponential backoff with jitter. |
| 1.6 | UTC time | `AbstractOutboxProcessor`, `OutboxEntry` | Inject `Clock`, use `Instant`. Migrate columns to `timestamptz` (Flyway: `ALTER … TYPE timestamptz USING … AT TIME ZONE 'UTC'`). |
| 1.7 | Graceful shutdown releases claims | `DualQueueOutboxRelay.stop`, `AbstractOutboxProcessor.destroy` | Drain, then for undrained cold items run a conditional reset `PROCESSING → FAILED, availableAt=now` (no attempt increment). |
| 1.8 | Remove the Java-serialization serializer | `framework/outbox/JavaSerializationOutboxEventSerializer` | Delete it, and add an ArchUnit rule forbidding `ObjectInputStream` in production code. |
| 1.9 | Cleanup in bounded batches | `JpaOutboxStore.deletePublishedOlderThan` | Delete by id range or batches of about 5k per TX to avoid long locks and WAL bursts. |

### Phase 2: Consumer idempotency & listener model

| # | Task | Notes |
|---|---|---|
| 2.1 | Framework port `ProcessedEventRepository` + JPA adapter base in `outbox-infrastructure` | `markProcessed(consumer, eventId)` uses `persist` + `flush` and catches the constraint violation, returning false. |
| 2.2 | `processed_event` table per consuming schema (Flyway) | identity, merchant, saleschannel, discovery/storefrontquery, catalog, inventory, pricing, cart, customer, region, workflows: whichever schemas have listeners. |
| 2.3 | Carry `eventId` on every integration-event-driven command | Commands are records, so add an `Id eventId` field. Mappers in listeners pass it through. |
| 2.4 | Handlers call `markProcessed` first and return early on duplicates | This is in the handler's existing `@{Module}Transactional`, so it is atomic with the side effect. |
| 2.5 | Remove `@Async` from `IdentitySecurityManifestRegistrationListener` and `IdentityRoleDeclarationRegistrationListener` | Fixes P6. If latency is a concern, measure first. Identity registration is a few rows. |
| 2.6 | Version-aware consumers | Audit the 89 listener sites. Every consumer of a mutable-aggregate event must compare `aggregateVersion`/`revision`. Add `aggregateVersion` to integration events where missing. |
| 2.7 | Inbox retention job (class B) | ShedLock'd. |

### Phase 3: Scheduling

| # | Task | Notes |
|---|---|---|
| 3.1 | Add ShedLock (`shedlock-spring`, `shedlock-provider-jdbc-template`) | Flyway `shedlock` table in each group schema. One `LockProvider` bean per group datasource, qualified (`accountLockProvider`, …). |
| 3.2 | Annotate class B jobs | `@SchedulerLock(name = "catalog.outbox.cleanup", lockAtMostFor = "PT10M", lockAtLeastFor = "PT4M")` and so on. Because there are multiple providers, use a small `ModuleLockingTaskExecutor` wrapper or ShedLock's `LockingTaskExecutor` with an explicit provider per job. |
| 3.3 | Leave class A (outbox pollers) unlocked | They depend on 1.1. |
| 3.4 | Scheduler and async pools | `spring.task.scheduling.pool.size: 14` (or virtual threads later). Add an explicit bounded `@Async` executor. |
| 3.5 | Make `WorkflowSweeper` idempotent per instance | Claim workflow instances with `SKIP LOCKED` (class A), or keep it singleton (class B). Prefer A for throughput. Its `@Version` already prevents double-advance, so add a metric for conflicts. |
| 3.6 | Job inventory test | ArchUnit/Modulith test: every `@Scheduled` method must have `@SchedulerLock` **or** be listed in a `CompetingWorkerJobs` allow-list. New jobs can't silently skip classification. |

### Phase 4: Bootstrap framework & boot-time consistency

| # | Task | Notes |
|---|---|---|
| 4.1 | `BootstrapTask` interface in `framework`, `BootstrapRunner` in `app/shared` | Runner uses ShedLock (`bootstrap:<key>`), bounded retries, readiness gating via `ApplicationAvailability` / `AvailabilityChangeEvent`. |
| 4.2 | Convert `DefaultSalesChannelStartupListener` into a `BootstrapTask` | Also harden `EnsureMarketplaceChannelService`: catch the unique violation in the **persistence adapter** (translate it to a domain `AlreadyExists` result), then re-read. The handler stays logic-free (R7). |
| 4.3 | Convert the 5 `*SecurityManifestLifecycleTrigger` and `MerchantRoleDeclarationStartupPublisher` | Startup part becomes a `BootstrapTask` (`required=true`). The periodic part stays `@Scheduled` with `@SchedulerLock`. The event-triggered part is unchanged. |
| 4.4 | Pre-create manifest and role-declaration state rows in Flyway | One row per module key, `ON CONFLICT DO NOTHING`. Keep `@SecurityManifestPublicationRetryable` as defence in depth. |
| 4.5 | `CONFLICT` handling | Publication `CONFLICT` makes the bootstrap task fail, keeps readiness DOWN and emits a metric/alert. Add a CI snapshot test of the manifest digest per revision. |
| 4.6 | Identity: explicit monotonic register rules (3.7) + inbox | In `RegisterSecurityManifest`/`RegisterRoleDeclaration` use cases or policies. |
| 4.7 | Move reference seeds into Flyway, restrict demo seeds to the `dev` profile | Set `*_SEED_ENABLED` to `false` by default in `application-prod.yml`. |
| 4.8 | Discovery rebuild becomes an admin command / K8s Job | Remove `REBUILD_ON_STARTUP` from prod config. |
| 4.9 | K8s probes | `readinessProbe: /actuator/health/readiness` (includes the bootstrap group), `livenessProbe: /actuator/health/liveness`, and a `startupProbe` with enough budget for the bootstrap timeout. |

### Phase 5: Observability

| Metric (Micrometer) | Tags | Alert |
|---|---|---|
| `outbox.pending` (gauge), `outbox.oldest_pending_age_seconds` | module | age > 60s for 5 min |
| `outbox.published`, `outbox.failed`, `outbox.dead` (counters) | module, event_type | `dead` > 0 |
| `outbox.hot_queue.dropped` | module | sustained > 0 |
| `outbox.claim.lost` (conditional update returned 0) | module | > 0 → investigate claim timeout |
| `inbox.duplicate` | consumer | informational |
| `scheduler.job.duration`, `scheduler.job.skipped_locked` | job | duration > `lockAtMostFor`/2 |
| `bootstrap.task.status` (gauge 0/1), `bootstrap.task.duration` | task | required task failed |
| `security.manifest.publication.result` | module, result (`ENQUEUED`/`NOT_DUE`/`SUPERSEDED`/`CONFLICT`) | `CONFLICT` > 0 |

Also propagate `traceId` (already in headers) **and** `eventId` into the MDC for listener logs. An admin replay endpoint (seller-central ops, permission-gated) re-queues events by `DEAD → NEW` per module and id range.

---

## 5. Testing Strategy (R22)

| Test | Type | What it proves |
|---|---|---|
| Concurrent claim | Testcontainers PG, 2 `AbstractOutboxProcessor` instances on separate threads, 1k rows | No event is published twice when handlers are fast. Both workers get work (no serialization). |
| Lost claim | Handler sleeps > claim timeout; second worker reclaims | The first worker's `markPublished` affects 0 rows. Status is consistent. The duplicate dispatch is absorbed by the inbox. |
| Retry → DEAD | Handler always throws | `attempt_count` grows with backoff, then DEAD at `max-attempts`. |
| Inbox dedupe | Deliver the same `eventId` twice | The side effect happens once. |
| Multi-listener partial failure | Listener A ok, B throws, then retry | A runs twice but its effect happens once (inbox). B eventually succeeds. |
| Boot race: marketplace channel | Two app contexts (or two threads calling the handler) against an empty schema | Exactly one row. Neither "pod" fails. Both report satisfied. |
| Boot race: manifest | Two publishers, same revision | One `ENQUEUED`, the rest `NOT_DUE`. One outbox row. |
| Rolling deploy | Publisher rev N+1, then rev N | Rev N gets `SUPERSEDED`. Identity state stays at N+1. |
| Digest conflict | Same revision, different content | Bootstrap fails, readiness DOWN, metric emitted. |
| Shutdown release | Stop relay with queued cold items | Rows return to FAILED with `availableAt=now`, not stuck in PROCESSING. |
| ShedLock | Two schedulers, same job | One execution per `lockAtLeastFor` window. |
| Architecture | ArchUnit | Every `@Scheduled` is classified. No `@Async` on integration-event listeners. No `ObjectInputStream`. |

---

## 6. Rollout & Migration Order

1. **Phase 1.1–1.3** (SKIP LOCKED + conditional updates). Low risk and independent; ship first.
2. **Phase 3.1–3.4** (ShedLock + pools). Required before running more than one replica.
3. **Phase 1.4 + 2.x** (event_id + inbox). Steps:
   1. Ship the column with a backfill.
   2. Have producers populate it.
   3. Have consumers start recording it.
   4. Remove `@Async` from the identity listeners last, once the inbox is in place.
4. **Phase 4** (bootstrap framework). Deploy with readiness gating behind a flag (`bootstrap.gate-readiness=false` at first), observe the metrics, then enable it.
5. **Phase 1.5–1.9, 5.** Hardening and observability.
6. **Broker phase (future).** Replace `ApplicationEventPublisher::publishEvent` in the module `OutboxEventDispatcher` beans with a broker producer. The inbox, `event_id`, versioning and ShedLock carry over unchanged. Consumer groups then remove P4.

Each Flyway change follows expand/contract. For example, add the nullable `event_id`, then backfill, then add `NOT NULL` + `UNIQUE` in a later release, so old and new pods coexist during rollout.

---

## 7. Acceptance Criteria

- [ ] With 3 replicas under load, every outbox row reaches PUBLISHED or DEAD. No row stays in PROCESSING beyond the claim timeout. `outbox.claim.lost` stays at ~0.
- [ ] Killing a pod mid-dispatch causes **no lost events** and no duplicate side effects (verified through inbox duplicates).
- [ ] First deploy of 3 replicas on an empty database: all become ready, there is exactly one marketplace channel, one manifest outbox row per module, and no crash loops.
- [ ] A rolling deploy with a manifest revision bump: no 403 spike. Old pods log `SUPERSEDED`. Identity converges to the new revision within one publish cycle.
- [ ] Each class B job runs once per interval cluster-wide (verified with the `scheduler.job.*` metrics).
- [ ] The ArchUnit job-classification and no-async-integration-listener rules pass.
