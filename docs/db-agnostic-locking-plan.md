# Plan: Database-Agnostic Concurrency Control for Outbox & Scheduler

> Goal: keep the guarantees of the current outbox / inbox / scheduler design (no double publish, fenced outcomes,
> one run per singleton interval, safe boot races) while removing the hard dependency on PostgreSQL, so the same
> code runs on PostgreSQL, MySQL 8 / MariaDB, Oracle, SQL Server, and H2 (tests), and can later move off the RDBMS
> for coordination.
>
> Related: `outbox-events-scheduling-plan.md`, `outbox-scheduler-architecture-review.md`.

---

## 1. Current locking mechanisms and their DB coupling

| # | Mechanism | Where | How it is implemented today | Portable? |
|---|---|---|---|---|
| L1 | Batch claim of outbox rows | `JpaOutboxStore.findBatchForProcessing` | JPQL + `PESSIMISTIC_WRITE` + hint `jakarta.persistence.lock.timeout = -2` → Hibernate renders `FOR UPDATE SKIP LOCKED` | **Partly.** JPQL is portable; *skip-locked* depends on the dialect (see §1.1). |
| L2 | Hot-path claim of one row | `findByIdForUpdateSkipLocked` | `em.find(..., PESSIMISTIC_WRITE, skip-locked hint)` | Same as L1 |
| L3 | Fenced outcome (published / failed / dead) | `findByIdForUpdate` + `isClaimedBy(token)` | `em.refresh(entity, PESSIMISTIC_WRITE)` → `SELECT … FOR UPDATE` | **Yes** (every major RDBMS has a blocking row lock; Hibernate renders it per dialect) |
| L4 | Stale-claim recovery | `claimed_at <= now - claimTimeout` | Timestamp from the **application clock** | Yes, but sensitive to clock skew between pods |
| L5 | Inbox dedupe | `JpaProcessedEventRepository.markProcessed` | `find` → `persist` → `flush`; a concurrent duplicate fails on the PK and the transaction rolls back (outbox retries; next attempt sees the row) | **Yes** (PK uniqueness only) |
| L6 | Cluster singleton jobs and bootstrap locks | `ShedLockSingletonJobExecutor` | ShedLock `JdbcTemplateLockProvider` + `usingDbTime()`, table `<schema>.shedlock` | **Yes** for the DBs ShedLock supports (PG, MySQL, MariaDB, Oracle, SQL Server, DB2, H2, HSQLDB); DDL differs per vendor |
| L7 | Boot-race on marketplace channel | Unique index + `@EnsureRetryable` (retry in new tx) | Constraint violation → retry → re-read | **Yes** |
| L8 | Schema / data migrations | `db/migration/<module>/V*.sql` (32 files touched) | `uuid`, `gen_random_uuid()`, `timestamptz`, partial indexes (`WHERE status = 'DEAD'`), `ON CONFLICT DO NOTHING`, quoted `"order"` schema | **No** — PostgreSQL SQL |

### 1.1 Skip-locked support (the real portability gap)

| Database | Row-skip syntax | Hibernate 6 renders it? |
|---|---|---|
| PostgreSQL ≥ 9.5 | `FOR UPDATE SKIP LOCKED` | Yes |
| MySQL ≥ 8.0 / MariaDB ≥ 10.6 | `FOR UPDATE SKIP LOCKED` | Yes |
| Oracle | `FOR UPDATE SKIP LOCKED` | Yes |
| SQL Server | `WITH (UPDLOCK, ROWLOCK, READPAST)` | Yes |
| DB2 | `SKIP LOCKED DATA` | Yes (recent dialects) |
| H2 | not supported | **Silently ignored** → plain `FOR UPDATE` (blocking, serialises workers) |
| SQLite / others | no row locks | Ignored |

**Risk today:** on a dialect without skip-locked support, the hint is dropped *silently*. The code stays correct
(L3 fencing still holds) but competing workers queue behind each other, and the "both workers get work" property
is lost without any signal.

---

