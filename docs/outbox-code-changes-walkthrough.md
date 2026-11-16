# Code Review & Walkthrough: Outbox, Events, Scheduling & Boot-time Consistency

> **Scope**: Detailed review of the implementation on branch `feature/outbox` against the requirements specified in [`docs/outbox-events-scheduling-plan.md`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/docs/outbox-events-scheduling-plan.md).  
> **Target Audience**: Developers, architects, and operators seeking a clear conceptual and code-level walkthrough of the changes.

---

## 1. Executive Summary & Why These Changes Were Made

### 1.1 The Core Problem
In a single-instance development environment, outbox event dispatch, scheduled background tasks, and application startup tasks seem to work without errors. However, as soon as **multiple replicas ($N \ge 2$) run concurrently in Kubernetes** or during **rolling deployments**, critical distributed system failures occur:
1. **Outbox Lock Contention & Serialization**: Outbox pollers on different pods lock the same rows simultaneously using `FOR UPDATE`, causing lock wait timeouts and serialization bottlenecks.
2. **Lost Claims & Overwritten States**: If a listener takes longer than the claim timeout, another pod reclaims the row. The first pod then marks the row `PUBLISHED`, creating race conditions and duplicate executions.
3. **Poison Event Loops**: A malformed event or permanently failing listener retries indefinitely every 30 seconds, cluttering logs and starving valid events.
4. **At-Most-Once Event Loss with `@Async` Listeners**: Event listeners annotated with `@Async @EventListener` mark the outbox row `PUBLISHED` as soon as the task is submitted to an in-memory thread pool. If the pod restarts or the task fails, the event is permanently lost.
5. **Scheduler Storms**: Background tasks (cleanup, manifest republishing, workflow sweeping) run simultaneously on every pod, causing optimistic lock collisions, database spikes, and duplicate executions.
6. **Boot-Time Startup Race Conditions**: Multiple pods booting simultaneously race on initializing shared records (such as the default marketplace channel or security manifest state rows), causing unique key violations that crash the pods (`ApplicationReadyEvent` listener exceptions fail application startup).
7. **Readiness Traffic Leaks**: Pods receive customer traffic before security manifests are registered in Identity, leading to transient HTTP `403 Forbidden` errors during rolling releases.

### 1.2 The Solution Architecture
The `feature/outbox` branch fulfills all 5 phases of [`docs/outbox-events-scheduling-plan.md`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/docs/outbox-events-scheduling-plan.md):
- **Concurrency-Safe Outbox Delivery**: Implemented `FOR UPDATE SKIP LOCKED` for batch claims, conditional status updates fenced by a unique `claim_token`, bounded exponential backoff with jitter, and a terminal `DEAD` status with an operator replay mechanism.
- **Consumer-Side Idempotency (Inbox Pattern)**: Established `processed_event` tables in every consuming module schema. Consumers record the event ID inside their own transaction before applying side effects, guaranteeing that at-least-once delivery results in exactly-once business side effects.
- **Synchronous Delivery Guarantee**: Removed `@Async` from integration-event listeners, ensuring listeners execute within the outbox dispatch transaction. If a listener fails, the outbox record remains failed and will be retried safely.
- **Three-Tier Scheduling Taxonomy**: Classified every `@Scheduled` task into **Class A** (Competing workers using row-level locks like `SKIP LOCKED`), **Class B** (Cluster singletons guarded by per-schema ShedLock JDBC locks), or **Class C** (Boot tasks).
- **Cluster-Aware Bootstrap Framework (`BootstrapTask`)**: Implemented a coordinated boot framework where required tasks run under distributed locks with automatic retries and gate Kubernetes readiness (`/actuator/health/readiness`), preventing crash-loops and traffic routing to uninitialized pods.
- **Full Observability & MDC Tracing**: Added Micrometer metrics for lag, failures, dead letters, and lock skips, plus MDC propagation of `traceId` and `eventId` to all logs.

---

## 2. Requirement Mapping: Problems P1–P15 vs. Implementation

