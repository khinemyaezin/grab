# R14. Events

Load when adding domain events, listeners, or cross-module integration.

## MUST

- Intra-module: `@TransactionalEventListener` in `store/.../event/`.
- Cross-module: transactional outbox via `outbox-infrastructure` (at-least-once delivery), exposed through Modulith named interfaces. See `architecture/module-structure.md`.
- Event listener classes dispatch cascading commands via `CommandBus`.

## MUST NOT

- Call handlers or repositories directly from event listeners.
