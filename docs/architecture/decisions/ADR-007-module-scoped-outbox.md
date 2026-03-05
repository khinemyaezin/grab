# ADR-007: Module-Scoped Transactional Outbox

## Status
Accepted (implemented on March 5, 2026)

## Context

The current codebase is a modular monolith with explicit module boundaries for:

- each module
- shared application wiring in `application` root

Each module already has its own persistence wiring:

- `DataSource`
- `EntityManagerFactory`
- `PlatformTransactionManager`

The current repository implementations publish domain events directly through
`ApplicationEventPublisher` after saving aggregates. That approach is fast, but
it is not durable:

- published events are lost if the process crashes after commit and before
  delivery completes
- delivery cannot be retried independently of the write transaction
- event publication is not clearly owned by the module's persistence boundary

The system also needs to support module-owned persistence. In practice, that
means a module must be able to keep its business tables and event publication
state together, even when modules eventually move to different schemas or
different physical databases.

Spring Modulith remains useful for:

- module boundaries
- application module verification
- listener programming model

However, for this repository we do not want outbox persistence to depend on a
single application-wide event publication store. The design needs to follow the
same module-scoped persistence boundary as the rest of the system.

---

## Decision

Adopt a **module-scoped transactional outbox**.

Each module will:

- persist domain events into its own outbox table in the same transaction as
  aggregate changes
- own the outbox entity, repository, and transaction manager bindings in its
  infrastructure module
- run one logical outbox processor that polls, claims, publishes, retries, and
  marks records as delivered

Shared contracts live in `framework/outbox`. Spring/JPA implementation details
live in the reusable `outbox-infrastructure` module. Outbox data still
belongs to the module that produced it.

If modules share the same physical database, they should still use
separate module-owned tables. Preferred layout:

- separate schemas when practical, for example `{module-schemas-name}.outbox_event`. 
- otherwise separate table names such as `{module-schemas-name}_outbox_event`.

See the design diagram in
[module-scoped-outbox.md](../diagrams/module-scoped-outbox.md).

---

## Alternatives Considered

### Option A: Shared Outbox Table

One table stores every unpublished event for all modules, typically with a
`module_name` column.

Example shape:

- `shared_outbox_event`
- columns for module name, event type, payload, status, retry metadata

#### Advantages

- simplest schema to introduce initially
- one poller can process all records
- one cleanup strategy and one dashboard
- easier ad-hoc querying for the whole application

#### Disadvantages

- weakens module ownership because one persistence artifact is shared across
  otherwise separate modules
- makes event persistence depend on one shared table contract and one shared
  migration path
- becomes awkward when modules use different transaction managers or later move
  to different physical databases
- increases contention and noisy-neighbor risk as all modules write to the same
  queue table
- makes retention, indexing, and replay policy global even when module needs
  differ
- pushes the design back toward a shared persistence center, which conflicts
  with the repository's current module-scoped data source design

#### Assessment For This Repository

This option is acceptable only if the application intentionally keeps one shared
database contract for the foreseeable future and treats the outbox as platform
infrastructure rather than module-owned state.

That is not the direction of this repository. Existing architecture decisions
already prefer module-scoped persistence boundaries.

### Option B: Per-Module Outbox Table

Each module stores its own unpublished events in its own table and processes
them through its own persistence unit.

Example shape:

- `{module-schema-name}.outbox_event`

#### Advantages

- preserves the same ownership boundary as the module's aggregate tables
- guarantees atomic writes using the module's own transaction manager
- works whether modules share one physical database, separate schemas, or fully
  separate databases
- isolates retention, retry, indexing, and operational tuning per module
- reduces coupling between module migrations
- keeps a clean path toward future service extraction

#### Disadvantages

- requires more Spring wiring than a single shared table
- introduces one logical poller per module
- needs shared framework abstractions to avoid repeating the same processor code
- observability becomes a roll-up concern instead of querying one table

#### Assessment For This Repository

This option fits the current architecture best. The repository already pays the
cost of explicit module persistence wiring, so the outbox should follow the
same rule instead of reintroducing a shared persistence hotspot.

---

## Comparison Summary

| Dimension | Shared outbox table | Per-module outbox table |
| --- | --- | --- |
| Transaction ownership | Shared infrastructure concern | Owned by producing module |
| Fit with current datasources and transaction managers | Weak | Strong |
| Future separate database support | Poor | Strong |
| Schema and migration isolation | Low | High |
| Runtime contention isolation | Low | High |
| Initial implementation effort | Lower | Higher |
| Operational simplicity on day one | Higher | Moderate |
| Long-term modularity | Weak | Strong |

---

## Proposed Design

### 1. Framework contracts (`framework/outbox`)

Keep framework contracts Spring-free so they can move with domain code during
future extraction:

- `OutboxEntry`
- `OutboxEventSerializer`
- `OutboxEventDispatcher`
- `OutboxStatus`
- `SerializedEvent`
- `ClaimedOutboxEvent`

### 2. Shared Spring adapter (`infrastructure-outbox-spring`)

Centralize reusable Spring/JPA outbox mechanics in one infra module:

- `OutboxStore`
- `JpaOutboxStore`
- `OutboxRowFactory`
- `JpaOutboxDomainEventProducer`
- `AbstractOutboxProcessor`

This keeps catalog/inventory infrastructure thin while avoiding duplicated
processor and producer implementations.

### 3. Module-owned infrastructure wrappers

Each module still owns:

- outbox entity/table shape
- module-specific scheduler wrapper
- module transaction manager and persistence wiring

Example ownership split:

- `{module_name}-infrastructure/.../outbox/*`

### 4. Write path

When an aggregate is saved:

1. persist aggregate state
2. drain domain events from the aggregate
3. serialize each event into module outbox rows
4. commit aggregate and outbox rows in the same module transaction

This replaces direct repository-time publication through
`ApplicationEventPublisher`.

### 5. Read and dispatch path

Each module has one logical outbox processor that:

1. polls unpublished rows in batches
2. claims rows using database locking or a lease field
3. deserializes the payload
4. dispatches the event
5. marks the row as delivered, or stores retry metadata on failure

The processor may publish:

- in-process through `ApplicationEventPublisher`
- to an external broker later through a different `OutboxDispatcher`

### 6. Scheduler model

Use one logical processor per module, but not one completely separate
framework per module.

Recommended shape:

- one processor wrapper bean per module (`@Scheduled`)
- one shared processor implementation (`AbstractOutboxProcessor`)
- each wrapper configured with the module's `OutboxStore`,
  transaction manager, batch size, retry policy, and dispatcher

This preserves module ownership while avoiding duplicated scheduling code.

### 7. Table shape

Minimum outbox fields:

- `id`
- `aggregate_type`
- `aggregate_id`
- `event_type`
- `event_version`
- `payload`
- `headers`
- `occurred_at`
- `available_at`
- `claimed_at`
- `claim_token`
- `published_at`
- `status`
- `attempt_count`
- `last_error`

Recommended indexes:

- `(status, available_at)`
- `(claimed_at)`
- `(aggregate_type, aggregate_id)`
- `(published_at)` for retention cleanup

### 8. Delivery semantics

The design provides at-least-once delivery.

Consumers must therefore be idempotent. If a consumer updates another module's
state and cannot safely re-run, that consumer should persist a processed-event
record or inbox entry on its own side.

### 9. Microservice migration guardrails

- Keep framework contracts independent of Spring so service extraction does not
  force contract rewrites.
- Keep event envelope fields (`event_type`, `event_version`, `headers`,
  `payload`) stable and backward-compatible.
- Treat serializer choice as a replaceable adapter. Current Java serialization
  works in monolith runtime; broker-based service extraction should switch to a
  schema-based format (for example JSON/Avro/Protobuf) via
  `OutboxEventSerializer`.
- Treat dispatcher choice as a replaceable adapter.
  `ApplicationEventPublisher` is monolith-local; extracted services should use
  a broker-backed `OutboxEventDispatcher`.
- Preserve at-least-once semantics and idempotent consumers after extraction.

---

## Consequences

### Positive

- durable event delivery aligned with module transactions
- no shared outbox bottleneck across modules
- clear ownership for schema, retention, and retry policy
- compatible with the repository's current explicit module persistence model
- cleaner path to future extraction of catalog or inventory into separate
  services

### Negative

- more beans, entities, and repositories to wire
- multiple outbox tables to monitor
- cleanup and dashboards need roll-up reporting instead of one central table
- local development needs fixtures or migrations for each module outbox

### Mitigations

- keep contracts in `framework/outbox` and reusable Spring/JPA mechanics in
  `outbox-infrastructure-outbox`
- standardize table columns and naming conventions across modules
- expose common metrics for backlog size, retry count, and publish latency
- keep one operational runbook for all processors even though storage is
  module-owned

---

## Migration Notes

Suggested rollout:

1. add module outbox tables and repositories
2. replace direct `DomainEventProducer` publication with outbox persistence
3. add per-module processors that republish events in-process
4. add retry, cleanup, and metrics
5. only then consider external broker publication

During rollout, avoid a hybrid design where some events are published directly
and some are written to the outbox from the same aggregate save path.

For service extraction:

1. keep module outbox table and processor inside the extracted service
2. replace in-process dispatcher with broker-backed dispatcher
3. replace Java serialization with a service-safe wire format
4. keep idempotency checks on consuming side
