# R13. Domain Aggregates

Load when changing domain models, invariants, or domain events.

## MUST

- Stay framework-agnostic. No Spring, JPA, or MapStruct annotations.
- Route all state mutations through aggregate methods that enforce invariants.
- Accumulate domain events via `addEvent()`, pull via `pullEvents()`.
- Keep aggregates non-anemic. Invariants live on the aggregate.
- Put reusable / cross-cutting decision rules in domain policies, or domain services when stateful coordination is required.
- Handlers orchestrate aggregates + policies inside a transaction.

## MUST NOT

- Put business rules in handlers.
