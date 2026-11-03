# Repository Specification & Core Invariants

> **Source of Truth**: Detailed topic rules reside in [.agents/rules/](.agents/rules/README.md).
> This document defines the **non-negotiable global architectural invariants**, the **Rule Index**, and the **Pre-Code Checklist (R21)**.

## Scope and Use

Follow every rule from `.agents/rules/` when generating, reviewing, or refactoring code. These rules override conflicting general coding conventions.
When implementing specific layers or components, inspect the dedicated rule in `.agents/rules/` or activate the relevant skill from `.agents/skills/`.

---

## Core Architectural Invariants

### 1. Hexagonal Layering & Dependency Direction
- **Pure Domain (`{bc}-domain`)**: Framework-agnostic. No Spring, JPA, or Jackson annotations. Contains aggregates, entities, value objects, domain events, domain policies, and outbound write repository ports (`port/outbound/{Domain}Repository`).
- **Application (`{bc}-application`)**: Inbound use case ports (`port/inbound/*UseCase`), use case services (`service/*Service`), outbound intra query ports (`port/outbound/*QueryPort`), application commands/queries/results, and read views (`model/read/*View`).
- **Persistence Adapter (`{bc}-adapter-persistence`)**: Strictly INTRA-module persistence. Outbound adapters (`*RepositoryAdapter`, `*QueryAdapter`), JPA entities, Spring Data JPA repositories, specifications, and mappers/assemblers (`mapper/`). Never contains cross-module calls or adapters.
- **Headless Domain Service Groups (`account`, `merchandising`, `channel`, `checkout`, `workflow`)**: Strictly headless domain cores. No REST controllers. Expose universal Java port interfaces (`port/message/*Port`) and inbound message adapters (`internal/adapter/inbound/message/*PortAdapter`) wired to group-scoped CQRS buses.
- **Platform BFFs (`sellercentral`, `marketplace`)**: Web and client assembly layers. Host REST controllers, request/response DTOs, mappers, HATEOAS assemblers, OpenAPI specifications, perimeter security, and error mapping, delegating to universal Java ports or BFF application orchestration services.
- **Dependency Flow**: `domain` <- `application` <- `adapter-persistence` / `app`. Inner layers NEVER depend on outer layers. Domain modules NEVER depend on Platform BFFs.

### 2. Strict CQRS Separation & Group-Scoped Isolation
- **Group-Scoped CQRS Buses**: Each service group operates isolated `CommandBus` and `QueryBus` beans (e.g. `@Qualifier("accountCommandBus")`, `@Qualifier("merchandisingCommandBus")`). Handlers register strictly with their own group bus.
- **CommandService / Inbound Write Adapter (Writes)**: Dispatches `Command<R>` via `CommandBus`. Handles write operations. MUST NOT inject or dispatch `QueryBus`, nor call `QueryService` or perform queries.
- **QueryService / Inbound Read Adapter (Reads)**: Dispatches `Query<R>` via `QueryBus`. Handles read operations. Strictly read-only; MUST NOT dispatch `CommandBus`, nor call `CommandService` or mutate state.
- **No Cross-Calling**: `CommandService` and `QueryService` (and write/read adapters) never inject or invoke each other.
- **Controllers**: Thin HTTP boundary in Platform BFFs only. Inject only Command/Query services or Java ports, and `ModelAssembler`. Controllers MUST NOT inject `CommandBus`, `QueryBus`, handlers, use cases, ports, or repositories directly.
- **Query Handlers**: Query use cases and query handlers MUST NOT inject domain write `*Repository` ports or load full aggregates. Reads go through `*QueryPort` returning view projections.

### 3. Transaction Boundaries & Multi-Schema Isolation
- Use group-specific transaction managers (`accountTransactionManager`, `merchandisingTransactionManager`, `channelTransactionManager`, `checkoutTransactionManager`) corresponding to isolated schema datasources.
- Use `@{Module}Transactional` for writes and `@{Module}ReadTransactional` for reads.
- Demarcate transactions on **CQRS Handlers** (`CommandHandler` / `QueryHandler`) or provider shared-interface / port adapters.
- **NEVER** put `@Transactional` on Controllers, Services, Mappers, Assemblers, or Policies.

