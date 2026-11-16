# Plan: Outbox Refactoring — SOLID, Module-Owned, Microservice-Ready

| | |
|---|---|
| **Date** | 2026-11-14 |
| **Status** | Proposed — revision 4 (supersedes revisions 1–3; adds the hexagonal layout of the outbox subsystem, §3.9) |
| **Parent docs** | [`outbox-events-scheduling-plan.md`](outbox-events-scheduling-plan.md), [`outbox-scheduler-architecture-review.md`](outbox-scheduler-architecture-review.md), [`db-agnostic-locking-plan.md`](db-agnostic-locking-plan.md) |
| **Goal** | Make the outbox and its scheduling a set of SOLID library components that each module composes for itself, so any module (catalog, inventory, …) can be extracted into its own service by taking its own code plus versioned libraries — and nothing from `app/shared`. |
| **Rules honoured** | AGENTS.md R1, R5 (no cross-module runtime coupling), R11, R16, R17, R22. |

---

## 1. Guiding principle: share code, not runtime

A module that becomes a microservice can take **libraries** with it (versioned Maven artifacts), but not **beans that another module also uses**. Today the outbox mixes the two.

| Kind | Allowed? | Example |
|---|---|---|
| Library class, instantiated per module | Yes | `OutboxDeliveryEngine`, `ShedLockSingletonJobExecutor`, `SkipLockedClaimStrategy` |
| Library class that discovers per-module beans by type and holds no module names | Yes, sparingly (process-level, works the same with 1 or 10 modules) | Optional actuator endpoint over `OutboxModule` beans |
| Bean in `app/shared` that every module's outbox depends on | **No** | today: `outboxObserver`, `outboxReplayer`, `outboxBacklogMetrics`, `outboxClaimStrategyVerifier` |
| Code in `app/shared` that knows module names or aggregates module internals | **No** | today: `OutboxReplayer` keyed by module, `OutboxBacklogMetrics` over all processors |
| Process-level infrastructure unrelated to module boundaries | Yes, stays in `app` | tracing, bootstrap, `AsyncExecutorConfig` (each future service has its own copy) |

Test for every decision in this plan: *"If catalog moved to its own repository tomorrow, which files would it need to copy from outside `catalog`?"* The answer must be: none — only library dependencies.

---

## 2. SOLID review of the current code

Measured on `feature/outbox`.

| Principle | Finding | Evidence |
|---|---|---|
| **S** | `AbstractOutboxProcessor` (397 lines) owns delivery **plus** retention cleanup, operator replay, and backlog sampling, each with its own transaction template. | `cleanupPublishedEvents`, `deletePublishedInBatches`, `replayDead`, `backlog`, `hotQueueDropped` |
| **S** | Module configs (108–154 lines ×10) mix outbox wiring, inbox retention, lock-provider setup and scheduling. | `*ModuleOutboxConfig`, `WorkflowInfraConfig` |
| **O** | Good: claim strategy, lock provider, dispatcher and retry policy are already strategies. Bad: scheduling is closed — a new module needs a new subclass just to carry `@Scheduled` with its property placeholder. | 10 × `*OutboxEventProcessor` (50 lines, no logic) |
| **L** | Inheritance used for configuration, not behaviour: processor and producer subclasses add annotations or a log line. Substitutability of stores and claim strategies is only proven for catalog (contract tests live in `catalog-adapter-persistence`). | 10 producers (4 override `produce` only to log); `outbox/claim/*ClaimPostgresTest` |
| **I** | `OutboxStore` gives the domain-side producer claim, fencing, replay and retention operations it never uses; the processor's public surface (`module`, `backlog`, `hotQueueDropped`, `replayDead`) serves three different clients. | `OutboxStore` (9 methods), `JpaOutboxDomainEventProducer` uses `saveAll` + `flush` only |
| **D** | Mostly good (engine depends on `OutboxStore`, `OutboxEventDispatcher`, `SingletonJobExecutor` abstractions). Violations sit in the composition: global `app/shared` beans are injected into every module and know every module. | `OutboxObservabilityConfig`, `OutboxReplayer(List<AbstractOutboxProcessor>)` |