## 2. Requirements for any alternative

| ID | Requirement |
|---|---|
| R-A | A row is dispatched by at most one worker at a time (duplicates only after a claim timeout, absorbed by the inbox). |
| R-B | Outcome updates are fenced: a worker whose claim was taken over cannot overwrite the new state. |
| R-C | Multiple replicas share the backlog (throughput scales with replicas, or at least does not collapse). |
| R-D | Singleton jobs run at most once per interval cluster-wide; a crashed holder is recovered within a bound. |
| R-E | Uses only JPA/JPQL, standard SQL, or a pluggable non-DB coordinator — no native queries (repo rule R11). |
| R-F | Works with the per-module schema / per-pool isolation (no shared cross-schema lock table). |
| R-G | Observable: contention and lost claims are measurable (`outbox.claim.lost`, `scheduler.job.skipped_locked`). |

---

## 3. Alternatives for outbox claiming (L1/L2)

### O1 — Keep pessimistic skip-locked, add capability detection (baseline)
Read `Dialect#supportsSkipLocked()` from the `SessionFactory` at startup. If unsupported, fail fast (prod) or log
a WARN and switch strategy (dev/test).

| Pros | Cons |
|---|---|
| No behaviour change on PG/MySQL/Oracle/SQL Server | Still requires a DB with skip-locked to get parallelism |
| Turns the silent degradation into an explicit decision | Doesn't help H2-based tests reflect production |

### O2 — Optimistic claim via conditional bulk UPDATE (portable fallback)
1. `SELECT e.id FROM Outbox e WHERE <claimable> ORDER BY e.id` with `setMaxResults(n)` — **no lock**.
2. `UPDATE Outbox e SET e.status = PROCESSING, e.claimToken = :t, e.claimedAt = :now, e.attemptCount = e.attemptCount + 1
   WHERE e.id IN :ids AND (<claimable>)` — JPQL bulk update; only rows still claimable change.
3. `SELECT e FROM Outbox e WHERE e.claimToken = :t` → exactly the rows this worker won.

Fencing (R-B) is unchanged: outcomes still check `claimToken`. An `@Version` column can replace the
`FOR UPDATE` re-read in L3 (`UPDATE … WHERE id = :id AND claim_token = :t` → 0 rows = claim lost).

| Pros | Cons |
|---|---|
| Pure JPQL, works on every RDBMS incl. H2 | Workers compete for the *same* head-of-queue ids → wasted round trips under contention |
| No long-held row locks; short transactions | Needs mitigation: random offset / shuffled candidate window (e.g. pick `n` from the first `4n`) |
| Easy to reason about; claim token already exists | Slightly more SQL per batch (3 statements) |

### O3 — Partitioned ownership (bucket leases)
Rows get `bucket = hash(aggregate_id) mod B` (e.g. B = 32). Replicas lease buckets through the existing
`SingletonJobExecutor` (`outbox:<module>:bucket:<k>`), then claim only their buckets with O2 (no contention).

| Pros | Cons |
|---|---|
| Portable; contention-free claims | Rebalancing on scale-up/down = lease expiry latency |
| **Per-aggregate ordering** becomes possible (fixes review con C3) | New column + backfill; bucket count is hard to change later |
| Reuses ShedLock — no new infrastructure | One stuck bucket holder stalls its slice until the lease expires |

### O4 — Single active poller per module (leader) + in-process workers
A class B job (`<module>.outbox.poll`) claims batches; hot-path delivery stays local to the producing pod.

| Pros | Cons |
|---|---|
| Simplest portable option; no row-level contention at all | Cold-path throughput bounded by one pod |
| Ordering per module achievable | Failover delay = `lockAtMostFor`; hot path still needs O1/O2 for cross-pod safety |

### O5 — External coordinator (Redis / ZooKeeper / etcd / Hazelcast)
Claims or leases live outside the DB (e.g. Redis `SET NX PX`, Redisson fair locks, Hazelcast CP).