### 4. Ports, Adapters, and Repositories
- Handlers and use cases inject **port interfaces only**, never concrete `*Adapter` or Spring Data `*JpaRepository` classes.
- **Universal Java Port Protocol (Tier 2)**: Headless domain services expose in-process Java Port Interfaces (`port.message.*Port`) with nested record types, implemented by thin inbound adapters (`internal.adapter.inbound.message.*PortAdapter`) delegating to group CQRS buses. Designed as zero-downtime drop-in replacements for future gRPC stubs.
- Persistence write adapter (`{Domain}RepositoryAdapter`) implements `{Domain}Repository`, injecting `{Domain}JpaRepository` + mapper/assembler, `DomainEventProducer`, and `PersistenceExecutor`.
- Persistence query adapter (`{Domain}QueryAdapter`) implements `{Domain}QueryPort`, returning application view records (`{Domain}View`), never domain aggregates or JPA entities.
- Controllers, services, mappers, assemblers, and policies MUST NOT touch repositories, ports, or adapters.
- Spring Data JPA repositories in persistence adapters MUST NOT use native queries (`@Query(nativeQuery = true)`). Use derived query methods, JPQL, or Criteria API Specifications instead.

### 5. Multi-Schema Database Isolation & Cross-Module Boundaries
- **Database Schema Isolation**: Dedicated PostgreSQL schema per service group (`account`, `catalog`, `channel`, `"order"`). Dedicated HikariCP pools with strict connection budgeting. Zero cross-schema foreign keys, zero cross-schema JPA joins.
- Cross-module communication is permitted ONLY via:
    1. **Universal Inbound Java Ports**: In-process synchronous calls between BFFs and domain groups or between domains (`port.message.*Port`).
    2. **Integration Events**: Published via schema-isolated transactional outbox relays and consumed asynchronously via `@EventListener`.
    3. **Shared Interface Ports**: Public provider interface in `store/.../{domain}/port/` with provider adapter in `store/.../{domain}/internal/api/adapter/` delegating strictly to inbound `*UseCase`.
- No module may directly import classes from another module's `internal/` packages.
- Cross-domain HATEOAS links use published `{Owner}ApiLinks` facade via `{owner}::api` named interfaces in Platform BFFs.