Microservice blockers found during the review:

1. `app/shared/outbox/*` and `app/shared/scheduling/*` are required by every module's outbox config.
2. `OutboxClaimStrategyVerifier` checks all stores from one global bean.
3. All module jobs share one scheduler pool (`spring.task.scheduling.pool.size: 14`), so a slow module starves others — the opposite of the per-module HikariCP bulkheads.
4. The claim/store contract tests can only be run from catalog.
5. *(Out of scope, noted)* integration event contracts live in `app/shared/events/<owner>` and the dispatcher is in-process `ApplicationEventPublisher`. Extraction also needs per-owner event contract artifacts and a broker dispatcher; this plan provides the seam (`OutboxEventDispatcher`) but not that migration.

---

## 3. Target design

### 3.1 Library layout

```
framework                     (contracts only — ports and value types, no Spring runtime)
  event/       DomainEventProducer                                           (exists; outbound port used by *RepositoryAdapter)
  outbox/      OutboxEntry, OutboxStatus, OutboxEventDispatcher, OutboxEventSerializer,
               OutboxRetryPolicy, OutboxRelay, ClaimedOutboxEvent            (exist)
  scheduling/  SingletonJob, SingletonJobExecutor                            (exist; ports only)

outbox-infrastructure         (one artifact, internally a hexagon — see §3.9)
  core/
    port/in/   DeliverOutboxEventsUseCase, OutboxAdministrationPort          (new)
    port/out/  OutboxWriter, OutboxClaimStore, OutboxMaintenanceStore,       (new, ISP)
               OutboxTransactions                                            (new)
               OutboxObserver                                                (exists, moved)
    service/   OutboxDeliveryEngine (final), OutboxOutcomeRecorder,
               OutboxRetentionCleaner, OutboxAdministrationService           (refactor / new)
  adapter/
    in/relay/        DualQueueOutboxRelay (drives DeliverOutboxEventsUseCase)(exists, moved)
    out/jpa/         JpaOutboxStore, JpaOutboxDomainEventProducer (final),
                     claim/ (OutboxClaimStrategy, skip-locked, conditional-update,
                     OutboxClaimStrategyCheck)                               (exist / moved from app/shared)
    out/spring/      SpringOutboxTransactions, InProcessOutboxEventDispatcher(new)
    out/micrometer/  MicrometerOutboxObserver, OutboxBacklogGauges           (moved from app/shared; micrometer optional)
  inbox/       ProcessedEventRetention, ObservedProcessedEventRepository     (exist / moved)
  module/      OutboxModule (+ Builder) — composition only                   (new)
  test-jar:    OutboxStoreContract, OutboxClaimStrategyContract              (moved from catalog tests)

scheduling-infrastructure     (new artifact; adapters for scheduling)
  ScheduledJob, CompetingWorkerTask, ClusterSingletonTask                   (new; driving-adapter types)
  ModuleJobScheduler                                                        (new; driving adapter, per module)
  ShedLockSingletonJobExecutor, LocalSingletonJobExecutor,
  LockProviderType, LockProviderSettings                                    (driven adapters; moved from app/shared/scheduling)
  test-jar:    SingletonJobExecutorContract
```

Dependency rules (ArchUnit):

- `framework` ← `outbox-infrastructure`, `scheduling-infrastructure` ← module configs in `app/<group>/<module>/internal/config` and `workflow-infrastructure`.
- `outbox-infrastructure` does not depend on `scheduling-infrastructure`. The outbox core exposes plain use-case methods; `OutboxModule` turns them into `ScheduledJob`s only when `scheduling-infrastructure` is on the classpath of the composing module (the module config does this, not the library).
- No library references a module name or a module package (`com.catalog..`, `com.grab.app..`).
- No module config depends on `com.grab.app.shared.outbox..` or `com.grab.app.shared.scheduling..` (both packages are deleted at the end).

### 3.2 Single Responsibility — one reason to change per class

