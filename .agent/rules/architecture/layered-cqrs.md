# R2. Layered Architecture and CQRS-Light

Load when adding or changing an HTTP use case, command/query flow, or layer responsibility.

## Layers

| Layer | Location | Responsibility |
|---|---|---|
| Controller | `store/.../api/rest/controller/` | HTTP in/out. Delegates to services. Returns `ResponseEntity<EntityModel<T>>` or `ResponseEntity<PagedModel<EntityModel<T>>>`. No business logic. |
| Service | `store/.../api/rest/service/` | Orchestrates DTO to Command/Query mapping, dispatches via bus, maps result to DTO. No repository access. No business rules. |
| Mapper | `store/.../api/rest/mapper/` | MapStruct abstract class: DTO to Command/Query/Result. |
| Handler | `store/.../command/handler/` or `store/.../query/handler/` | Owns the transaction boundary. Command handlers use domain `{Domain}Repository`. Query handlers use `{Domain}QueryRepository` for reads/search. Delegates business rules to aggregates/policies, returns Result. `@Component`. |
| Policy | `{name}-domain/.../policy/` or `store/.../policy/` | Encodes business rules. See `domain/policies.md`. |
| Domain | `{name}-domain/` | Pure domain. Framework-agnostic. |
| Infrastructure | `{name}-infrastructure/` | JPA and persistence concerns. |

## CQRS data flow

1. Controller -> Service -> Mapper(to Command) -> CommandBus.dispatch -> Handler.handle -> policy/aggregate -> repository.save -> Result
2. Controller -> Service -> Mapper(to Query) -> QueryBus.dispatch -> Handler.handle -> repository.findById -> Result
3. Paginated queries: Mapper produces a record implementing `Query<Page<Result>>` and `PageableQueryRequest`. Handler returns `Page<Result>`. Service maps `resultPage.map(mapper::toResponse)`.

## CQRS Separation & Isolation Rules

- **Strict Pipeline Independence**: The Command pipeline (write) and Query pipeline (read) are strictly isolated. They must not mess with each other.
- **CommandService Cannot Query**: `CommandService` MUST NOT inject, invoke, or dispatch via `QueryBus`, nor call `QueryService`. It handles writes only. Any aggregate state required for command processing must be retrieved inside the `CommandHandler` via its domain repository, or provided via the request DTO.
- **QueryService Cannot Mutate**: `QueryService` MUST NOT inject, invoke, or dispatch via `CommandBus`, nor call `CommandService`. It is strictly read-only and side-effect free.
- **No Cross-Service Invocations**: `CommandService` and `QueryService` must never inject or call one another.
- **Controller Delegation**: Controllers bridge HTTP to services by delegating write operations (POST, PUT, PATCH, DELETE) to `CommandService` and read operations (GET) to `QueryService`. Controllers MUST NOT inject `CommandBus`, `QueryBus`, handlers, or repositories directly.
