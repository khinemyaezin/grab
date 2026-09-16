# R7. Handlers

Load when creating or changing command/query handlers, or deciding who may use repositories.

## MUST

- CommandHandler: `@Component` + `@RequiredArgsConstructor`, implements `CommandHandler<C,R>`. Use `@{Module}Transactional` for writes.
- QueryHandler: `@Component` + `@RequiredArgsConstructor`, implements `QueryHandler<Q,R>`. Use `@{Module}ReadTransactional` for reads.
- Implement both `handle(...)` and `getCommandType()` / `getQueryType()`.
- Own the transaction and only that orchestration: load aggregates, invoke policies/aggregate methods, persist, map to Result.
- Command handlers inject domain `{Domain}Repository`.
- Query handlers for list/search/paged reads inject `{Domain}QueryRepository`. See `infrastructure/persistence.md`.
- Cascade work by dispatching another command/query via `CommandBus` / `QueryBus` (typically from an event listener), or by calling domain/application policies and repositories directly within the same handler.

## MUST NOT

- Embed business rules inline in the handler.
- Call another handler.
- Inject Spring Data `*JpaRepository` directly.
- Let controllers, services, mappers, assemblers, or policies inject or call repositories.
- Let event listeners call handlers or repositories. Listeners cascade via `CommandBus`/`QueryBus`.