| Class | Responsibility | Changes when… |
|---|---|---|
| `OutboxDeliveryEngine<T, ID>` | Claim → dispatch (with trace/MDC context) → hand outcome to recorder; hot/cold coordination with the relay. | the delivery loop changes |
| `OutboxOutcomeRecorder<T, ID>` | Fenced outcome writes in `REQUIRES_NEW`: published, failed + backoff, dead, claim lost, claim release. | outcome semantics change |
| `DualQueueOutboxRelay` | Hot/cold in-memory queues and worker threads. | queueing changes |
| `OutboxRetentionCleaner<T, ID>` | Delete published rows past retention in bounded batches. | retention rules change |
| `OutboxAdministrationService<T, ID>` (implements `OutboxAdministrationPort`) | Operator replay (`DEAD → NEW`) and backlog reads for one module. | operator operations change |
| `OutboxBacklogGauges` | Register `outbox.pending` / `outbox.oldest_pending_age_seconds` for one module, cached sampling. | metric definitions change |
| `OutboxClaimStrategyCheck` | Startup check of the configured claim strategy against the store's dialect. | dialect support changes |
| `ModuleJobScheduler` | Run one module's scheduled jobs on that module's own threads; ordered start/stop. | scheduling mechanics change |
| `OutboxModule<T>` | Composition root for one module's outbox: builds the components above and owns their lifecycle. **No business logic.** | wiring changes |

`OutboxRetryPolicy` (backoff, exhaustion) and `OutboxEventDispatcher` (transport) already exist and are reused — no new retry or dispatcher abstractions.

### 3.3 Open/Closed — extend by adding a strategy, never by subclassing

| Extension point | Implementations | Selected by |
|---|---|---|
| `OutboxClaimStrategy` | `skip-locked`, `conditional-update` (exist) | `<module>.outbox.claim-strategy` |
| `SingletonJobExecutor` | `ShedLockSingletonJobExecutor` (jdbc / jdbc-app-time), `LocalSingletonJobExecutor` (new, for single-replica services and tests) | `<module>.scheduling.lock-provider` (+ `local`) |
| `OutboxEventDispatcher` | in-process `ApplicationEventPublisher::publishEvent` (today); broker dispatcher (future extraction) | module config |
| `OutboxObserver` | `MicrometerOutboxObserver`, `OutboxObserver.NOOP` | presence of a `MeterRegistry` |
| `ScheduledJob` | `CompetingWorkerTask`, `ClusterSingletonTask` (sealed) | job type |

Adding a module = one config class that calls the builder. Adding a transport, lock provider or claim mechanism = one new strategy class; the engine is not edited.

### 3.4 Liskov — composition replaces inheritance, contracts prove substitutability

- `AbstractOutboxProcessor` becomes the `final` class `OutboxDeliveryEngine`; `JpaOutboxDomainEventProducer` becomes `final`. All 10 processor and 10 producer subclasses are deleted (their only contribution — annotations, `Entity::pending`, a log line — moves into the builder and the base producer).
- Every implementation of a strategy interface must pass a contract suite shipped in a library test-jar, so each future service runs the same suite against its own entity and database:
  - `OutboxClaimStrategyContract` (exists in catalog tests → moves): no double claims under 4 workers, stale reclaim, `claimOne`, fencing after takeover.
  - `OutboxStoreContract` (new): writer/claim/maintenance behaviour incl. retention batches and replay.
  - `SingletonJobExecutorContract` (new): mutual exclusion, lock release, `lockAtLeastFor` / `lockAtMostFor`.

### 3.5 Interface Segregation — each client sees only what it uses

```java
package com.grab.outbox.infrastructure.store;

public interface OutboxWriter<T> {                       // JpaOutboxDomainEventProducer
    void saveAll(Collection<T> events);
    void flush();
}

public interface OutboxClaimStore<T, ID> {               // OutboxDeliveryEngine, OutboxOutcomeRecorder
    List<ClaimedOutboxEvent<ID>> claimBatch(OutboxClaimRequest request);
    Optional<ClaimedOutboxEvent<ID>> claimOne(ID id, Instant now);
    Optional<T> findById(ID id);
    Optional<T> findByIdForUpdate(ID id);                // OutboxClaimStrategy.lockForOutcome
}

public interface OutboxMaintenanceStore<T, ID> {         // OutboxRetentionCleaner, OutboxAdministrationService, gauges
    int deletePublishedOlderThan(Instant cutoff, int limit);
    List<T> findDeadForReplay(ID fromId, ID toId, int limit);
    OutboxBacklog backlog(Collection<OutboxStatus> pendingStatuses);
}
```