| Pros | Cons |
|---|---|
| Fully DB-agnostic; very fast | New infrastructure and failure mode; lock and data no longer share a transaction (fencing must stay in the DB via claim token/version) |
| Same coordinator can back the scheduler | Redis locks are not linearizable without care (Redlock debate) |

### O6 — Broker / CDC (future phase)
Outbox rows are relayed to Kafka (polling relay under one singleton, or Debezium CDC). Consumer groups own
partitions; the DB only needs append + cleanup.

| Pros | Cons |
|---|---|
| Removes row-claiming entirely; ordering per key; independent consumer scaling | Kafka/Connect infrastructure; Debezium connectors are themselves DB-specific (but per-vendor connectors exist) |
| Inbox, `event_id`, versioning carry over unchanged | Largest change; out of scope for "agnostic locking" alone |

### Outbox comparison

| Criterion | O1 | O2 | O3 | O4 | O5 | O6 |
|---|---|---|---|---|---|---|
| Portable across RDBMS | ◑ | ● | ● | ● | ● | ◑ |
| Multi-replica throughput | ● | ◑ | ● | ◔ | ● | ● |
| Ordering per aggregate | ○ | ○ | ● | ◑ | ◑ | ● |
| New infrastructure | none | none | none | none | yes | yes |
| Implementation effort | XS | S | M | S | M | L |

● strong ◑ partial ◔ weak ○ none

---

## 4. Alternatives for cluster singleton / bootstrap locks (L6)

| Option | Mechanism | Pros | Cons |
|---|---|---|---|
| S1 — ShedLock JDBC (**current**) | Row per lock name, `lock_until`, DB time | Already multi-vendor; per-schema table fits R-F; tiny | DDL per vendor; `lockAtMostFor` must exceed job duration |
| S2 — ShedLock non-JDBC provider | Redis / Mongo / Hazelcast / ZooKeeper / K8s ConfigMap providers | Same API, zero code change behind `SingletonJobExecutor` | New infrastructure |
| S3 — Spring Integration `JdbcLockRegistry` | `INT_LOCK` table, renewable locks | Multi-vendor DDL shipped; supports lock renewal | Extra dependency; global table unless one registry per schema |
| S4 — Custom JPA lease table with `@Version` | `UPDATE lease SET owner=?, until=?, version=version+1 WHERE name=? AND (until < :now OR owner = ?)` | Pure JPQL; no library | Must decide time source: app clock (skew) vs DB time (not portable in JPQL) |
| S5 — Kubernetes Lease (Spring Cloud Kubernetes leader election) | One leader per pod group | DB-independent; standard K8s primitive | Binds to K8s; one leader for all jobs (coarse); RBAC needed |
| S6 — db-scheduler / Quartz clustered JobStore | Persistent task tables with vendor delegates | Rich scheduling, misfire handling, history | Heavy; replaces `@Scheduled` model; Quartz schema is large |

**Recommendation:** keep **S1** (it is already database-agnostic), ship vendor DDL for `shedlock`, and keep
`SingletonJobExecutor` as the seam so S2/S5 remain a configuration swap.

---

## 5. Cross-cutting concerns

| Concern | Today | Agnostic approach |
|---|---|---|
| Time source for claims (L4) | App clock `Instant.now()` | Keep app clock but require NTP and size `claimTimeout` ≫ max skew; or read DB time once per batch via a portable `SELECT CURRENT_TIMESTAMP` (JPQL `CURRENT_TIMESTAMP`) |
| Migrations (L8) | PG-only SQL | Flyway vendor locations: `db/migration/{vendor}/<module>` (Flyway resolves `{vendor}`), or Liquibase changelogs with `dbms` contexts. Replace `gen_random_uuid()` defaults with application-generated ids (already done in code), `ON CONFLICT` with idempotent bootstrap tasks, partial indexes with full indexes where unsupported |
| UUID / timestamp types | `uuid`, `timestamptz` | JPA `@JdbcTypeCode(SqlTypes.UUID)` + `Instant` mapped to vendor-appropriate types (`BINARY(16)`/`CHAR(36)`, `TIMESTAMP(6)`); Hibernate handles per dialect |
| Schema isolation (R-F) | PG schemas | MySQL: database per module; Oracle/SQL Server: schema/user per module — same `<module>DataSource` + Flyway per module |
| Inbox (L5) | PK collision → rollback → retry | Already portable; optionally catch `ConstraintViolationException` in a `REQUIRES_NEW` probe to avoid a full retry cycle |

