# R7. Handlers

Load when creating or changing command/query handlers, or deciding who may use ports and repositories.

## MUST

- CommandHandler: `@Component` + `@RequiredArgsConstructor`, implements `CommandHandler<C,R>`. Use `@{Group}Transactional` (or `@{Module}Transactional`) for writes.
- QueryHandler: `@Component` + `@RequiredArgsConstructor`, implements `QueryHandler<Q,R>`. Use `@{Group}ReadTransactional` (or `@{Module}ReadTransactional`) for reads.
- Register with the corresponding group-scoped CQRS bus (`@Qualifier("{group}CommandBus")` / `@Qualifier("{group}QueryBus")`).
- Implement both `handle(...)` and `getCommandType()` / `getQueryType()`.
- Act as CQRS adapters: demarcate the transaction boundary, adapt Command/Query records, and delegate execution to the appropriate inbound `*UseCase` port (or orchestrate ports directly in lite bounded contexts).
- Command handlers / use cases inject domain write port `{Domain}Repository`.
- Query handlers / use cases for list/search/paged reads inject application query port `{Domain}QueryPort`. See `outbound/persistence.md`.
- Cascade work by dispatching another command/query via the group-scoped `CommandBus` / `QueryBus` (typically from an event listener), or by calling policies and ports directly within the handler orchestration.

## MUST NOT

- Embed business rules inline in the handler. Business rules belong in domain aggregates, domain policies, or use case services.
- Call another handler directly.
- Register handlers with a CQRS bus belonging to a different service group.
- Inject Spring Data `*JpaRepository` or concrete `*Adapter` classes directly. Handlers/use cases inject port interfaces only.
- Let controllers, services, mappers, assemblers, or policies inject or call ports, repositories, or adapters.
- Let event listeners call handlers or repositories directly. Listeners cascade via `CommandBus`/`QueryBus`.
- Let named-interface adapters or port adapters inject application outbound `*QueryPort` or dispatch `QueryBus` directly. They MUST call inbound `*UseCase.execute(...)` and map results.