`JpaOutboxStore` implements all three; `OutboxStore` is removed after Phase 2. The same split applies to the processor's old public API: operators get `OutboxAdministrationPort`, metrics get `OutboxBacklogGauges`, nobody gets the engine.

### 3.6 Dependency Inversion — each module is its own composition root

Components depend only on abstractions (`OutboxClaimStore`, `OutboxEventDispatcher`, `OutboxRetryPolicy`, `OutboxObserver`, `SingletonJobExecutor`, `Clock`). Concrete choices are made **once, inside the module**, from that module's properties:

```java
// app/merchandising/catalog/internal/config — moves with catalog when it is extracted
@Configuration(proxyBeanMethods = false)
public class CatalogModuleOutboxConfig {

    @Bean("catalogSingletonJobExecutor")
    public SingletonJobExecutor catalogSingletonJobExecutor(
            @Qualifier("catalogDataSource") DataSource dataSource,
            @Value("${catalog.flyway.default-schema:catalog}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meterRegistry) {
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "catalog.scheduling", meterRegistry.getIfAvailable());
    }

    @Bean("catalogOutboxModule")
    public OutboxModule<CatalogOutboxEvent> catalogOutboxModule(
            JpaContext jpaContext,
            @Qualifier("catalogTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("catalogSingletonJobExecutor") SingletonJobExecutor singletonJobs,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meterRegistry) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(CatalogOutboxEvent.class);
        return OutboxModule.builder("catalog", CatalogOutboxEvent.class, CatalogOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .dispatcher(publisher::publishEvent)
                .singletonJobs(singletonJobs)
                .properties(environment)                    // reads catalog.outbox.* only
                .meterRegistry(meterRegistry.getIfAvailable())
                .build();
    }

    @Bean("catalogDomainEventProducer")                      // unchanged name: persistence adapters keep their @Qualifier
    public DomainEventProducer catalogDomainEventProducer(
            @Qualifier("catalogOutboxModule") OutboxModule<CatalogOutboxEvent> outbox) {
        return outbox.producer();
    }

    @Bean("catalogProcessedEventRetention")
    public ProcessedEventRetention catalogProcessedEventRetention(…) { … }

    @Bean("catalogJobScheduler")
    public ModuleJobScheduler catalogJobScheduler(
            @Qualifier("catalogOutboxModule") OutboxModule<CatalogOutboxEvent> outbox,
            @Qualifier("catalogProcessedEventRetention") ProcessedEventRetention inboxRetention,
            Environment environment) {
        List<ScheduledJob> jobs = new ArrayList<>(OutboxScheduledJobs.of(outbox, environment));
        jobs.add(InboxScheduledJobs.of("catalog", inboxRetention, environment));
        return ModuleJobScheduler.fromProperties("catalog", jobs, environment);  // catalog.scheduling.pool-size
    }
}
```

`OutboxScheduledJobs` / `InboxScheduledJobs` live in `scheduling-infrastructure` and adapt the driving ports (`DeliverOutboxEventsUseCase.pollOnce()`, `cleanupPublished()`, `ProcessedEventRetention.purgeExpired()`) into typed jobs, so neither `outbox-infrastructure` nor `ProcessedEventRetention` knows about schedulers.

About 60 lines including imports (from 123), with every bean name that other code depends on unchanged. The serializer, observer, claim-strategy check, backlog gauges and administration are created **inside** `OutboxModule`, per module — there are no global outbox beans left.

### 3.7 Scheduling: per-module, typed, bulkheaded

```java
// scheduling-infrastructure (adapter types; framework keeps only SingletonJob / SingletonJobExecutor ports)
public sealed interface ScheduledJob permits CompetingWorkerTask, ClusterSingletonTask {
    String name();
    Duration initialDelay();
    Duration fixedDelay();
}
public record CompetingWorkerTask(String name, String partitioning, Duration initialDelay,
                                  Duration fixedDelay, Runnable body) implements ScheduledJob { }
public record ClusterSingletonTask(SingletonJob job, SingletonJobExecutor executor, Duration initialDelay,
                                   Duration fixedDelay, Runnable body) implements ScheduledJob { }
```