---

## 6. Target design — strategy pattern, selected by manual configuration

Every database-sensitive concurrency operation sits behind a **strategy interface**. Which strategy a module uses
is **set explicitly in configuration** per module: no database detection, no capability probing, no
auto-selection. Whoever deploys a module onto a database also picks the matching strategy (see the cheat sheet
in §6.5). The outbox processor, scheduler jobs and bootstrap runner keep calling the same ports (`OutboxStore`,
`SingletonJobExecutor`) and never see which strategy is behind them.

```
 catalog.outbox.claim-strategy ─────▶ OutboxClaimStrategies.byName(...) ─▶ JpaOutboxStore(em, type, strategy)
                                                                                   │
                                                                                   ▼
                                                                         AbstractOutboxProcessor   (unchanged caller)

 catalog.scheduling.lock-provider ──▶ SingletonLockProviders.byName(...) ─▶ ShedLockSingletonJobExecutor(provider)
                                                                                   │
                                                                                   ▼
                                                                         class B jobs / BootstrapRunner   (unchanged callers)
```

**Why manual instead of detection**

| | Manual config (chosen) | Auto-detection (`DatabaseProfile`) |
|---|---|---|
| Code | One `enum` + a lookup per concern | Detector, capability record, version table, resolver, strict mode |
| Predictability | What you configure is what runs; easy to review in a diff | Depends on runtime detection and on the Hibernate dialect setting |
| Testing | Choose the strategy directly in each test | Also needs tests for the detector and resolver |
| Risk | A wrong value is possible, e.g. `skip-locked` on H2 silently serialises the workers | Picks the wrong strategy only if detection is wrong |
| Mitigation | Safe default + validation at startup + one-line dialect check (§6.4) | n/a |

### 6.1 Outbox claim strategy

The current flow is "find rows under a lock, then call `markProcessing` on each entity". That split only works
with pessimistic locking, so each strategy owns **find + claim together** and returns rows it has already claimed:

```java
// outbox-infrastructure: com.grab.outbox.infrastructure.claim
public interface OutboxClaimStrategy {

    /** Claims up to request.limit() claimable rows; returns only the rows this caller now owns. */
    <T extends OutboxEntry<ID>, ID> List<ClaimedOutboxEvent<ID>> claimBatch(ClaimContext<T> ctx, ClaimRequest request);

    /** Hot path: claims a single row if it is still claimable; empty if taken or not ready. */
    <T extends OutboxEntry<ID>, ID> Optional<ClaimedOutboxEvent<ID>> claimOne(ClaimContext<T> ctx, ID id, Instant now);

    /** Re-reads a claimed row exclusively so only the token holder can apply an outcome. */
    default <T extends OutboxEntry<ID>, ID> Optional<T> lockForOutcome(ClaimContext<T> ctx, ID id) {
        return PessimisticOutcomeLock.lock(ctx, id);   // SELECT … FOR UPDATE — portable on every supported RDBMS
    }
}

public record ClaimRequest(Collection<OutboxStatus> retryable, Instant now, Instant staleBefore, int limit) {}
public record ClaimContext<T>(EntityManager em, Class<T> entityType, String entityName) {}
```