### 6. DTOs, Mappers, and Types
- All DTOs, Commands, Queries, and Results are immutable Java `record` types.
- Request DTOs in Platform BFFs use Jakarta Bean Validation (`@NotBlank`, `@Min`, etc.).
- Response DTOs contain primitives and `String` only. Never expose domain aggregates or JPA entities in responses.
- Entity identifiers use the framework `Id` type in domain/application layers, not raw `String`.
- **Single-Operation Mappers**: Exactly one MapStruct mapper class per Command / Query operation, annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`, with methods named strictly `toCommand(...)` / `toQuery(...)` and `toResponse(...)`.

---

## Rule Index

Refer to the source files in [.agents/rules/](.agents/rules/README.md) for detailed requirements:

| ID | Topic | Source Rule File |
|:---|:---|:---|
| **R1** | Module Structure & Dependencies | [.agents/rules/architecture/module-structure.md](.agents/rules/architecture/module-structure.md) |
| **R2** | Layered Architecture & CQRS-Light | [.agents/rules/architecture/layered-cqrs.md](.agents/rules/architecture/layered-cqrs.md) |
| **R3** | REST Controllers | [.agents/rules/inbound/controllers.md](.agents/rules/inbound/controllers.md) |
| **R4** | Command & Query Services | [.agents/rules/inbound/services.md](.agents/rules/inbound/services.md) |
| **R5** | MapStruct Mappers | [.agents/rules/inbound/mappers.md](.agents/rules/inbound/mappers.md) |
| **R6** | Command, Query, and Result Records | [.agents/rules/inbound/commands-queries.md](.agents/rules/inbound/commands-queries.md) |
| **R7** | CQRS Handlers | [.agents/rules/inbound/handlers.md](.agents/rules/inbound/handlers.md) |
| **R8** | Request & Response DTOs | [.agents/rules/inbound/dtos.md](.agents/rules/inbound/dtos.md) |
| **R9** | HATEOAS & Link Relations | [.agents/rules/inbound/hateoas.md](.agents/rules/inbound/hateoas.md) |
| **R10** | Outbound Shared Interface Ports & Universal Protocols | [.agents/rules/outbound/api-ports.md](.agents/rules/outbound/api-ports.md) |
| **R11** | Persistence Adapters & Multi-Schema JPA | [.agents/rules/outbound/persistence.md](.agents/rules/outbound/persistence.md) |
| **R12** | Infrastructure & Platform Adapters | [.agents/rules/outbound/infrastructure.md](.agents/rules/outbound/infrastructure.md) |
| **R13** | Domain Aggregates & Events | [.agents/rules/domain/aggregates.md](.agents/rules/domain/aggregates.md) |
| **R14** | Domain Policies | [.agents/rules/domain/policies.md](.agents/rules/domain/policies.md) |
| **R15** | Sealed Errors & Internationalization | [.agents/rules/platform/errors.md](.agents/rules/platform/errors.md) |
| **R16** | Transaction Demarcation | [.agents/rules/platform/transactions.md](.agents/rules/platform/transactions.md) |
| **R17** | Domain & Integration Events | [.agents/rules/platform/events.md](.agents/rules/platform/events.md) |
| **R18** | Structured Logging | [.agents/rules/platform/logging.md](.agents/rules/platform/logging.md) |
| **R19** | Naming Conventions | [.agents/rules/conventions/naming.md](.agents/rules/conventions/naming.md) |
| **R20** | Coding Style Guidelines | [.agents/rules/conventions/coding-style.md](.agents/rules/conventions/coding-style.md) |
| **R21** | Pre-Code Generation Checklist | [.agents/rules/README.md](.agents/rules/README.md) |
| **R22** | Testing Strategy, JPA Testcontainers & ArchUnit | [.agents/rules/testing/README.md](.agents/rules/testing/README.md) |

---

## On-Demand Skills

For multi-step implementation workflows, consult the corresponding skill in [.agents/rules/documentation/specification](.agents/rules/documentation/specification.md):
- **`specification`**: Authoring guides and templates for Domain Specs, Tech Specs, and Workflow Specs.

---

## Before Generating Code (R21 Checklist)

Verify all 27 checks before producing or changing code:

1. REST controllers reside strictly within Platform BFFs (`sellercentral`, `marketplace`) and delegate writes to `CommandService` / write ports and reads to `QueryService` / read ports. No inline logic, no direct bus, port, or repository injection. Domain services are strictly headless.
2. MapStruct mapper abstract class exists per Command/Query operation, annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`, with methods named strictly `toCommand(...)` / `toQuery(...)` and `toResponse(...)`.
3. Command/Query records use `Id` for identifiers, not `String`.
4. Handlers are `@Component` implementing `CommandHandler`/`QueryHandler` with group transactional annotations.
5. No business logic in controller, service, adapter, or mapper. Rules live in aggregates/policies.
6. Controller returns `ResponseEntity<EntityModel<T>>` or `ResponseEntity<PagedModel<EntityModel<T>>>`.
7. Model assemblers add HATEOAS links with correct kebab-case rel naming in Platform BFFs.
8. Every `PagedModel` exposes `list-{entities}` and `create-{entity}` when creation is available.
9. BFF DTOs are Java records in `dto/request/` and `dto/response/`. Port message DTOs are records nested inside the `*Port` interface.
10. Errors follow the sealed interface pattern with `ErrorCategory` and i18n codes.
11. Files live in the correct packages per module structure.
12. Non-`self` rel names are inline string literals. No `LinkRelations` class.
13. Platform BFFs host public API ingress routes (`/api/seller/...`, `/api/marketplace/...`), while domain services expose universal Java port interfaces.
14. Controllers inject assemblers directly and call `.toModel()` inline.
15. Logging in infra/app layers uses `Loggers.getLogger()`.
16. Intermediate variables are extracted. No nested function invocations.
17. No business logic in handlers. Aggregate or policy owns business rules.
18. A handler does not call another handler. Cascading work goes through `CommandBus`/`QueryBus`, usually from event listeners.
19. Only handlers and use cases inject/use ports and repositories. Never services, controllers, mappers, assemblers, or policies.
20. Write path: command handler/use case to domain write port `{Domain}Repository`. Query list/search to application query port `{Domain}QueryPort` (implemented by `{Domain}QueryAdapter`), not `JpaRepository`. Spring Data JPA repositories MUST NOT use native queries (`nativeQuery = true`).
21. Paged search uses a specification class injected into the query adapter. Results are application view records (`{Domain}View`), not JPA entities.
22. Cross-module integration events MUST be published via schema-isolated transactional outbox. Consuming modules listen via `@EventListener`.
23. Cross-module HATEOAS uses `{owner}::api` and `{Owner}ApiLinks` within Platform BFFs. Consumers do not import owner `internal/` controllers. Same rel names as the owning root. No URL hardcoding.
24. `shared` remains `@ApplicationModule(type = OPEN)`.
25. Strict CQRS isolation: `CommandService` and `QueryService` (and write/read adapters) never mess with each other or cross-call. `CommandService` NEVER injects or invokes `QueryBus` or queries data. `QueryService` NEVER injects or invokes `CommandBus` or mutates state. Group buses are strictly scoped.
26. Three-tier communication protocol: Tier 1 (Platform BFF REST ingress), Tier 2 (Universal Inbound Java Port Interfaces `port.message.*Port` with nested records and thin `*PortAdapter`s mapping to group CQRS buses), Tier 3 (Asynchronous Outbox Event Choreography). Persistence adapters are strictly intra-domain.
27. Multi-schema database isolation (dedicated schema & HikariCP pool per service group, zero cross-schema joins/FKs). JPA repository and adapter tests MUST use Testcontainers with the target database container. Architecture tests in ArchUnit MUST pass without violating layer boundaries, CQRS bus scoping, headless domain rules, or native query prohibitions.