- Classification is the type: a class-B job cannot be constructed without a lock executor, so the guarantee that `@ClusterSingletonJob` + ArchUnit provide today becomes a compile-time guarantee.
- `ModuleJobScheduler` owns a small `ThreadPoolTaskScheduler` per module (default pool 2: poller + cleanups; `<module>.scheduling.pool-size`). This is a bulkhead matching the per-module HikariCP pools: one module's slow cleanup cannot starve another module's poller, and the shared pool (14 threads) is no longer used by outbox jobs. Thread budget: 10 modules × 2 = 20 threads, mostly idle.
- It does **not** use Spring's global `ScheduledTaskRegistrar` (a single registrar per context would be a shared runtime component). It rejects duplicate job names and wraps `ClusterSingletonTask` bodies in `executor.execute(job, body)`.
- Lifecycle is module-local (`SmartLifecycle` phases defined in the library): start relay → start scheduler; stop scheduler (await in-flight runs) → stop relay (release undispatched claims). Today this ordering depends on global bean destruction order.
- Job names and delays keep today's properties: `<module>.outbox.fixed-delay-ms`, `<module>.outbox.cleanup-fixed-delay-ms`, `inbox.cleanup.*` (now overridable per module as `<module>.inbox.cleanup.*`). Operators change nothing.
- The remaining non-outbox `@Scheduled` jobs (security-manifest triggers, `WorkflowSweeper`) keep their annotations and the existing `everyScheduledMethodIsClassified` rule; migrating them to `ModuleJobScheduler` is a follow-up.

### 3.8 What happens to `app/shared/outbox` and `app/shared/scheduling`

| Today (`app/shared`) | Becomes | Scope |
|---|---|---|
| `MicrometerOutboxObserver` | `outbox-infrastructure/metrics`, created per module by the builder (tagged `module`) | library, per module |
| `OutboxBacklogMetrics` (all processors) | `OutboxBacklogGauges` per module | library, per module |
| `OutboxReplayer` (map of modules) | `OutboxAdministrationPort` per module, exposed as `outbox.administration()`; no consumer exists today (verified), so no HTTP surface is added. An optional library actuator endpoint over `OutboxModule` beans can be added later. | library, per module |
| `OutboxClaimStrategyVerifier` (all stores) | `OutboxClaimStrategyCheck` run by the builder; mode `<module>.outbox.claim-strategy-check` (default `fail`; global `outbox.claim-strategy-check` kept as fallback) | library, per module |
| `ObservabilityBeanPostProcessor` (wraps every inbox and publication port) | Inbox: module config wraps explicitly with `ObservedProcessedEventRepository` (moved to `outbox-infrastructure/inbox`). Publication ports: out of scope (security-manifest code); stays until that area is refactored, but moves out of the `outbox` package. | explicit decorator per module |
| `ShedLockSingletonJobExecutor`, `LockProviderType`, `LockProviderSettings` | `scheduling-infrastructure` | library |
| `OutboxObservabilityConfig` | deleted | — |
| `AsyncExecutorConfig`, tracing, bootstrap | unchanged (process-level; each future service has its own) | app |

### 3.9 Hexagonal architecture of the outbox subsystem

At system level the outbox already fits the hexagon: domains emit events without knowing about the outbox, persistence adapters write through the `DomainEventProducer` outbound port, outbox rows live in `*-adapter-persistence`, and each module's config is its outermost composition root. The gaps are **inside** the outbox: core logic and adapters share one package tree, the core depends on Spring transactions, there are no inbound ports, and today a driving adapter (`@Scheduled`) sits inside a driven adapter (`*-adapter-persistence`). Revision 4 treats the outbox subsystem as its own hexagon:

```
            driving adapters                      core (no Spring, no JPA)                    driven adapters
  ┌──────────────────────────┐      ┌───────────────────────────────────────────┐      ┌──────────────────────────────┐
  │ ModuleJobScheduler       │─────▶│ port/in  DeliverOutboxEventsUseCase        │      │ out/jpa  JpaOutboxStore       │
  │  (OutboxScheduledJobs)   │      │          OutboxAdministrationPort          │      │          + claim strategies   │
  │ DualQueueOutboxRelay     │─────▶│                                            │      │ out/spring SpringOutbox-      │
  │  (hot / cold workers)    │      │ service  OutboxDeliveryEngine              │─────▶│          Transactions,        │
  │ (future) admin endpoint  │─────▶│          OutboxOutcomeRecorder             │      │          InProcess dispatcher │
  └──────────────────────────┘      │          OutboxRetentionCleaner            │      │ out/micrometer observer       │
                                    │          OutboxAdministrationService       │      │ ShedLock executor             │
                                    │ port/out OutboxWriter / ClaimStore /       │      │ (future) broker dispatcher    │
                                    │          MaintenanceStore, OutboxTransactions│    └──────────────────────────────┘
                                    │          OutboxObserver, OutboxEventDispatcher,
                                    │          SingletonJobExecutor, Clock        │
                                    └───────────────────────────────────────────┘
                         module/OutboxModule — composition only (the only package that sees both sides)
```

**Inbound ports**

```java
package com.grab.outbox.infrastructure.core.port.in;

public interface DeliverOutboxEventsUseCase {
    void pollOnce();                     // cold path: claim a batch, hand to relay or deliver inline
    void deliverHot(Object id);          // hot path after commit (relay worker)
    void deliverClaimed(ClaimedOutboxEvent<?> claim);   // cold-queue worker
    void cleanupPublished();             // retention, under the module's SingletonJobExecutor
}

public interface OutboxAdministrationPort {
    int replayDead(long fromId, long toId, int limit);
    OutboxBacklog backlog();
}
```

**Transaction port** (replaces `PlatformTransactionManager` / `TransactionTemplate` in the core; the existing `PersistenceExecutor` only translates exceptions, so it is not reused):

```java
package com.grab.outbox.infrastructure.core.port.out;

public interface OutboxTransactions {
    <R> R inNewTransaction(Supplier<R> work);        // claims, outcomes, cleanup batches, replay
    <R> R readOnly(Supplier<R> work);                // backlog
}
// adapter/out/spring/SpringOutboxTransactions — TransactionTemplate(REQUIRES_NEW) on the module transaction manager
```

**Enforced by ArchUnit (`OutboxHexagonArchitectureTest` in `outbox-infrastructure`):**

| Rule | |
|---|---|
| `core..` must not depend on `org.springframework..`, `jakarta.persistence..`, `io.micrometer..`, `net.javacrumbs..` | core is framework-free |
| `core..` must not depend on `adapter..` or `module..` | dependency points inward |
| `adapter..` may depend on `core.port..` but not on `core.service..` | adapters see ports only |
| Only `module..` may depend on both `core.service..` and `adapter..` | single composition point |
| Only `adapter.out.jpa..` may use `jakarta.persistence.LockModeType` (replaces the rule added for claim strategies) | locking stays in the JPA adapter |

**Deliberate deviations (documented, not fixed):**

1. **No separate core outbox model.** The core mutates rows through the `OutboxEntry` contract (`markProcessing`, `markFailed`, …) that the JPA entities implement. The outbox row has no business invariants to protect; a mapped core model would add a copy per event on the hot path and does not fit bulk claims (`conditional-update` never loads entities).
2. **Integration events remain a shared kernel** (`app/shared/events`, consumed in-process via `@EventListener`). This is the main remaining extraction blocker and gets its own plan (per-owner event contract artifacts + a broker `OutboxEventDispatcher` adapter). This plan keeps `OutboxEventDispatcher` as that seam.

---

## 4. Work breakdown

Each phase leaves the build green and is independently mergeable. Catalog pilots every structural change before rollout.