| Config value | Class | Mechanics | Use on |
|---|---|---|---|
| `skip-locked` **(default)** | `SkipLockedClaimStrategy` | Current code moved here: JPQL + `PESSIMISTIC_WRITE` + lock-timeout hint `-2`, then `entity.markProcessing(now, token)` for each row | PostgreSQL, MySQL ≥ 8.0, MariaDB ≥ 10.6, Oracle, SQL Server, DB2. Hibernate generates `SKIP LOCKED` or `READPAST` for each of these, so one class covers them all |
| `conditional-update` | `ConditionalUpdateClaimStrategy` | (1) select candidate ids with no lock from a window of `limit × 4`, shuffled; (2) JPQL bulk `UPDATE … SET status=PROCESSING, claimToken=:t, claimedAt=:now, attemptCount=attemptCount+1 WHERE id IN :ids AND (<claimable>)`; (3) `SELECT … WHERE claimToken = :t`; `em.clear()` after the bulk update | H2, MySQL 5.7, MariaDB < 10.6, any other database. Works everywhere, with some contention between workers |
| `serial-leader` *(optional)* | `SerialLeaderClaimStrategy` | Wraps `conditional-update`; claims only while it holds `outbox:<module>:claim` through `SingletonJobExecutor` | Databases with no usable row locks. Gives up throughput for zero contention |
| `bucket-lease` *(future, O3)* | `BucketLeaseClaimStrategy` | Leases hash buckets; claims with `conditional-update` filtered by `bucket in :owned` | When per-aggregate ordering is required |

```java
public enum ClaimStrategyType {
    SKIP_LOCKED, CONDITIONAL_UPDATE, SERIAL_LEADER;          // Spring binds "skip-locked" → SKIP_LOCKED
}

public final class OutboxClaimStrategies {
    public static OutboxClaimStrategy of(ClaimStrategyType type, SingletonJobExecutor executor) {
        return switch (type) {
            case SKIP_LOCKED -> new SkipLockedClaimStrategy();
            case CONDITIONAL_UPDATE -> new ConditionalUpdateClaimStrategy();
            case SERIAL_LEADER -> new SerialLeaderClaimStrategy(new ConditionalUpdateClaimStrategy(), executor);
        };
    }
}
```

A plain `switch` over an enum is enough here: the set of strategies is small, closed, and lives in one module.
Adding a strategy means adding one enum constant, one class and one `case`. The compiler forces the `case`.

`JpaOutboxStore` delegates to the injected strategy. `OutboxStore` gains `claimBatch` / `claimOne` /
`lockForOutcome` and loses `findBatchForProcessing` / `findByIdForUpdateSkipLocked` / `findByIdForUpdate`.
`AbstractOutboxProcessor` changes only at its call sites (around lines 250, 269, 304, 343 and 383) and loses its
private `claim(...)` helper.

### 6.2 Singleton lock provider (scheduler + bootstrap)

`SingletonJobExecutor` is already the strategy interface. Only the ShedLock `LockProvider` behind it becomes
configurable:

| Config value | Provider | Notes |
|---|---|---|
| `jdbc` **(default)** | `JdbcTemplateLockProvider` + `usingDbTime()` | ShedLock ships SQL for PG, MySQL, MariaDB, Oracle, SQL Server, DB2, H2 and HSQLDB. The table name comes from `table` (default `<schema>.shedlock`); on MySQL set it to `<database>.shedlock` |
| `jdbc-app-time` | `JdbcTemplateLockProvider` without DB time | For databases ShedLock can't read the time from. Requires NTP and a generous `lockAtMostFor` |
| `redis` *(optional)* | `RedisLockProvider` | Independent of the database. Key prefix `<module>:` |
| `kubernetes` *(optional)* | K8s Lease provider | Independent of the database. Needs RBAC permission on `leases` |

`ShedLockSingletonJobExecutor.forSchema(...)` becomes `ShedLockSingletonJobExecutor.from(LockProviderSettings, DataSource, MeterRegistry)`.
Bootstrap locks (`bootstrap:<key>`) automatically use the same provider.

### 6.3 Configuration

```yaml
catalog:
  outbox:
    claim-strategy: skip-locked      # skip-locked | conditional-update | serial-leader
  scheduling:
    lock-provider: jdbc              # jdbc | jdbc-app-time | redis | kubernetes
    lock-table: catalog.shedlock     # optional; defaults to <flyway default-schema>.shedlock
```

