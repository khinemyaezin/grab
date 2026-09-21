# R17. Events

Load when adding domain events, listeners, or cross-module integration.

## Event Dispatch & Listener Annotation Rules

The choice of listener annotation depends on the **event delivery mechanism and transaction lifecycle**, NOT the package folder:

| Event Source | Annotation | Rationale |
|---|---|---|
| **Outbox-dispatched events** (cross-module integration events, outbox domain events, workflow signals) | `@EventListener` | `AbstractOutboxProcessor` dispatches synchronously inside `publishTransactionTemplate`. `@EventListener` allows exceptions to be caught so the processor can mark the event as `FAILED` and schedule retries (guaranteeing at-least-once delivery). |
| **Direct in-memory events** (published inside an active DB transaction before commit, e.g. SSE UI notifications) | `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` | Defers execution until the active transaction commits, preventing phantom side effects or UI race conditions if the transaction rolls back. |

## MUST

- Use `@EventListener` in `store/.../event/` for all outbox-dispatched events (cross-module integration events, outbox domain events, and workflow signals).
- Use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` ONLY for direct in-memory events published during an active database transaction (e.g. `WorkflowTerminalUiEventListener`).
- Cross-module communication: transactional outbox via `outbox-infrastructure` (at-least-once delivery), exposed through Modulith named interfaces. See `architecture/module-structure.md`.
- Event listener classes dispatch cascading commands via `CommandBus`.

## MUST NOT

- Use `@TransactionalEventListener` on outbox-dispatched events. It defers execution until AFTER `AbstractOutboxProcessor` commits `markPublished()`, silently breaking outbox error handling and retries.
- Call command handlers or write domain repositories directly from event listeners.