### Phase 1 — Libraries and contracts (no behaviour change)
| # | Task |
|---|---|
| 1.1 | Create `scheduling-infrastructure`; move `ShedLockSingletonJobExecutor`, `LockProviderType`, `LockProviderSettings` and their tests. Add `LocalSingletonJobExecutor`. |
| 1.2 | Move `MicrometerOutboxObserver` and backlog sampling into `outbox-infrastructure/metrics` (micrometer `optional`). |
| 1.3 | Move `OutboxClaimStrategyContract` (+ Postgres/H2 bases) into an `outbox-infrastructure` test-jar; catalog keeps thin subclasses. Add `OutboxStoreContract`, `SingletonJobExecutorContract`. |
| 1.4 | ArchUnit: libraries reference no module packages; `outbox-infrastructure` ↛ `scheduling-infrastructure`. |

### Phase 2 — Interface segregation
| # | Task |
|---|---|
| 2.1 | Add `OutboxWriter`, `OutboxClaimStore`, `OutboxMaintenanceStore`; `JpaOutboxStore` implements them. |
| 2.2 | `JpaOutboxDomainEventProducer` → `final`, depends on `OutboxWriter`; absorbs the module-tagged "Producing N … event(s)" log. |
| 2.3 | Remove `OutboxStore`. |

### Phase 3 — Single responsibility and the outbox hexagon (§3.9)
| # | Task |
|---|---|
| 3.1 | Create `core/port/in`, `core/port/out`, `core/service`, `adapter/in`, `adapter/out/{jpa,spring,micrometer}`, `module` packages; move existing classes (claim strategies → `adapter/out/jpa/claim`, relay → `adapter/in/relay`). |
| 3.2 | Add `OutboxTransactions` port and `SpringOutboxTransactions` adapter; replace every `TransactionTemplate` in core code. |
| 3.3 | Extract `OutboxOutcomeRecorder` from `publishEvent` / `recordFailure` / `releaseClaim` / `reportClaimLost`. |
| 3.4 | Extract `OutboxRetentionCleaner` and `OutboxAdministrationService` (implements `OutboxAdministrationPort`). |
| 3.5 | Convert `AbstractOutboxProcessor` into `final OutboxDeliveryEngine` implementing `DeliverOutboxEventsUseCase`, depending on `OutboxClaimStore` and `OutboxTransactions` only; replace `DisposableBean` with an explicit `shutdown()` called by the module lifecycle. Existing subclasses temporarily delegate so modules still compile. |
| 3.6 | Move `OutboxClaimStrategyVerifier` logic into `adapter/out/jpa/claim/OutboxClaimStrategyCheck`. |
| 3.7 | Add `OutboxHexagonArchitectureTest`; engine/recorder unit tests run with an in-memory `OutboxTransactions` (no Spring). |

### Phase 4 — Per-module composition and scheduling (pilot: catalog)
| # | Task |
|---|---|
| 4.1 | Add `ScheduledJob`, `CompetingWorkerTask`, `ClusterSingletonTask`, `ModuleJobScheduler`, `OutboxScheduledJobs`, `InboxScheduledJobs` to `scheduling-infrastructure`. |
| 4.2 | Add `OutboxModule` + builder (property reading, check, gauges, observer, lifecycle). |
| 4.3 | Drop `@Scheduled` from `ProcessedEventRetention`; it is scheduled through `InboxScheduledJobs`. |
| 4.4 | Rewrite `CatalogModuleOutboxConfig` per 3.6; delete `CatalogOutboxEventProcessor`, `CatalogOutboxEventProducer`. |
| 4.5 | Add `CatalogOutboxModuleTest` (context): jobs registered, bean names intact, shutdown releases claims after polling stops. |

### Phase 5 — Rollout and removal of shared runtime
| # | Task |
|---|---|
| 5.1 | Apply 4.4 to cart, region, saleschannel, inventory, pricing, identity, customer, merchant, and `WorkflowInfraConfig` (workflows). |
| 5.2 | Delete the remaining processor/producer subclasses, `OutboxObservabilityConfig`, `OutboxReplayer`, `OutboxBacklogMetrics`, `OutboxClaimStrategyVerifier`, and `app/shared/scheduling/*` except `AsyncExecutorConfig`. |
| 5.3 | Inbox decoration explicit per module; remove inbox handling from `ObservabilityBeanPostProcessor`. |
| 5.4 | ArchUnit: no class outside `app/shared` depends on `app.shared.outbox` / `app.shared.scheduling` (packages then deleted); only `ModuleJobScheduler` calls `TaskScheduler.schedule*`. |