```yaml
# application-test-h2.yml (example)
catalog.outbox.claim-strategy: conditional-update
```

Both properties are bound into enums (`ClaimStrategyType`, `LockProviderType`), so a typo fails at startup with
Spring's binding error rather than at the first claim.

Wiring for one module (the 10 module configs and `WorkflowInfraConfig` repeat this, so a `ModuleOutboxWiring`
helper in `app/shared/outbox` cuts each to one line per bean):

```java
@Bean("catalogOutboxStore")
public OutboxStore<CatalogOutboxEvent, Long> catalogOutboxStore(
        JpaContext context,
        @Value("${catalog.outbox.claim-strategy:skip-locked}") ClaimStrategyType claimStrategyType,
        @Qualifier("catalogSingletonJobExecutor") SingletonJobExecutor singletonJobExecutor) {
    EntityManager entityManager = context.getEntityManagerByManagedType(CatalogOutboxEvent.class);
    OutboxClaimStrategy strategy = OutboxClaimStrategies.of(claimStrategyType, singletonJobExecutor);
    return new JpaOutboxStore<>(entityManager, CatalogOutboxEvent.class, strategy);
}
```

### 6.4 Guarding against misconfiguration (minimal, no profile)

The one failure that is silent and worth catching is **`skip-locked` on a dialect that cannot skip locks**. There,
Hibernate drops the hint, which stays correct but serialises the workers. A single check at startup covers it:

```java
// inside SkipLockedClaimStrategy, called once from the module config
static void verify(EntityManagerFactory emf, String module) {
    Dialect dialect = emf.unwrap(SessionFactoryImplementor.class).getJdbcServices().getDialect();
    if (!dialect.supportsSkipLocked()) {
        throw new IllegalStateException(
                module + ".outbox.claim-strategy=skip-locked but " + dialect.getClass().getSimpleName()
                + " cannot skip locked rows; use conditional-update");
    }
}
```

This is not detection: it never *chooses* anything, it only refuses an invalid combination. Drop it if you want
zero coupling to Hibernate SPI. In that case the wrong combination shows up only as low throughput and in the
contention metric. Each module also logs one line at startup:
`outbox.locking module=catalog claimStrategy=skip-locked lockProvider=jdbc lockTable=catalog.shedlock`.

### 6.5 Which value to configure for each database

| Database | `claim-strategy` | `lock-provider` | Flyway location |
|---|---|---|---|
| PostgreSQL ≥ 9.5 | `skip-locked` | `jdbc` | `db/migration/postgresql/<module>` |
| MySQL ≥ 8.0 / MariaDB ≥ 10.6 | `skip-locked` | `jdbc` (`lock-table: <db>.shedlock`) | `db/migration/mysql/<module>` |
| MySQL 5.7 / MariaDB < 10.6 | `conditional-update` | `jdbc` | `db/migration/mysql/<module>` |
| Oracle | `skip-locked` | `jdbc` | `db/migration/oracle/<module>` |
| SQL Server | `skip-locked` | `jdbc` | `db/migration/sqlserver/<module>` |
| H2 (tests) | `conditional-update` | `jdbc` | `db/migration/h2/<module>` |
| Other / no row locks | `serial-leader` | `jdbc-app-time` or `redis` | vendor folder |

Flyway's `{vendor}` placeholder (`classpath:db/migration/{vendor}/catalog`) chooses the migration folder by
itself. Migrations are the one place that follows the database automatically, and that comes from Flyway, not
from our code.

### 6.6 Supporting concerns (unchanged by the strategy choice)

| Concern | Approach |
|---|---|
| Duplicate-key handling (inbox, `@EnsureRetryable`) | Rely on Spring's `DataIntegrityViolationException` / `DuplicateKeyException`, which already translates each vendor's error codes. Retrying in a **new** transaction is correct on every vendor |
| Outcome fencing | `lockForOutcome` default (`SELECT … FOR UPDATE`) is portable; no per-vendor variant needed |
| Time source for stale-claim recovery | App clock (current). Size `claimTimeout` well above the maximum clock skew between pods |

