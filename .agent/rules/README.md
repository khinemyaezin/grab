# Architectural Rules

Follow every rule in this directory when generating, reviewing, or refactoring code. These rules override conflicting general coding conventions.

Load `README.md` first. Then load only the category that matches the files you touch. Load related categories when a change crosses a boundary (for example handler + domain + persistence).

Do not invent diagrams. Do not add pictures. Use the MUST / MUST NOT lists as the source of truth.

## Categories

| Category | Path | Load when |
|---|---|---|
| Architecture | `architecture/` | Module layout, dependencies, layers, CQRS flow |
| Inbound | `inbound/` | REST controllers, services, mappers, commands, queries, handlers, DTOs, HATEOAS |
| Outbound | `outbound/` | Shared API query ports & adapters, persistence adapters, JPA, infrastructure |
| Domain | `domain/` | Aggregates, domain events, policies |
| Platform | `platform/` | Errors, transactions, events, logging |
| Conventions | `conventions/` | Naming, coding style |

## Rule index

| ID | File |
|---|---|
| R1 | `architecture/module-structure.md` |
| R2 | `architecture/layered-cqrs.md` |
| R3 | `inbound/controllers.md` |
| R4 | `inbound/services.md` |
| R5 | `inbound/mappers.md` |
| R6 | `inbound/commands-queries.md` |
| R7 | `inbound/handlers.md` |
| R8 | `inbound/dtos.md` |
| R9 | `inbound/hateoas.md` |
| R10 | `outbound/api-ports.md` |
| R11 | `outbound/persistence.md` |
| R12 | `outbound/infrastructure.md` |
| R13 | `domain/aggregates.md` |
| R14 | `domain/policies.md` |
| R15 | `platform/errors.md` |
| R16 | `platform/transactions.md` |
| R17 | `platform/events.md` |
| R18 | `platform/logging.md` |
| R19 | `conventions/naming.md` |
| R20 | `conventions/coding-style.md` |
| R21 | this file, section "Before generating code" |

## Before generating code

Verify all of the following before producing or changing code.

1. Controller delegates writes to CommandService and reads to QueryService. No inline logic, no direct bus, port, or repository injection.
2. MapStruct mapper abstract class exists per Command/Query operation, annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
3. Command/Query records use `Id` for identifiers, not `String`.
4. Handlers are `@Component` implementing `CommandHandler`/`QueryHandler` with the module transactional annotations.
5. No business logic in controller, service, or mapper. Rules live in aggregates/policies.
6. Controller returns `EntityModel<T>` or `PagedModel<EntityModel<T>>`.
7. Model assemblers add HATEOAS links with correct rel naming (entity-level or per-operation).
8. Every `PagedModel` exposes `list-{entities}` and `create-{entity}` when creation is available.
9. DTOs are Java records in `dto/request/` and `dto/response/`.
10. Errors follow the sealed interface pattern with `ErrorCategory` and i18n codes.
11. Files live in the correct packages per module structure.
12. Non-`self` rel names are inline string literals. No `LinkRelations` class.
13. New bounded contexts have a Tier 2 root endpoint and are linked from `ApiRootController`.
14. Controllers inject assemblers directly and call `.toModel()` inline.
15. Logging in infra/app layers uses `Loggers.getLogger()`.
16. Intermediate variables are extracted. No nested function invocations.
17. No business logic in handlers. Aggregate or policy owns business rules.
18. A handler does not call another handler. Cascading work goes through `CommandBus`/`QueryBus`, usually from event listeners.
19. Only handlers and use cases inject/use ports and repositories. Never services, controllers, mappers, assemblers, or policies.
20. Write path: command handler/use case to domain write port `{Domain}Repository`. Query list/search to application query port `{Domain}QueryPort` (implemented by `{Domain}QueryAdapter`), not `JpaRepository`.
21. Paged search uses a specification class injected into the query adapter. Results are application view records (`{Domain}View`), not JPA entities.
22. Cross-module events use named interfaces (`{module}::events`). Consuming modules list them in `allowedDependencies`.
23. Cross-module HATEOAS uses `{owner}::api` and `{Owner}ApiLinks`. Consumers do not import owner `internal/` controllers. Same rel names as the owning root. No URL hardcoding. No proxying owner list/search.
24. `shared` remains `@ApplicationModule(type = OPEN)`.
25. Strict CQRS isolation: CommandService and QueryService never mess with each other or cross-call. CommandService NEVER injects or invokes QueryBus or queries data. QueryService NEVER injects or invokes CommandBus or mutates state.
26. Outbound API exposure via shared interface ports (instead of gRPC): provider public interface lives in `store/.../{domain}/port/` with DTOs declared inside the interface, provider adapter lives in `store/.../{domain}/internal/api/adapter/{port}Adapter` delegating only to `*UseCase` with transaction starting on the adapter method, and mapper lives in `store/.../{domain}/internal/api/adapter/mapper/{PortName}Mapper`. Consuming modules define an outbound port in `{consumer}-application/.../port/outbound/{TargetDomain}{Capability}Port` and implement it via an adapter in `store/.../{consumer}/internal/adapter/{TargetDomain}{Capability}Adapter`. Persistence adapters are strictly intra-domain.
