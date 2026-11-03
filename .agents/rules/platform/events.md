# R17. Events & Transactional Outbox

Load when adding domain events, integration events, outbox relays, or event listeners.

## Schema-Isolated Transactional Outbox Pattern

To eliminate distributed transactions across isolated database schemas, cross-domain state changes rely on the **Transactional Outbox Pattern**:
1. When an aggregate changes state within a local schema transaction, a domain/integration event record is inserted into the outbox table inside that same schema.
2. A schema-isolated outbox relay / poller reads pending events, publishes them as Spring integration events (or message broker events), and marks them published within the local schema.
3. Consuming domains listen asynchronously to these integration events and trigger local sagas or state projections.

## Event Dispatch & Listener Annotation Rules

The choice of listener annotation depends on the **event delivery mechanism and transaction lifecycle**, NOT the package folder:

| Event Source | Annotation | Rationale |
|---|---|---|
| **Outbox-dispatched events** (cross-module integration events, outbox domain events, workflow signals) | `@EventListener` | `AbstractOutboxProcessor` dispatches synchronously inside `publishTransactionTemplate`. `@EventListener` allows exceptions to be caught so the processor can mark the event as `FAILED` and schedule retries (guaranteeing at-least-once delivery). |
| **Direct in-memory events** (published inside an active DB transaction before commit, e.g. SSE UI notifications) | `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` | Defers execution until the active transaction commits, preventing phantom side effects or UI race conditions if the transaction rolls back. |

## MUST

- Place all cross-module integration events under `store/src/main/java/com/grab/store/shared/events/{module}/` (or `app/src/main/java/com/grab/app/shared/events/{module}/`) organized by module folder name (e.g., `com.grab.store.shared.events.catalog.*`, `com.grab.store.shared.events.identity.*`).
- For independent module startup, each module emits its authority manifest integration event asynchronously and non-blocking upon receiving `ApplicationReadyEvent`. Identity listens to these manifest events and registers them idempotently.
- Use `@EventListener` in event listener classes for all outbox-dispatched events (cross-module integration events, outbox domain events, and workflow signals).
- Use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` ONLY for direct in-memory events published during an active database transaction.
- Event listener classes cascade work by dispatching commands via the destination domain's group-scoped `CommandBus`.

## MUST NOT

- Block application startup or wait on asynchronous event acknowledgements (no two-way ACK sagas or thread-blocking wait loops at boot time).
- Introduce HTTP readiness filters returning 503 for startup metadata initialization.
- Use `@TransactionalEventListener` on outbox-dispatched events. It defers execution until AFTER `AbstractOutboxProcessor` commits `markPublished()`, silently breaking outbox error handling and retries.
- Call command handlers or write domain repositories directly from event listeners. Cascading work must go through the destination group's `CommandBus`.