### 6.7 Package placement (respects R1/R11)

| Type | Location |
|---|---|
| `OutboxClaimStrategy`, `ClaimStrategyType`, `OutboxClaimStrategies`, implementations | `outbox-infrastructure` `…infrastructure.claim` |
| `LockProviderType`, `LockProviderSettings`, provider factory | `app` `shared/scheduling` (next to `ShedLockSingletonJobExecutor`) |
| `ModuleOutboxWiring` helper | `app` `shared/outbox` |
| Per-module beans | the existing `*ModuleOutboxConfig` classes |

### 6.8 Guardrails

- **ArchUnit:** `LockModeType.PESSIMISTIC_*`, the `jakarta.persistence.lock.timeout` hint and `executeUpdate` on
  outbox entities are allowed only in `..outbox.infrastructure.claim..`. `JdbcTemplateLockProvider` is allowed
  only in `..shared.scheduling..`. `AbstractOutboxProcessor` must not depend on any concrete `*ClaimStrategy`.
- **Contract tests:** an abstract `OutboxClaimStrategyContract` with four cases: no double claim with 2–4
  concurrent claimers, stale reclaim, fenced outcome after a takeover, and the hot-path `claimOne` race. Each
  strategy runs it on each Testcontainers database it is meant for. A `SingletonLockContract` covers one run per
  window and recovery after `lockAtMostFor`.
- **Metric:** `outbox.claim.contention{module,strategy}` (candidates selected vs. won), mainly for `conditional-update`.

---

## 7. Work breakdown

| Phase | Item | Detail | Effort |
|---|---|---|---|
| **A. Seam (no behaviour change)** | A1 | `OutboxClaimStrategy` + `ClaimContext`/`ClaimRequest`. Reshape `OutboxStore` to `claimBatch`/`claimOne`/`lockForOutcome` and update the `AbstractOutboxProcessor` call sites | M |
| | A2 | `SkipLockedClaimStrategy` (move the current `JpaOutboxStore` locking code) + `ClaimStrategyType` + `OutboxClaimStrategies` | S |
| | A3 | `claim-strategy` property per module (default `skip-locked`), `ModuleOutboxWiring` helper; migrate the 10 module configs + `WorkflowInfraConfig` | S |
| **B. Portable strategy** | B1 | `ConditionalUpdateClaimStrategy` with a shuffled candidate window + `outbox.claim.contention` metric | M |
| | B2 | Optional startup check `skip-locked` ⇄ dialect (§6.4) + startup log line | XS |
| | B3 | Optional `SerialLeaderClaimStrategy` | S |
| **C. Lock providers** | C1 | `LockProviderType` + `lock-provider` / `lock-table` properties; `jdbc` and `jdbc-app-time`; replace the `forSchema(...)` call sites | S |
| | C2 | Optional `redis` / `kubernetes` providers | S each |
| **D. Migrations** | D1 | Flyway `{vendor}` locations; port outbox / inbox / shedlock / publication-state DDL to MySQL + H2 | M |
| | D2 | Port remaining module schemas (incl. quoted `"order"` schema naming) | L |
| **E. Verification** | E1 | `OutboxClaimStrategyContract`, `SingletonLockContract`: `skip-locked` on PostgreSQL + MySQL 8 (CI); `conditional-update` on PostgreSQL, MySQL and H2; SQL Server / Oracle XE nightly | M |
| | E2 | ArchUnit guardrails from §6.8 | XS |
| | E3 | Load test: 3 replicas × each strategy on PostgreSQL; compare throughput and `claim.lost` | S |

Suggested order: **A1 → A2 → A3** (this alone ships safely: PostgreSQL behaviour is identical, it just adds the
seam) **→ E1 (PG) → B1 → C1 → E1 (MySQL/H2) → D1 → D2**.