### Phase 6 — Extraction rehearsal (verification)
| # | Check |
|---|---|
| 6.1 | `ModuleIndependenceTest` (ArchUnit): `app/<group>/<module>/internal/config/*Outbox*` depends only on its own module, `framework`, `outbox-infrastructure`, `scheduling-infrastructure`, Spring. |
| 6.2 | Boot a context containing **only** catalog's persistence + outbox config against Testcontainers PostgreSQL: produce → deliver → retention → replay work with no other module present. |
| 6.3 | Existing suites green: claim contract (Postgres both strategies + H2), `OutboxBacklogAndReplayPostgresTest`, `SchedulingArchitectureTest`, `ModularityTests`, full `./mvnw verify`. |

---

## 5. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Bean renames break `@Qualifier` injection in persistence configs | High | Names `*DomainEventProducer`, `*SingletonJobExecutor`, `*ProcessedEventRetention` unchanged; context tests per phase. |
| Programmatic scheduling escapes guardrails | High | Sealed job types; only `ModuleJobScheduler` may schedule (ArchUnit); per-module inventory test. |
| More threads (20 module scheduler threads) | Low | Mostly idle fixed-delay tasks; pool size per module configurable; outbox jobs leave the shared pool, which shrinks. |
| Metric names or tags change | Medium | Keep `outbox.*` names and the `module` tag; dashboard check in Phase 5. |
| Shutdown ordering regression (claims released while polling) | Medium | Module-local `SmartLifecycle` phases; test 4.5. |
| Transaction boundaries drift | High | Claims keep their short transaction; outcomes, cleanup and replay keep `REQUIRES_NEW` on the module transaction manager. |
| Extraction still blocked by `app/shared/events` and in-process dispatch | Known | Out of scope; separate plan for per-owner event contract artifacts and a broker `OutboxEventDispatcher`. |

---

## 6. Acceptance criteria

- [ ] `app/shared/outbox` and `app/shared/scheduling` contain no outbox or scheduling beans (only `AsyncExecutorConfig` remains; publication-port observability moved out of the `outbox` package).
- [ ] Every module's outbox, inbox retention, lock executor and scheduler are built in that module's own config, from `<module>.*` properties, and pass `ModuleIndependenceTest`.
- [ ] No `*OutboxEventProcessor` / `*OutboxEventProducer` subclasses remain; `OutboxDeliveryEngine` and `JpaOutboxDomainEventProducer` are `final`.
- [ ] `OutboxDeliveryEngine` contains no cleanup, replay, backlog or metrics code; `OutboxOutcomeRecorder`, `OutboxRetentionCleaner`, `OutboxAdministrationService`, `ModuleJobScheduler` are each ≤ 150 lines.
- [ ] Producer depends only on `OutboxWriter`; engine only on `OutboxClaimStore`; cleaner/administration/gauges only on `OutboxMaintenanceStore`.
- [ ] Claim-strategy, store and singleton-executor contract suites ship in library test-jars and run in at least catalog and one other module.
- [ ] Each outbox module job is a `CompetingWorkerTask` or `ClusterSingletonTask` on its module's `ModuleJobScheduler`.
- [ ] Outbox `core..` has no Spring, JPA, Micrometer or ShedLock imports; adapters depend only on `core.port..`; `OutboxHexagonArchitectureTest` passes.
- [ ] Scheduler, relay and (future) admin endpoint reach the core only through `DeliverOutboxEventsUseCase` / `OutboxAdministrationPort`.
- [ ] `framework/scheduling` contains only ports (`SingletonJob`, `SingletonJobExecutor`).
- [ ] The two deliberate deviations (§3.9) are recorded; integration-event extraction has its own plan.
- [ ] Catalog-only context boots and delivers events (6.2); full `./mvnw verify` passes; AGENTS.md R1, R11, R16, R17, R22 preserved.
