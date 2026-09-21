# R2. Layered Architecture and CQRS-Light

Load when adding or changing an HTTP use case, command/query flow, or layer responsibility.

## Layers

| Layer | Location | Responsibility |
|---|---|---|
| Controller | `store/.../api/rest/controller/` | HTTP in/out. Delegates to services. Returns `ResponseEntity<EntityModel<T>>` or `ResponseEntity<PagedModel<EntityModel<T>>>`. No business logic. |
| Service | `store/.../api/rest/service/` | Orchestrates DTO to Command/Query mapping, dispatches via bus, maps result to DTO. No repository access. No business rules. |
| Mapper | `store/.../api/rest/mapper/` | MapStruct abstract class: DTO to Command/Query/Result. |
| Handler | `store/.../{module}/internal/command/handler/` and `query/handler/` (full hex) | CQRS adapter: `CommandHandler` / `QueryHandler` + `@{Bc}Transactional` / `@{Bc}ReadTransactional`; delegates to `*UseCase.execute(...)`. REST and sagas use `CommandBus` / `QueryBus` only. |
| Use case | `{bc}-application/port/inbound/*UseCase` + `{bc}-application/service/*Service` | Spring-free orchestration (no `@Component` / `@Transactional`). Command/Query/Result in application implement framework `Command` / `Query`. **Exception:** Spring Data `Page` / `Pageable` on search ports until later mapping. Wired via `{Bc}UseCaseConfig` in `store`. |
| Policy | `{name}-domain/.../policy/` or `store/.../policy/` | Encodes business rules. See `domain/policies.md`. |
| Domain | `{name}-domain/` | Pure domain. Framework-agnostic. Write ports: `domain/port/outbound`. |
| Application | `{name}-application/` | Commands, queries, inbound/outbound ports, read models, use case services (full hex). |
| Persistence adapter | `{bc}-adapter-persistence` | JPA, outbox, `*Adapter` port impls, `{Bc}PersistenceConfig`. Package `com.{bc}.adapter.persistence`. |

Cart is full hex like catalog and sales-channel (`cart-domain`, `cart-application`, `cart-adapter-persistence`, thin handlers in `store`).

## CQRS data flow

1. Controller -> Service -> Mapper(to Command) -> CommandBus.dispatch -> **Handler.handle** -> **UseCase.execute** -> domain/ports -> Result
2. Controller -> Service -> Mapper(to Query) -> QueryBus.dispatch -> **Handler.handle** -> **UseCase.execute** -> query ports -> Result
3. Paginated queries: Mapper produces a record implementing `Query<Page<Result>>` and `PageableQueryRequest`. Handler returns `Page<Result>`. Service maps `resultPage.map(mapper::toResponse)`.

## CQRS Separation & Isolation Rules

- **Strict Pipeline Independence**: The Command pipeline (write) and Query pipeline (read) are strictly isolated. They must not mess with each other.
- **CommandService Cannot Query**: `CommandService` MUST NOT inject, invoke, or dispatch via `QueryBus`, nor call `QueryService`. It handles writes only. Any aggregate state required for command processing must be retrieved inside the **use case** via its domain repository, or provided via the request DTO.
- **QueryService Cannot Mutate**: `QueryService` MUST NOT inject, invoke, or dispatch via `CommandBus`, nor call `CommandService`. It is strictly read-only and side-effect free.
- **No Cross-Service Invocations**: `CommandService` and `QueryService` must never inject or call one another.
- **Controller Delegation**: Controllers bridge HTTP to services by delegating write operations (POST, PUT, PATCH, DELETE) to `CommandService` and read operations (GET) to `QueryService`. Controllers MUST NOT inject `CommandBus`, `QueryBus`, handlers, use cases, ports, or adapters directly.
- **Query handlers read via query ports**: Query use cases and `QueryHandler` classes MUST NOT inject domain write `*Repository` ports or call domain services that load aggregates. Reads go through `*QueryPort` (or lite BC query ports in `{bc}-domain.port.outbound`) returning views/projections.
- **Modulith `{module}::query` named interfaces**: Adapters under `store/.../{module}/internal/.../query/` (or `internal/api/query/` or `internal/api/adapter/`) that implement published `{module}::query` ports MUST delegate to inbound `*UseCase` only. They MUST NOT inject application outbound `*QueryPort` or dispatch `QueryBus`. Passthrough use cases are OK. Future gRPC/HTTP servers call the same use cases. Cross-module consumers (cart, storefront projectors) stay thin mappers on the named interface; one business question = one named-interface method. See `outbound/api-ports.md`.