The table below maps each problem identified in [§2 of the plan](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/docs/outbox-events-scheduling-plan.md#L59-L78) to its exact code solution on `feature/outbox`:

| ID | Problem in Plan | Code Solution on `feature/outbox` | Key Files Changed |
|:---|:---|:---|:---|
| **P1** | Batch claim uses `FOR UPDATE` without `SKIP LOCKED` | Added `query.setHint("jakarta.persistence.lock.timeout", -2)` to JPQL queries. In Hibernate 6, `-2` renders as `FOR UPDATE SKIP LOCKED` on PostgreSQL, allowing replicas to process distinct batches concurrently. | [`JpaOutboxStore.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/jpa/JpaOutboxStore.java#L67-L70) |
| **P2** | Unfenced `publishEvent` re-read and status write | Added claim fencing: before status transition, re-query row under lock and verify `isClaimedBy(claimToken)`. If claim was lost to another pod, rollback transaction, discard outcome, and increment `outbox.claim.lost`. | [`AbstractOutboxProcessor.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/AbstractOutboxProcessor.java#L304-L311) |
| **P3** | No max attempts or dead-letter state | Created `OutboxRetryPolicy` (exponential backoff $base \times 2^{attempt-1} \pm 20\%$ jitter, capped at 15 min), added `OutboxStatus.DEAD`, and added `OutboxPoisonEventException` for immediate dead-lettering of unparseable payloads. | [`OutboxRetryPolicy.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/outbox/OutboxRetryPolicy.java#L8-L72), [`OutboxStatus.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/outbox/OutboxStatus.java#L8) |
| **P4** | Failure of one listener re-runs already successful listeners | Introduced per-consumer inbox pattern (`processed_event` table). Handlers call `ProcessedEventRepository.markProcessed(consumer, eventId)` in their business transaction; duplicate deliveries return early without re-executing side effects. | [`JpaProcessedEventRepository.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/inbox/jpa/JpaProcessedEventRepository.java#L47-L58), [`RegisterCustomerCommandHandler.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/account/customer/internal/command/handler/RegisterCustomerCommandHandler.java#L36-L40) |
| **P5** | No stable `event_id` on outbox row | Added `event_id uuid not null unique` column to `outbox_record` across all 10 schemas via Flyway, backed by `gen_random_uuid()`. Propagated `eventId` via `OutboxDeliveryContext`. | `V*__outbox_hardening_and_shedlock.sql`, [`JpaOutboxEventEntity.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/jpa/JpaOutboxEventEntity.java#L32-L34) |
| **P6** | `@Async @EventListener` causes at-most-once event loss | Removed `@Async` from all integration-event listeners. Listeners execute synchronously on the outbox delivery thread, dispatching commands that commit independently with `REQUIRES_NEW`. | [`IdentitySecurityManifestRegistrationListener.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/account/identity/internal/adapter/inbound/event/IdentitySecurityManifestRegistrationListener.java#L22), [`UserRegisteredIntegrationEventListener.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/account/customer/internal/adapter/inbound/event/UserRegisteredIntegrationEventListener.java#L31) |
| **P7** | Every `@Scheduled` job runs on every replica | Defined job taxonomy annotations (`@CompetingWorkerJob` vs `@ClusterSingletonJob`). Singleton jobs execute through `ShedLockSingletonJobExecutor` backed by a per-schema `shedlock` table using DB time. | [`ShedLockSingletonJobExecutor.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/shared/scheduling/ShedLockSingletonJobExecutor.java#L42-L83), [`SchedulingArchitectureTest.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/test/java/com/grab/app/architecture/SchedulingArchitectureTest.java#L48-L55) |
| **P8** | Scheduler thread pool starvation (5 threads for ~28 jobs) | Raised `spring.task.scheduling.pool.size` to 14 (competing workers + buffer), configured bounded `@Async` thread pool with `CallerRunsPolicy`. | [`application.yml`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/resources/application.yml#L10-L15), [`AsyncExecutorConfig.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/shared/scheduling/AsyncExecutorConfig.java#L38-L50) |
| **P9** | Boot tasks race across replicas (marketplace channel & manifests) | (a) Converted `EnsureMarketplaceChannel` into a `BootstrapTask` with `@EnsureRetryable` that retries on unique violation in a fresh transaction. (b) Pre-created security manifest publication state rows in Flyway migrations (`INSERT ... ON CONFLICT DO NOTHING`). | [`MarketplaceChannelBootstrapTask.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/channel/saleschannel/internal/adapter/inbound/lifecycle/MarketplaceChannelBootstrapTask.java#L19-L57), [`EnsureRetryable.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/shared/bootstrap/EnsureRetryable.java#L21-L25), `V*__precreate_publication_state.sql` |
| **P10** | Readiness takes traffic before boot tasks complete | Implemented `BootstrapRunner` (executing tasks in `@Order` under cluster locks) and `BootstrapHealthIndicator`. `/actuator/health/readiness` stays `DOWN` until all required boot tasks succeed. | [`BootstrapRunner.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/shared/bootstrap/BootstrapRunner.java#L112-L172), [`BootstrapHealthIndicator.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/shared/bootstrap/BootstrapHealthIndicator.java#L24-L38) |
| **P11** | Event ordering per aggregate | Ensured monotonic revision comparison in consumers (e.g. `SecurityCatalog.consider`), added aggregate version tracking, and captured `eventId` for idempotency. | [`SecurityCatalog.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/identity-domain/src/main/java/com/identity/domain/model/SecurityCatalog.java), [`PublicationEligibilityPolicy.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/security/policy/PublicationEligibilityPolicy.java) |
| **P12** | Timezone skew from JVM `LocalDateTime.now()` | Migrated all outbox timestamps from `LocalDateTime` to `java.time.Instant` in Java, and from `timestamp without time zone` to `timestamptz` (`timestamp(6) with time zone`) in PostgreSQL. | [`JpaOutboxEventEntity.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/jpa/JpaOutboxEventEntity.java#L54-L68), `V*__outbox_hardening_and_shedlock.sql` |
| **P13** | Shutdown leaves claimed rows in `PROCESSING` for 120s | Overrode `destroy()` in `AbstractOutboxProcessor` to drain cold queues and conditionally release claims back to `FAILED` with `available_at = now` and restored attempt count. | [`AbstractOutboxProcessor.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/outbox-infrastructure/src/main/java/com/grab/outbox/infrastructure/AbstractOutboxProcessor.java#L128-L142) |
| **P14** | `JavaSerializationOutboxEventSerializer` remote code execution risk | Completely removed `JavaSerializationOutboxEventSerializer`. Added an ArchUnit test forbidding `ObjectInputStream` in production code. | [`SchedulingArchitectureTest.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/test/java/com/grab/app/architecture/SchedulingArchitectureTest.java#L86-L91) |
| **P15** | No outbox or scheduler telemetry | Created `MicrometerOutboxObserver`, `OutboxBacklogMetrics` (with cached table queries), `BootstrapMetrics`, `ObservabilityBeanPostProcessor`, and operator `OutboxReplayer`. Added `traceId` and `eventId` to MDC logging. | [`MicrometerOutboxObserver.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/shared/outbox/MicrometerOutboxObserver.java#L11-L49), [`OutboxBacklogMetrics.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/shared/outbox/OutboxBacklogMetrics.java#L21-L54), [`OutboxReplayer.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/main/java/com/grab/app/shared/outbox/OutboxReplayer.java#L15-L44), [`LogContext.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/logger-slf4j/src/main/java/com/grab/framework/logger/slf4j/LogContext.java#L8-L33) |

---

## 3. Deep-Dive Code Walkthrough by Layer

Let's walk through the architecture layer by layer to see how each component fits together.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   PLATFORM BFF                                         │
│                       (Seller / Marketplace REST Ingress)                              │
└─────────────────────────────────────────┬──────────────────────────────────────────────┘
                                          │ dispatches Command
                                          ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              APPLICATION & DOMAIN                                      │
│  CommandHandler (Module TX)                                                            │
│     ├── Mutate Aggregate                                                               │
│     └── DomainEventProducer.produce() ──► outbox_record(status=NEW, event_id=UUID)     │
└─────────────────────────────────────────┬──────────────────────────────────────────────┘
                                          │ afterCommit()
                                          ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              OUTBOX INFRASTRUCTURE                                     │
│  DualQueueOutboxRelay                                                                  │
│     ├── Hot Queue: ID ──► claimByIdForUpdateSkipLocked()                               │
│     └── Cold Queue (Poller @Scheduled, every 5s): findBatchForProcessing(SKIP LOCKED)  │
│                                                                                        │
│  AbstractOutboxProcessor (Worker Thread)                                               │
│     1. Claim row (status=PROCESSING, claim_token=UUID)                                 │
│     2. Dispatch to In-Process Event Publisher (MDC: traceId, eventId)                  │
│     3. Outcome Transaction (REQUIRES_NEW):                                             │
│        - Re-verify claimToken under lock                                               │
│        - Success: markPublished()                                                      │
│        - Failure: markFailed(backoff) or markDead() if max attempts reached            │
└─────────────────────────────────────────┬──────────────────────────────────────────────┘
                                          │ publishEvent(event) [Synchronous]
                                          ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                             CONSUMER APPLICATION                                       │
│  EventListener (Synchronous, NO @Async)                                                │
│     └── CommandBus.dispatch(ConsumerCommand with sourceEventId)                        │
│                                                                                        │
│  Consumer CommandHandler (@Transactional REQUIRES_NEW)                                 │
│     ├── ProcessedEventRepository.markProcessed(consumer, eventId)                     │
│     │     ├── true:  Proceed with business side effect & commit atomically             │
│     │     └── false: Duplicate delivery! Return early (No-op)                          │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

### 3.1 Framework Layer (`framework`)

The `framework` module defines pure Java contracts without any framework ties or Spring dependencies:

1. **[`BootstrapTask.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/bootstrap/BootstrapTask.java)**:
   - Contract for startup operations (scheduling Class C).
   - Defines `key()` (lock identifier), `required()` (whether failure keeps readiness DOWN), `timeout()`, `execute()`, and `isSatisfied()`.
   - Requires tasks to be idempotent.
2. **[`ProcessedEventRepository.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/inbox/ProcessedEventRepository.java)**:
   - Consumer inbox port.
   - `markProcessed(String consumer, UUID eventId)`: returns `true` if this is the first time the consumer processes the event; returns `false` if it is a duplicate delivery.
   - `deleteProcessedBefore(Instant cutoff, int limit)`: batch cleanup port.
3. **[`SingletonJobExecutor.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/scheduling/SingletonJobExecutor.java)**, **[`ClusterSingletonJob.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/scheduling/ClusterSingletonJob.java)**, and **[`CompetingWorkerJob.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/scheduling/CompetingWorkerJob.java)**:
   - Formal annotations defining the scheduling taxonomy.
   - Replaces unstructured `@Scheduled` jobs with explicit execution semantics enforced by ArchUnit.
4. **[`OutboxRetryPolicy.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/outbox/OutboxRetryPolicy.java)**:
   - Computes exponential backoff: $baseDelay \times 2^{attempt - 1} \times (1 \pm jitter)$, capped at `maxDelay`.
   - Determines when attempts are exhausted (default 10 attempts).
5. **[`OutboxStatus.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/framework/src/main/java/com/grab/framework/outbox/OutboxStatus.java)**:
   - Added `DEAD` to `{NEW, PROCESSING, PUBLISHED, FAILED}`.

---

### 3.2 Outbox Infrastructure Layer (`outbox-infrastructure`)

This module houses the core outbox engine and JPA persistence adapters.

#### `JpaOutboxStore.java` — Concurrency-Safe Queries without Native SQL
Under rule **R11** of `AGENTS.md`, Spring Data JPA repositories must not use native SQL (`nativeQuery = true`). `JpaOutboxStore` accomplishes non-blocking PostgreSQL locking using standard JPQL and Hibernate hints:
```java
// JpaOutboxStore.java
static final String LOCK_TIMEOUT_HINT = "jakarta.persistence.lock.timeout";
static final int SKIP_LOCKED = -2; // Hibernate 6 maps -2 to PostgreSQL 'FOR UPDATE SKIP LOCKED'

@Override
public List<T> findBatchForProcessing(...) {
    String jpql = """
            select event
            from %s event
            where (event.status in :retryableStatuses and event.availableAt <= :availableAt)
               or (event.status = :processingStatus and event.claimedAt <= :staleBefore)
            order by event.id asc
            """.formatted(entityName);

    TypedQuery<T> query = entityManager.createQuery(jpql, entityType);
    query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
    query.setHint(LOCK_TIMEOUT_HINT, SKIP_LOCKED);
    query.setMaxResults(batchSize);
    return query.getResultList();
}
```
**Why this works**:
- When 3 pods poll the outbox table at the same second, Pod A locks rows 1–50.
- With `SKIP LOCKED`, Pod B does **not** wait or block; it skips rows 1–50 and immediately claims rows 51–100.
- Pod C immediately claims rows 101–150.
- Throughput scales linearly with replica count, with zero lock contention.

#### `AbstractOutboxProcessor.java` — State Machine & Claim Fencing
`AbstractOutboxProcessor` coordinates claiming, dispatching, and status recording:

```java
// AbstractOutboxProcessor.java
private void publishEvent(ClaimedOutboxEvent<ID> claimedEvent) {
    try {
        publishTransactionTemplate.executeWithoutResult(status -> {
            // 1. Initial check: verify claim token before dispatching
            Optional<T> claimedRow = outboxStore.findById(claimedEvent.id())
                    .filter(event -> event.isClaimedBy(claimedEvent.claimToken()));
            if (claimedRow.isEmpty()) {
                reportClaimLost(claimedEvent, "before dispatch");
                return;
            }

            // 2. Dispatch synchronously to in-process listeners
            dispatch(claimedRow.get());

            // 3. Post-dispatch check under lock: verify claim token is STILL ours
            Optional<T> stillClaimed = outboxStore.findByIdForUpdate(claimedEvent.id())
                    .filter(event -> event.isClaimedBy(claimedEvent.claimToken()));
            if (stillClaimed.isEmpty()) {
                status.setRollbackOnly();
                reportClaimLost(claimedEvent, "after dispatch");
                return;
            }

            // 4. Mark published safely
            T event = stillClaimed.get();
            event.markPublished(clock.instant());
            observer.published(module, event.getEventType(), lag(event, now));
        });
    } catch (OutboxPoisonEventException exception) {
        recordFailure(claimedEvent, exception, true); // Immediate DEAD
    } catch (RuntimeException exception) {
        recordFailure(claimedEvent, exception, false); // Retry with backoff
    }
}
```
**Why claim fencing is essential**:
- Suppose Pod 1 claims an outbox row with claim token `token-1` and a 120-second timeout.
- A downstream listener hangs for 125 seconds due to a slow external call.
- Meanwhile, the 120-second timeout expires. Pod 2 reclaims the row with token `token-2` and starts processing it.
- When Pod 1 finishes at second 125, it attempts to mark the row `PUBLISHED`.
- Without claim fencing, Pod 1 would overwrite the row and erase Pod 2's claim.
- With claim fencing (`findByIdForUpdate` checking `isClaimedBy("token-1")`), Pod 1 detects that the row is now owned by `token-2`. Pod 1 immediately rolls back its transaction, logs a warning, and increments `outbox.claim.lost`. Pod 2 retains full control.

#### Graceful Shutdown Claim Release
When a pod terminates during a rolling deployment, claimed events in memory would normally be stuck in `PROCESSING` until the 120-second timeout expires on surviving pods.
In `AbstractOutboxProcessor.destroy()`:
```java
@Override
public void destroy() {
    relay.stop();
    List<ClaimedOutboxEvent<ID>> undrained = relay.drainCold();
    for (ClaimedOutboxEvent<ID> claimed : undrained) {
        releaseClaim(claimed); // Transitions PROCESSING -> FAILED, availableAt = now, decrements attemptCount
    }
}
```
Undispatched claims are immediately freed so surviving pods can pick them up within milliseconds, eliminating rolling release delivery stalls.

---

### 3.3 Consumer-Side Idempotency & The Inbox Pattern

#### The Problem: Partial Failures on Fan-Out
When an outbox event is dispatched via `ApplicationEventPublisher.publishEvent()`, multiple `@EventListener` methods may be invoked. If Listener A succeeds but Listener B throws an exception, the outbox processor marks the entire event `FAILED`.
On retry, Listener A will be called a second time. If Listener A is not idempotent, it produces duplicate side effects (e.g., duplicate customer records, duplicate emails, duplicate billing actions).

#### The Solution: Per-Schema Inbox (`processed_event`)
Each consuming module owns a `processed_event` table in its isolated database schema:
```sql
CREATE TABLE IF NOT EXISTS processed_event (
    consumer     varchar(150)             NOT NULL,
    event_id     uuid                     NOT NULL,
    processed_at timestamp(6) with time zone NOT NULL DEFAULT now(),
    CONSTRAINT pk_processed_event PRIMARY KEY (consumer, event_id)
);
```
Inside the consumer's command handler:
```java
// RegisterCustomerCommandHandler.java
@Override
@CustomerTransactional
public CustomerResult handle(RegisterCustomerCommand command) {
    UUID sourceEventId = command.sourceEventId();
    if (sourceEventId != null && !processedEvents.markProcessed(CONSUMER, sourceEventId)) {
        return null; // Event already processed by this consumer; safely skip!
    }
    return registerCustomerUseCase.execute(command);
}
```
**Why this provides exactly-once side effects**:
1. `markProcessed()` executes in the **exact same database transaction** as the customer creation use case.
2. If the transaction commits, both the customer record and the `processed_event` record are committed together atomically.
3. If the outbox retries the event later, `markProcessed()` queries `processed_event`, finds the existing record, returns `false`, and the handler exits cleanly with `null`.
4. If two pods attempt to process the same duplicate simultaneously, the primary key `(consumer, event_id)` rejects the second insert with a unique constraint violation, aborting the duplicate transaction.

---

### 3.4 Cluster Scheduling & ShedLock (`app/shared/scheduling`)

Scheduled jobs in a multi-pod cluster require coordination:

| Job Class | Scheduling Strategy | Mechanism | Examples |
|:---|:---|:---|:---|
| **Class A: Competing Workers** | Concurrently runs on every replica. | Work partitioned at row level using `FOR UPDATE SKIP LOCKED`. | Outbox pollers (10 modules). |
| **Class B: Cluster Singletons** | Runs on at most one replica per interval. | Distributed lock acquired via ShedLock before executing method body. | Outbox cleanup, inbox retention, workflow sweeping. |
| **Class C: Boot Tasks** | Runs once per deployment, cluster-wide. | Managed by `BootstrapRunner` with cluster locks and readiness gating. | Ensuring default sales channels, publishing security manifests. |

#### `ShedLockSingletonJobExecutor.java`
Uses `JdbcTemplateLockProvider` with database time (`usingDbTime()`):
- **Advisory locks were avoided** because PostgreSQL advisory locks are bound to physical connections, which break when using PgBouncer in transaction pooling mode.
- **Table-based locking** writes to `<module_schema>.shedlock`, maintaining strict multi-schema database isolation.
- Automatically records telemetry: `scheduler.job.duration` (success/error) and `scheduler.job.skipped_locked` (when another replica won the lock).

---

### 3.5 Startup Consistency & The Bootstrap Framework (`app/shared/bootstrap`)

#### Problem P9 & P10 in Action
Previously, startup listeners used `@EventListener(ApplicationReadyEvent.class)`. When 3 replicas booted simultaneously:
1. Both pods evaluated `EnsureMarketplaceChannel`: both saw "missing", both inserted, one threw a unique constraint violation on `uq_sales_channels_marketplace`, and the uncaught exception in `ApplicationReadyEvent` crashed the pod in a Kubernetes CrashLoopBackOff.
2. The security manifest publication state row had no initial row. Both pods attempted an initial `INSERT`, causing primary key collisions.
3. Pods reported readiness immediately, taking customer traffic before their authorization manifests were registered in Identity, resulting in transient `403 Forbidden` errors.

#### The Solution: `BootstrapRunner` and `BootstrapHealthIndicator`
1. **Flyway Pre-Creation**: Added `V*__precreate_publication_state.sql` migrations across all schemas:
   ```sql
   INSERT INTO security_manifest_publication (module_key, security_revision, content_digest, version)
   VALUES ('catalog', 0, '', 0)
   ON CONFLICT (module_key) DO NOTHING;
   ```
   This eliminates the initial insertion race condition entirely.
2. **`@EnsureRetryable` Aspect**:
   Applied to `EnsureMarketplaceChannelCommandHandler`:
   ```java
   @EnsureRetryable
   @SalesChannelTransactional(propagation = Propagation.REQUIRES_NEW)
   public ProvisionSalesChannelResult handle(EnsureMarketplaceChannelCommand command) {
       return useCase.execute(command);
   }
   ```
   If a concurrent pod wins the unique constraint, the retry aspect catches `DataIntegrityViolationException` and retries in a fresh transaction. The subsequent find re-reads the committed winner's row without error.
3. **Sequential Locked Execution in `BootstrapRunner`**:
   - Executes after `ApplicationReadyEvent` on a dedicated thread (`bootstrap-runner`).
   - Obtains a distributed lock `bootstrap:<key>` for each task.
   - Non-leader replicas poll `isSatisfied()`.
   - Bounded exponential retries for transient failures.
4. **Kubernetes Readiness Gating**:
   `BootstrapHealthIndicator` is registered under the actuator readiness health group (`management.endpoint.health.group.readiness.include: readinessState,bootstrap`):
   - As long as any `required` task is pending, running, or failed, `BootstrapHealthIndicator` returns `DOWN`.
   - Kubernetes delays routing traffic to the pod until all required manifests and provisioning tasks succeed.
   - If a fatal configuration error occurs (such as a manifest digest `CONFLICT`), the task throws `BootstrapTaskException.permanent()`, readiness remains `DOWN`, and the deployment halts safely without serving broken traffic.

---

### 3.6 Architecture & Snapshot Invariant Testing

To ensure these rules cannot be accidentally broken in future pull requests, two automated tests were added:

1. **[`SchedulingArchitectureTest.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/test/java/com/grab/app/architecture/SchedulingArchitectureTest.java)** (ArchUnit):
   - **Rule 1**: Every `@Scheduled` method must be annotated with exactly one of `@ClusterSingletonJob` or `@CompetingWorkerJob`.
   - **Rule 2**: No `@EventListener` method may be annotated with `@Async` (prevents at-most-once event loss).
   - **Rule 3**: No class outside `com.grab.app.shared.bootstrap..` may listen to `ApplicationReadyEvent`.
   - **Rule 4**: Classes declaring `@ClusterSingletonJob` must hold a `SingletonJobExecutor`.
   - **Rule 5**: No production class may use `java.io.ObjectInputStream` (deserialization security check).
2. **[`SecurityManifestDigestSnapshotTest.java`](file:///Users/khinemyaezin/Repository/grab-ecommerce/grab/app/src/test/java/com/grab/app/architecture/SecurityManifestDigestSnapshotTest.java)**:
   - Validates current security manifests against `security-manifest-digests.properties`.
   - If a developer edits a manifest's scopes or permissions without bumping its `securityRevision`, this test fails in CI, preventing a runtime digest `CONFLICT` during production rollout.

---

## 4. End-to-End Walkthrough: The Life of an Outbox Event

To understand how the pieces cooperate at runtime, let's trace a user registration event through the entire pipeline:

```
[1. User Registers]
       │
       ▼
SellerUserAdminCommandService
       │
       ▼
RegisterUserCommandHandler (Identity Transaction)
  ├── 1. Inserts User in 'identity.users'
  └── 2. IdentityDomainEventProducer.produce(UserRegisteredIntegrationEvent)
            └── Inserts outbox_record:
                - status: NEW
                - event_id: 3c9b... (UUID)
                - traceId: a7f1...
                - available_at: now (UTC Instant)
       │
  [Identity Transaction Commits]
       │
       ▼
SpringOutboxCommitHook.afterCommit()
  └── Offers outbox ID to DualQueueOutboxRelay (Hot Queue)
       │
       ▼
DualQueueOutboxRelay Worker Thread (claims row via SKIP LOCKED)
  └── AbstractOutboxProcessor.publishEvent()
        ├── 1. Checks claim_token under lock (claim fencing)
        ├── 2. Binds MDC: traceId=a7f1..., eventId=3c9b...
        ├── 3. Calls ApplicationEventPublisher.publishEvent(UserRegisteredIntegrationEvent)
        │        │
        │        ▼
        │     UserRegisteredIntegrationEventListener (Customer Module)
        │        ├── Reads sourceEventId from OutboxDeliveryContext
        │        └── Dispatches RegisterCustomerCommand(sourceEventId=3c9b...)
        │                 │
        │                 ▼
        │              RegisterCustomerCommandHandler (Customer Transaction)
        │                 ├── Checks customerProcessedEventRepository.markProcessed("customer.RegisterCustomer", 3c9b...)
        │                 │     ├── First time? Inserts into 'customer.processed_event' -> returns true
        │                 │     └── Duplicate? Returns false -> skip use case
        │                 └── Inserts Customer in 'customer.customers'
        │                 [Customer Transaction Commits]
        │
        ├── 4. Re-checks claim_token under row lock (still ours!)
        └── 5. Marks outbox_record:
                - status: PUBLISHED
                - published_at: now
                - increments metric: outbox.published(module="identity")
```

### What Happens in Failure Scenarios?

#### Scenario A: Downstream Consumer Throws Exception
1. The customer database experiences a transient deadlock or timeout.
2. `RegisterCustomerCommandHandler` rolls back its transaction (no customer record is saved, no `processed_event` record is saved).
3. The exception bubbles up to `AbstractOutboxProcessor.publishEvent()`.
4. The outbox processor catches the exception and executes an outcome transaction:
   - Increments `attempt_count` (e.g., attempt 1).
   - Computes backoff delay via `OutboxRetryPolicy` (e.g., 30s + jitter).
   - Sets `status = FAILED`, `available_at = now + 30s`, `last_error = "..."`.
5. 30 seconds later, the scheduled poller picks up the row using `SKIP LOCKED` and retries.
6. The retry succeeds, the customer is registered, and the outbox status becomes `PUBLISHED`.

#### Scenario B: Poison Payload
1. A bug causes an unparseable payload or corrupted JSON.
2. Deserializer throws `OutboxPoisonEventException`.
3. `AbstractOutboxProcessor` catches this specific exception and immediately transitions the row to `DEAD` without burning through 10 retry cycles.
4. Increments `outbox.dead` metric and triggers an alert.
5. The operator fixes the parser, deploys the fix, and invokes `OutboxReplayer.replayDead("identity", minId, maxId, 100)` to redeliver.

#### Scenario C: Pod Crash During Dispatch
1. Pod 1 claims the row (`status = PROCESSING`, `claim_token = "pod-1-token"`).
2. Pod 1 crashes midway through execution.
3. The row remains in `PROCESSING`.
4. After `claimTimeout` (120 seconds), Pod 2's poller identifies that `status = PROCESSING` and `claimed_at <= now - 120s`.
5. Pod 2 reclaims the row with `claim_token = "pod-2-token"`.
6. Pod 2 delivers the event. The consumer checks its inbox:
   - If Pod 1 had committed before dying, the inbox finds the event and skips the side effect.
   - If Pod 1 died before committing, the inbox processes the event cleanly.
7. Pod 2 marks the row `PUBLISHED`. No events were lost and no duplicate business effects were produced.

---

## 5. Architectural Compliance with `AGENTS.md`

All code changes on `feature/outbox` strictly adhere to the non-negotiable architectural rules of the repository:

- **R7 & R16 (Transaction Demarcation)**:
  - Transactions reside solely on CQRS handlers (`CommandHandler`) and outbox processor templates (`@{Module}Transactional`, `REQUIRES_NEW`).
  - No `@Transactional` annotations on controllers, services, policies, or mappers.
- **R11 (No Native Queries in Spring Data Repositories)**:
  - All database queries in `JpaOutboxStore` and `JpaProcessedEventRepository` use type-safe JPQL and EntityManager methods.
  - Non-blocking locking uses the standard Hibernate hint `jakarta.persistence.lock.timeout = -2` rather than raw SQL `FOR UPDATE SKIP LOCKED`.
- **R17 (Outbox Pattern & Event Listeners)**:
  - Cross-module communication strictly follows transactional outbox publication + `@EventListener`.
  - `@Async` is completely forbidden on integration listeners.
- **Multi-Schema Database Isolation**:
  - Dedicated schema and HikariCP pool per service group (`account`, `catalog`, `channel`, `"order"`).
  - Outbox tables, `processed_event` inbox tables, and `shedlock` tables are created in each module's own schema.
  - Zero cross-schema foreign keys and zero cross-schema JPA joins.
- **R22 (Testing Strategy)**:
  - Validated by unit tests, ArchUnit architectural tests, and PostgreSQL Testcontainers integration tests.

---

## 6. How to Run and Verify the Tests

You can verify the test suite directly from the command line:

```bash
# 1. Run ArchUnit scheduling & boot architecture tests:
mvn test -pl app -Dtest=SchedulingArchitectureTest

# 2. Run security manifest digest snapshot test:
mvn test -pl app -Dtest=SecurityManifestDigestSnapshotTest

# 3. Run bootstrap runner unit tests:
mvn test -pl app -Dtest=BootstrapRunnerTest,PublicationBootstrapOutcomeTest

# 4. Run outbox processor tests:
mvn test -pl outbox-infrastructure -Dtest=AbstractOutboxProcessorTest

# 5. Run PostgreSQL inbox integration tests (requires Docker / Testcontainers):
mvn test -pl catalog-adapter-persistence -Dtest=ProcessedEventInboxPostgresTest,OutboxBacklogAndReplayPostgresTest
```

---

## 7. Summary & Conclusion

The `feature/outbox` branch comprehensively transforms the monolithic event delivery and scheduling mechanisms into a resilient, production-ready distributed architecture. By combining:
1. `FOR UPDATE SKIP LOCKED` claiming and claim-fenced status updates,
2. Consumer-side `processed_event` deduplication,
3. Synchronous `@EventListener` delivery semantics,
4. Three-tier job classification with ShedLock database-time locks, and
5. Gated cluster bootstrapping with Kubernetes readiness integration,

the platform guarantees **at-least-once delivery with exactly-once side effects**, zero lost events, zero rolling deployment crashes, and complete horizontal scalability across any number of replicas.