---

### 7.1 Implementation status (2026-11-14)

| Item | Status | Where |
|---|---|---|
| A1 seam: `OutboxClaimStrategy`, `OutboxClaimContext`, `OutboxClaimRequest`; `OutboxStore.claimBatch` / `claimOne` replace `findBatchForProcessing` / `findByIdForUpdateSkipLocked`; `findByIdForUpdate` delegates to `lockForOutcome` | Done | `outbox-infrastructure/…/claim/`, `OutboxStore`, `JpaOutboxStore`, `AbstractOutboxProcessor` |
| A2 `SkipLockedClaimStrategy` (current behaviour, moved) + `OutboxClaimStrategies` (`Type` enum + `fromProperties`) | Done | `…/claim/` |
| A3 `<module>.outbox.claim-strategy` wired in all 9 module configs + `WorkflowInfraConfig` (`workflows.outbox.claim-strategy`) | Done | `*ModuleOutboxConfig` |
| B1 `ConditionalUpdateClaimStrategy` (shuffled candidate window ×4, one claim token per batch) | Done; contention metric not yet added | `…/claim/` |
| B2 startup check `outbox.claim-strategy-check: fail \| warn \| off` + `outbox.locking` log line | Done | `app/shared/outbox/OutboxClaimStrategyVerifier` |
| B3 `serial-leader` | Not started (optional) | |
| C1 `<module>.scheduling.lock-provider: jdbc \| jdbc-app-time` and `lock-table` | Done | `app/shared/scheduling/LockProviderType`, `LockProviderSettings`, `ShedLockSingletonJobExecutor.fromProperties` |
| C2 redis / kubernetes providers | Not started (optional) | |
| D1/D2 vendor migrations | Not started | |
| E1 contract suite: both strategies on PostgreSQL, `conditional-update` on H2 (6 cases each incl. 4-worker no-double-claim) | Done | `catalog-adapter-persistence/…/outbox/claim/` |
| E2 ArchUnit: `LockModeType` only in `…claim..` / `JpaOutboxStore`; concrete strategies only built by the factory | Done | `SchedulingArchitectureTest` |

`serial-leader` is not a configurable value yet, so the enum currently accepts `skip-locked` and `conditional-update` only.

---

## 8. Risks & open questions

| Risk | Mitigation |
|---|---|
| O2 contention collapses throughput with many replicas | Randomized window, smaller batches, contention metric; switch to O3 if `won/selected` < 50 % |
| Bulk JPQL updates bypass the persistence context | Run claim in its own short transaction; clear/refresh before dispatch (already a fresh transaction) |
| Clock skew causes premature reclaim | `claimTimeout` ≥ 10 × max skew; inbox absorbs the duplicate; alert on `claim.lost` |
| Wrong `claim-strategy` configured for the database | Enum binding rejects typos; optional §6.4 startup check rejects `skip-locked` on unsupported dialects; per-database cheat sheet (§6.5) |
| Vendor migration drift | Contract tests + schema validation (`hibernate.hbm2ddl.auto=validate`) in the Testcontainers matrix |
| Which databases are actually targeted? | **Decision needed** — the scope of D2/E1 depends on it (MySQL only vs. MySQL + SQL Server + Oracle) |

---

## 9. Acceptance criteria

- [ ] The same build passes the concurrency contract suite on PostgreSQL and MySQL 8 (plus any other targeted vendor).
- [ ] With `claim-strategy=conditional-update` on PostgreSQL, 3 replicas under load publish every row exactly once
      (excluding inbox-absorbed reclaims) and `outbox.claim.lost` ≈ 0.
- [ ] Each module's `claim-strategy` and `lock-provider` are explicit in config and logged at startup; `skip-locked`
      on a dialect without skip-locked support is rejected at startup (if the §6.4 check is enabled).
- [ ] No native queries; no lock hints outside the claim strategy classes (ArchUnit).
- [ ] Singleton jobs run once per `lockAtLeastFor` window on every targeted vendor.
