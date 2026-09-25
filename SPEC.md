# Repository Specification

> Consolidated from [`.agent/rules/`](.agent/rules/README.md). The source files are retained; this document collects every indexed rule and the pre-code checklist in one place.

## Scope and use

Follow every rule from `.agent/rules/` when generating, reviewing, or refactoring code. These rules override conflicting general coding conventions.

When using the source files, load `README.md` first, then the category that matches the files you touch. Load related categories when a change crosses a boundary (for example handler + domain + persistence).

Do not invent diagrams. Do not add pictures. Use the MUST / MUST NOT lists as the source of truth.

## Contents

- [Architecture](#architecture)
  - [R1. Module Structure and Dependencies](#r1-module-structure-and-dependencies)
  - [R2. Layered Architecture and CQRS-Light](#r2-layered-architecture-and-cqrs-light)
- [Inbound](#inbound)
  - [R3. Controllers](#r3-controllers)
  - [R4. Services](#r4-services)
  - [R5. Mappers](#r5-mappers)
  - [R6. Command, Query, and Result Records](#r6-command-query-and-result-records)
  - [R7. Handlers](#r7-handlers)
  - [R8. DTOs](#r8-dtos)
  - [R9. HATEOAS](#r9-hateoas)
- [Outbound](#outbound)
  - [R10. Outbound API Rules (Shared Interface Ports)](#r10-outbound-api-rules-shared-interface-ports)
  - [R11. Persistence Adapters](#r11-persistence-adapters)
  - [R12. Outbound Infrastructure & Platform Adapters](#r12-outbound-infrastructure--platform-adapters)
- [Domain](#domain)
  - [R13. Domain Aggregates](#r13-domain-aggregates)
  - [R14. Policies](#r14-policies)
- [Platform](#platform)
  - [R15. Errors](#r15-errors)
  - [R16. Transactions](#r16-transactions)
  - [R17. Events](#r17-events)
  - [R18. Logging](#r18-logging)
- [Conventions](#conventions)
  - [R19. Naming](#r19-naming)
  - [R20. Coding Style](#r20-coding-style)
- [Before generating code (R21)](#before-generating-code-r21)
- [Source review](#source-review)

## Rule index

| ID | Rule | Source |
|---|---|---|
| R1 | [R1. Module Structure and Dependencies](#r1-module-structure-and-dependencies) | [`.agent/rules/architecture/module-structure.md`](.agent/rules/architecture/module-structure.md) |
| R2 | [R2. Layered Architecture and CQRS-Light](#r2-layered-architecture-and-cqrs-light) | [`.agent/rules/architecture/layered-cqrs.md`](.agent/rules/architecture/layered-cqrs.md) |
| R3 | [R3. Controllers](#r3-controllers) | [`.agent/rules/inbound/controllers.md`](.agent/rules/inbound/controllers.md) |
| R4 | [R4. Services](#r4-services) | [`.agent/rules/inbound/services.md`](.agent/rules/inbound/services.md) |
| R5 | [R5. Mappers](#r5-mappers) | [`.agent/rules/inbound/mappers.md`](.agent/rules/inbound/mappers.md) |
| R6 | [R6. Command, Query, and Result Records](#r6-command-query-and-result-records) | [`.agent/rules/inbound/commands-queries.md`](.agent/rules/inbound/commands-queries.md) |
| R7 | [R7. Handlers](#r7-handlers) | [`.agent/rules/inbound/handlers.md`](.agent/rules/inbound/handlers.md) |
| R8 | [R8. DTOs](#r8-dtos) | [`.agent/rules/inbound/dtos.md`](.agent/rules/inbound/dtos.md) |
| R9 | [R9. HATEOAS](#r9-hateoas) | [`.agent/rules/inbound/hateoas.md`](.agent/rules/inbound/hateoas.md) |
| R10 | [R10. Outbound API Rules (Shared Interface Ports)](#r10-outbound-api-rules-shared-interface-ports) | [`.agent/rules/outbound/api-ports.md`](.agent/rules/outbound/api-ports.md) |
| R11 | [R11. Persistence Adapters](#r11-persistence-adapters) | [`.agent/rules/outbound/persistence.md`](.agent/rules/outbound/persistence.md) |
| R12 | [R12. Outbound Infrastructure & Platform Adapters](#r12-outbound-infrastructure--platform-adapters) | [`.agent/rules/outbound/infrastructure.md`](.agent/rules/outbound/infrastructure.md) |
| R13 | [R13. Domain Aggregates](#r13-domain-aggregates) | [`.agent/rules/domain/aggregates.md`](.agent/rules/domain/aggregates.md) |
| R14 | [R14. Policies](#r14-policies) | [`.agent/rules/domain/policies.md`](.agent/rules/domain/policies.md) |
| R15 | [R15. Errors](#r15-errors) | [`.agent/rules/platform/errors.md`](.agent/rules/platform/errors.md) |
| R16 | [R16. Transactions](#r16-transactions) | [`.agent/rules/platform/transactions.md`](.agent/rules/platform/transactions.md) |
| R17 | [R17. Events](#r17-events) | [`.agent/rules/platform/events.md`](.agent/rules/platform/events.md) |
| R18 | [R18. Logging](#r18-logging) | [`.agent/rules/platform/logging.md`](.agent/rules/platform/logging.md) |
| R19 | [R19. Naming](#r19-naming) | [`.agent/rules/conventions/naming.md`](.agent/rules/conventions/naming.md) |
| R20 | [R20. Coding Style](#r20-coding-style) | [`.agent/rules/conventions/coding-style.md`](.agent/rules/conventions/coding-style.md) |
| R21 | [Before generating code](#before-generating-code-r21) | [`.agent/rules/README.md`](.agent/rules/README.md) |

## Architecture

### R1. Module Structure and Dependencies

Load when adding a module, changing package layout, or wiring Modulith dependencies.

#### Layout

- Module layout per bounded context:
  - Domain: `{name}-domain/`
  - Application: `{name}-application/`
  - Persistence Adapter: `{name}-adapter-persistence/`
  - Infrastructure & Adapters: `storage-adapter-s3/`, `outbox-infrastructure/`, `workflow-infrastructure/`, `logger-slf4j/`
  - Web & Application Assembly: `store/`
- Dependency direction (Hexagonal / Ports & Adapters):
  - `framework` <- `{name}-domain`
  - `{name}-domain` <- `{name}-application`
  - `{name}-domain` + `{name}-application` <- `{name}-adapter-persistence`
  - `{name}-application` + `{name}-adapter-persistence` <- `store`
  - `framework` <- `outbox-infrastructure` <- `store`
  - `framework` <- `workflow-infrastructure` <- `store`
  - `framework` <- `logger-slf4j` <- `store`
  - `framework` <- `storage-adapter-s3` <- `store`

#### Spring Modulith

- Each bounded context has an `@ApplicationModule(allowedDependencies = "shared")` marker class in `store`, for example `CatalogModule`, `InventoryModule`.
- `shared` is OPEN: `@ApplicationModule(type = ApplicationModule.Type.OPEN)` on `com.grab.store.shared` (`package-info.java`).
- Internal package goes under `internal/`.
- Cross-module communication is via domain/integration events and published named interfaces only. No direct method calls into another module's `internal/` packages.

#### Named interfaces

- Events: publish from a public named-interface package, for example `com.grab.store.merchant.events` with `@NamedInterface("events")`. Consuming modules declare the dependency explicitly, for example `@ApplicationModule(allowedDependencies = {"shared", "merchant::events"})`.
- HATEOAS links: publish link facades from a public named-interface package, for example `com.grab.store.catalog.api` with `@NamedInterface("api")`. Consuming modules declare `{module}::api` in `allowedDependencies`. See `inbound/hateoas.md`.

#### Layer contents

- Domain (`{name}-domain/`): Pure domain logic. No Spring, JPA, or MapStruct annotations. Contains aggregates, entities, value objects, domain events, domain policies, and outbound write repository ports (`port/outbound/{Domain}Repository`).
- Application (`{name}-application/`): Inbound use case ports (`port/inbound/*UseCase`), use case services (`service/*Service`), outbound intra query ports (`port/outbound/*QueryPort`), outbound inter-module ports (`port/outbound/{TargetDomain}*Port`), application commands/queries, and read views (`model/read/*View`).
- Persistence Adapter (`{name}-adapter-persistence/`): Strictly INTRA-module persistence. Outbound persistence adapters (`adapter/*RepositoryAdapter`, `adapter/*QueryAdapter`, `adapter/*PersistenceExecutor`), JPA entities, Spring Data JPA repositories (`repository/jpa/*JpaRepository`), specifications (`specification/jpa/`), mappers/assemblers (`mapper/`), outbox producers/processors (`outbox/`), and Spring bean configuration (`config/*PersistenceConfig`). Never contains cross-module adapters.
- Storage Adapter (`storage-adapter-s3/`): Outbound file storage adapter implementing `FileStoragePort` using AWS S3 / MinIO.
- Web & App Assembly (`store/`): REST controllers, request/response DTOs, mappers, HATEOAS model assemblers, CQRS command/query handlers delegating to use cases, event listeners, application policies, provider public ports (`{domain}/port/*`), provider adapters (`{domain}/internal/api/adapter/*`), consumer cross-module adapters (`{domain}/internal/adapter/*`), and application configuration.

### R2. Layered Architecture and CQRS-Light

Load when adding or changing an HTTP use case, command/query flow, or layer responsibility.

#### Layers

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

#### CQRS data flow

1. Controller -> Service -> Mapper(to Command) -> CommandBus.dispatch -> **Handler.handle** -> **UseCase.execute** -> domain/ports -> Result
2. Controller -> Service -> Mapper(to Query) -> QueryBus.dispatch -> **Handler.handle** -> **UseCase.execute** -> query ports -> Result
3. Paginated queries: Mapper produces a record implementing `Query<Page<Result>>` and `PageableQueryRequest`. Handler returns `Page<Result>`. Service maps `resultPage.map(mapper::toResponse)`.

#### CQRS Separation & Isolation Rules

- **Strict Pipeline Independence**: The Command pipeline (write) and Query pipeline (read) are strictly isolated. They must not mess with each other.
- **CommandService Cannot Query**: `CommandService` MUST NOT inject, invoke, or dispatch via `QueryBus`, nor call `QueryService`. It handles writes only. Any aggregate state required for command processing must be retrieved inside the **use case** via its domain repository, or provided via the request DTO.
- **QueryService Cannot Mutate**: `QueryService` MUST NOT inject, invoke, or dispatch via `CommandBus`, nor call `CommandService`. It is strictly read-only and side-effect free.
- **No Cross-Service Invocations**: `CommandService` and `QueryService` must never inject or call one another.
- **Controller Delegation**: Controllers bridge HTTP to services by delegating write operations (POST, PUT, PATCH, DELETE) to `CommandService` and read operations (GET) to `QueryService`. Controllers MUST NOT inject `CommandBus`, `QueryBus`, handlers, use cases, ports, or adapters directly.
- **Query handlers read via query ports**: Query use cases and `QueryHandler` classes MUST NOT inject domain write `*Repository` ports or call domain services that load aggregates. Reads go through `*QueryPort` (or lite BC query ports in `{bc}-domain.port.outbound`) returning views/projections.
- **Modulith `{module}::query` named interfaces**: Adapters under `store/.../{module}/internal/.../query/` (or `internal/api/query/` or `internal/api/adapter/`) that implement published `{module}::query` ports MUST delegate to inbound `*UseCase` only. They MUST NOT inject application outbound `*QueryPort` or dispatch `QueryBus`. Passthrough use cases are OK. Future gRPC/HTTP servers call the same use cases. Cross-module consumers (cart, storefront projectors) stay thin mappers on the named interface; one business question = one named-interface method. See `outbound/api-ports.md`.

## Inbound

### R3. Controllers

Load when creating or changing REST controllers.

#### MUST

- `@RestController` + `@RequestMapping("/api/v1/{resource}")` + `@RequiredArgsConstructor`
- Inject `XxxCommandService`, `XxxQueryService`, `XxxModelAssembler` (entity-level or per-operation)
- Delegate write operations (POST, PUT, PATCH, DELETE) strictly to `XxxCommandService`
- Delegate read operations (GET) strictly to `XxxQueryService`
- Keep write and read paths cleanly separated without mixing responsibilities
- Inject `PagedResourcesAssembler<ResponseDto>` as a method param for paginated endpoints
- Return `ResponseEntity<EntityModel<T>>` (single), `ResponseEntity<PagedModel<EntityModel<T>>>` (paginated), or `ResponseEntity<Void>` plus `Location` header (creation without body)
- Use `@Valid @RequestBody` and `@RequestHeader(value = "X-Actor-Id")`

#### MUST NOT

- Put business logic in the controller. Delegate everything to services.
- Inject or invoke `CommandBus` or `QueryBus` directly. Controllers must go through services.
- Inject or call handlers, use cases, ports, repositories, or adapters.
- Call `XxxCommandService` from a GET endpoint or `XxxQueryService` from a state-mutating endpoint.

### R4. Services

Load when creating or changing command/query services.

#### MUST

- `@Service` + `@RequiredArgsConstructor`
- Maintain strict separation between CommandService (writes) and QueryService (reads).
- CommandService: inject only `CommandBus`, operation-specific request mappers (`XxxRequestMapper`), and identity utilities (e.g. `IdGenerator`). Flow: `mapper.toCommand(dto)` then `commandBus.dispatch(command)` then `mapper.toResponse(result)`. Returns `XxxResponse` or ID.
- QueryService: inject only `QueryBus` and query mappers (`XxxRequestMapper`).
  - Single: `mapper.toQuery(id)` then `queryBus.dispatch(query)` then `mapper.toResponse(result)`
  - Paginated list: `mapper.toQuery(filters, pageable)` then `queryBus.dispatch(query)` then `resultPage.map(mapper::toResponse)`

#### MUST NOT

- Mess with each other (no cross-calling between command and query services):
  - `CommandService` MUST NOT inject, call, or invoke `QueryBus` or any `QueryService`.
  - `CommandService` MUST NOT perform queries. If business logic needs aggregate data, the command handler/use case must load the aggregate via domain repository port, or required inputs must be provided in the request payload.
  - `QueryService` MUST NOT inject, call, or invoke `CommandBus` or any `CommandService`.
  - `QueryService` MUST NOT trigger state changes or side effects.
- Inject or call ports, repositories, or adapters. Only handlers and use cases touch ports.
- Contain business rules. Those belong in policies or aggregates.

### R5. Mappers

Load when creating or changing API MapStruct mappers.

#### MUST

##### REST Mappers
- One mapper class per handler/operation.
- File: `store/.../api/rest/mapper/{Action}{Entity}RequestMapper.java`
- Annotate with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`
- Declare as `public abstract class` (not interface)
- Provide exactly two methods:
  - `toCommand(params...)` -> Command, or `toQuery(params...)` -> Query
  - `toResponse(Result)` -> ResponseDto

##### Outbound Query Mappers (Shared Interface Ports)
- One mapper class per outbound query port.
- File: `store/.../{domain}/internal/api/query/mapper/{QueryPortName}Mapper.java`
- Annotate with `@Mapper(config = CentralMapperConfig.class)`
- Declare as `public abstract class` (not interface)
- Map use case `*Result` to the query interface's embedded response DTO (`toResponse(Result) -> ResponseDto`).

### R6. Command, Query, and Result Records

Load when creating or changing Command, Query, or Result types.

#### MUST

- Java `record` implementing `Command<R>` or `Query<R>`
- Use `Id` from framework for entity identifiers, not raw `String`
- Paginated queries: `R = Page<XxxResult>`, and the record also implements `PageableQueryRequest`
- Result records use primitives, `String`, and `Id`

#### MUST NOT

- Put domain aggregates on Result records.

### R7. Handlers

Load when creating or changing command/query handlers, or deciding who may use ports and repositories.

#### MUST

- CommandHandler: `@Component` + `@RequiredArgsConstructor`, implements `CommandHandler<C,R>`. Use `@{Module}Transactional` for writes.
- QueryHandler: `@Component` + `@RequiredArgsConstructor`, implements `QueryHandler<Q,R>`. Use `@{Module}ReadTransactional` for reads.
- Implement both `handle(...)` and `getCommandType()` / `getQueryType()`.
- Act as CQRS adapters: demarcate the transaction boundary, adapt Command/Query records, and delegate execution to the appropriate inbound `*UseCase` port (or orchestrate ports directly in lite bounded contexts).
- Command handlers / use cases inject domain write port `{Domain}Repository`.
- Query handlers / use cases for list/search/paged reads inject application query port `{Domain}QueryPort`. See `infrastructure/persistence.md`.
- Cascade work by dispatching another command/query via `CommandBus` / `QueryBus` (typically from an event listener), or by calling policies and ports directly within the handler orchestration.

#### MUST NOT

- Embed business rules inline in the handler. Business rules belong in domain aggregates, domain policies, or use case services.
- Call another handler directly.
- Inject Spring Data `*JpaRepository` or concrete `*Adapter` classes directly. Handlers/use cases inject port interfaces only.
- Let controllers, services, mappers, assemblers, or policies inject or call ports, repositories, or adapters.
- Let event listeners call handlers or repositories directly. Listeners cascade via `CommandBus`/`QueryBus`.
- Let `{module}::query` named-interface adapters inject application outbound `*QueryPort` or dispatch `QueryBus`. They MUST call inbound `*UseCase.execute(...)` and map results to the published slice types. See `architecture/layered-cqrs.md`.

### R8. DTOs

Load when creating or changing request/response DTOs.

#### MUST

- All DTOs are Java `record` types.
- Request DTOs use Jakarta Bean Validation (`@NotBlank`, `@Min`, and similar).
- Response DTOs use primitive types and `String` only.
- Controllers accept and return DTOs.

#### MUST NOT

- Put domain types on response DTOs.
- Accept or return domain aggregates or JPA entities from controllers.

### R9. HATEOAS

Load when adding links, assemblers, root controllers, or HAL configuration.

#### Implementation

- `@Component`, implements `RepresentationModelAssembler<ResponseDto, EntityModel<ResponseDto>>`
- Use `linkTo(methodOn(XxxController.class).methodName(...))`. Never manual URL strings except Tier 2 root endpoints.
- Pass `null` for `@RequestBody`, `@RequestHeader`, `Pageable`, and `PagedResourcesAssembler` params in `methodOn()`.

#### Assembler granularity

- Entity-level: `{Entity}ModelAssembler` for shared links reused across endpoints.
- Per-operation: `{Action}{Entity}ModelAssembler` when the response DTO or link set is operation-specific. Prefer this when command/query results have distinct shapes or navigation.
- Both styles are valid. Choose based on whether links/DTO are shared or operation-specific.

#### Rel naming

Use inline string literals. No `LinkRelations` constants class.

| Link type | Rel pattern |
|---|---|
| Self | `self` via `.withSelfRel()` |
| Retrieve | `get-{entity}` |
| List collection | `list-{entities}` |
| Search/filter | `search-{entities}` |
| Create | `create-{entity}` |
| Update | `update-{entity}` |
| Delete | `delete-{entity}` |
| State transition | `{action}-{entity}` |
| Qualified action | `{action}-{entity}-{qualifier}` |
| Subresource | `get-{entity}-{subresource}`, `list-{entity}-{subresources}` |

Rules: kebab-case, action-first, plural for collections, never `paged-*`, never `edit-*`, never bare entity nouns.

#### Self-link

- `self` only for the canonical GET endpoint that returns the exact representation.
- Command-result DTOs use `get-{entity}` instead of `self`.

#### PagedModel links

- Every `PagedModel` MUST expose `list-{entities}`.
- Add `create-{entity}` when creation is available.
- Add links on `PagedModel` after `pagedAssembler.toModel()`.

#### 3-tier API discovery

1. Tier 1: `ApiRootController` at `GET /api/v1` links to all bounded context roots.
2. Tier 2: `{Context}RootController` at `GET /api/v1/{context}` links to top-level resources plus any cross-domain workflow entry links required by that context (via `{owner}::api`).
3. Tier 3: individual resource endpoints.

- New bounded context = new Tier 2 root + update `ApiRootController`.
- Root endpoints return `ResponseEntity<RepresentationModel<?>>` with `MediaTypes.HAL_JSON_VALUE`.

#### Bare EntityModel

- `EntityModel.of(dto)` without links is acceptable for bulk/utility/audit endpoints.
- Prefer adding a meaningful navigation link.

#### Cross-domain link relations

HATEOAS links may point across bounded contexts for UI/workflow discovery. Links are navigation affordances only. The owning module still serves the data and owns the endpoint.

| Concern | Owner |
|---|---|
| Endpoint URI + controller | Owning bounded context |
| Data returned by that endpoint | Owning bounded context |
| Advertising the link from another context's response | Consuming module via published `{owner}::api` facade |
| Local read-model / projection for validation | Consuming module. Not a substitute for the owner's list/search API. |

##### MUST

- Advertise cross-domain navigation through a published link facade in the owning module: package `com.grab.store.{owner}.api` with `@NamedInterface("api")`.
- Facade class name: `{Owner}ApiLinks`. Methods return `org.springframework.hateoas.Link` via `linkTo(methodOn(...))`. Never hardcoded path strings.
- Consuming modules import only `{owner}.api` types and declare `allowedDependencies = { ..., "{owner}::api" }`.
- Place cross-domain links where the client needs them:
  - Tier 2 `{Consumer}RootController` when the context entry exposes a use case that needs another context.
  - Relevant `PagedModel` / assemblers that surface the create/compose workflow.
- Keep the same `rel` names as the owning context's root. Do not invent parallel rel synonyms for the same endpoint.
- Prefer linking to the owning Tier 2 root (`get-{owner}-root`) only as a last resort when a specific published link does not exist yet.

##### MUST NOT

- Import another module's `internal/` controllers, assemblers, services, or handlers from a consumer.
- Proxy or re-implement another module's list/search/get under the consumer's API path solely to attach a HATEOAS link.
- Expose a local projection as the primary browse API for UI picking. Use the owning catalog/module links instead.
- Hardcode absolute/relative URL strings in assemblers or root controllers for cross-domain endpoints.
- Add `{owner}::api` dependency just in case. Only when a real cross-domain workflow link is required.
- Put business data from another aggregate into the consuming module's response just because a link was added. Link means navigate; query the owner for payload.

##### Published `{Owner}ApiLinks` shape

- Facade may reference the owner's own `internal/.../controller` types. That is within the owning module. Consumers never touch those controllers.
- Expand the facade when new stable entry-point links are needed by other modules. Keep methods coarse (search/get/create of top-level resources), not every sub-action.

##### Client discovery

- `GET /api/v1/{consumer}` (or a workflow `PagedModel`) exposes `_links.{owner-rel}` that point at the owning module endpoint, and `_links.create-{entity}` for the consumer write endpoint.
- Frontend MUST follow `_links` hrefs. MUST NOT hardcode cross-module paths when those rels are present.

##### Workflow composition

- When a use case needs many links from several modules, prefer a thin workflow / composition resource under `shared` or a dedicated non-domain package, for example `GET /api/v1/workflows/create-inventory-item`.
- Do not stuff unrelated foreign links onto every consumer resource representation.
- Do not elevate domain handlers into workflow orchestrators for HATEOAS-only concerns.

#### HAL configuration (R18)

- No custom `HalConfiguration` bean, no `CurieProvider`, no Affordances API.
- Only relevant config: `server.forward-headers-strategy: framework` (correct URLs behind proxies).
- Dependency: `spring-boot-starter-hateoas`.

## Outbound

### R10. Outbound API Rules (Shared Interface Ports)

Load when exposing APIs between modules via shared interface ports instead of gRPC.

#### Context

When a module needs to expose query APIs to other modules synchronously (inter-module communication), shared interface ports are used in place of gRPC.

#### Package & File Structure

##### 1. Provider Side (Exposing the Port)

| Component | Location | Example |
|---|---|---|
| Public Port & DTOs | `store/.../{provider}/port/{Capability}Port.java` (or `{Capability}Query.java`) | `com.grab.store.identity.port.UserProfileQuery` |
| Provider Port Adapter | `store/.../{provider}/internal/api/adapter/{Capability}PortAdapter.java` (or `{Capability}QueryAdapter.java`) | `com.grab.store.identity.internal.api.adapter.UserProfileQueryAdapter` |
| Provider Port Mapper | `store/.../{provider}/internal/api/adapter/mapper/{Capability}Mapper.java` | `com.grab.store.identity.internal.api.adapter.mapper.UserProfileQueryMapper` |

##### 2. Consumer Side (Consuming the Port via Anti-Corruption Layer)

| Component | Location | Example |
|---|---|---|
| Consumer Outbound Port | `{consumer}-application/.../port/outbound/{TargetDomain}{Capability}Port.java` | `com.customer.application.port.outbound.UserProfileQueryPort` |
| Consumer Outbound Adapter | `store/.../{consumer}/internal/adapter/{TargetDomain}{Capability}Adapter.java` | `com.grab.store.customer.internal.adapter.CustomerUserProfileQueryPortAdapter` |

> [!IMPORTANT]
> **Persistence is strictly INTRA**: `{name}-adapter-persistence` communicates only with its local database via JPA. It MUST NOT contain `{Inter}Adapter` or call other modules. All cross-module inter-communication adapters live in `store/.../{consumer}/internal/adapter/`.

#### Rules

##### 1. Provider Public Port & DTOs
- The public port interface lives in the module's public port package (`store/.../{provider}/port/`).
- **DTOs MUST only be declared inside the port interface** (e.g., as nested Java `record`s like `record UserProfileResponse(...)` or `record GrantAccessRequest(...)`).
- Do not create separate DTO files in `dto/` for shared outbound ports.

##### 2. Provider Adapter Implementation
- The adapter implements the provider port interface and lives in `store/.../{provider}/internal/api/adapter/`.
- **Must ONLY use inbound `*UseCase`** to retrieve data or execute commands.
- **MUST NOT** inject outbound `*QueryPort`, `*Repository`, JPA repositories, or dispatch via `QueryBus` / `CommandBus`.
- **Transaction starts on this adapter method**: annotate the query method with `@{Module}ReadTransactional` (or `@{Module}Transactional` if writing).

##### 3. Provider Mapper
- Mapper lives in `store/.../{provider}/internal/api/adapter/mapper/{Capability}Mapper.java`.
- MapStruct abstract class annotated with `@Mapper(config = CentralMapperConfig.class)`.
- Maps the use case's `*Result` to the port interface's DTO response.

##### 4. Consumer Outbound Port & Adapter (ACL)
- The consumer application defines an outbound port in `{consumer}-application/.../port/outbound/{TargetDomain}{Capability}Port.java` in terms of its own domain needs.
- The consumer implements this port in `store/.../{consumer}/internal/adapter/{TargetDomain}{Capability}Adapter.java`.
- The adapter injects the provider's public port (`store/.../{provider}/port/*`) and maps the response to the consumer's model.


#### MUST
- Declare response DTOs as records inside the query interface.
- Implement the query interface in `{domain}/internal/api/adapter/{Port}Adapter` or `{domain}/internal/api/query/{Port}Adapter`.
- Inject only `*UseCase` in the adapter to fetch results.
- Demarcate transaction boundary on the adapter method with `@{Module}ReadTransactional`.
- Place mappers in `{domain}/internal/api/query/mapper/{QueryPortName}Mapper`.

#### MUST NOT
- Inject outbound `*QueryPort`, `*Repository`, or JPA repositories into the query adapter.
- Dispatch via `QueryBus` or `CommandBus` from the query adapter.
- Define standalone DTO files outside the query interface.
- Omit the transaction annotation on the adapter query method.

### R11. Persistence Adapters

Load when adding or changing JPA entities, mappers, persistence adapters, specifications, or persistence configuration.

#### Mapping

- EntityMapper (MapStruct): JPA entity <-> domain aggregate fields.
- JpaAssembler (manual): complex assembly from multiple JPA entities into a domain aggregate.
- Overall Mapper coordinates EntityMapper + JpaAssembler.

#### Adapter and Port Layout

Path base: `{name}-adapter-persistence/src/main/java/com/{name}/adapter/persistence/`

Separate write and query concerns into distinct ports and adapters:

| Artifact | Location | Role |
|---|---|---|
| Domain write port | `{name}-domain/.../port/outbound/{Domain}Repository` | Aggregate load/save/delete interface. Framework-agnostic. |
| Application query port | `{name}-application/.../port/outbound/{Domain}QueryPort` | Read/search interface returning view records. |
| Write repository adapter | `.../adapter/{Domain}RepositoryAdapter` | Outbound adapter implementing domain `{Domain}Repository`. |
| Query adapter | `.../adapter/{Domain}QueryAdapter` | Outbound adapter implementing application `{Domain}QueryPort`. |
| Persistence executor | `.../adapter/{Domain}PersistenceExecutor` | Implements `PersistenceExecutor` for query/command exception translation. |
| Spring Data JPA | `.../repository/jpa/{Domain}JpaRepository` | Spring Data interface used internally by adapters only. |
| Specification | `.../specification/jpa/{Domain}*Specification` | Criteria API predicates / queries for paged search and filtering. |
| Read view | `{name}-application/.../model/read/{Domain}View` | Read-model record defined in the application layer. |
| Persistence configuration | `.../config/{Domain}PersistenceConfig` | Spring `@Configuration` wiring adapters and exposing beans as port types. |

#### Write repository adapter (`{Domain}RepositoryAdapter`)

- Implements domain outbound port `{Domain}Repository` (aggregates in / aggregates out).
- Resides in `com.{name}.adapter.persistence.adapter`.
- Injects `{Domain}JpaRepository` + mapper/assembler, `DomainEventProducer`, and `PersistenceExecutor`.
- Used by command use cases and command handlers. Query handlers use it only when loading a full aggregate for a single-get that maps from domain.

#### Query adapter (`{Domain}QueryAdapter`)

- Implements application outbound port `{Domain}QueryPort`.
- Resides in `com.{name}.adapter.persistence.adapter`.
- Has its own class. Do not fold query methods into the write repository adapter.
- Injects `{Domain}JpaRepository` and uses it for fixed JPQL / derived queries defined on the JPA interface.
- For paged search / filtered list: injects a specification class and MUST build the query via Criteria API through that specification. Never ad-hoc criteria inside the handler.
- Returns view record types (`{Domain}View` / summary records). Never domain aggregates or JPA entities to the application layer.

#### Specification (paged search)

- Every paged search/list query MUST go through a specification class injected into the query adapter.
- Specification encapsulates Criteria API (`CriteriaBuilder` / predicates / joins), or an equivalent dedicated search-query helper under `specification/jpa/`, given filter criteria + `Pageable`.
- Result shape is a view/summary record suitable for mapping to Query Result then Response DTO.

#### Configuration & Bean Exposure

- `{Domain}PersistenceConfig` exposes beans returning **port interfaces**, not concrete adapter types:
  - `public {Domain}Repository {domain}Repository(...) { return new {Domain}RepositoryAdapter(...); }`
  - `public {Domain}QueryPort {domain}QueryPort(...) { return new {Domain}QueryAdapter(...); }`

#### Handler and Use Case Usage

- Command handlers / use cases -> domain write port `{Domain}Repository` only.
- Query handlers / use cases (list/search/paged) -> application query port `{Domain}QueryPort` only.
- Modulith `{module}::query` adapters in `store` -> inbound `*UseCase` only, never outbound `{Domain}QueryPort` directly.
- Handlers and use cases MUST NOT inject `{Domain}JpaRepository`, `EntityManager`, or concrete `*Adapter` classes directly.
- Controllers, services, mappers, assemblers, and policies MUST NOT use any repository, port, or adapter. See `application/handlers.md`.

### R12. Outbound Infrastructure & Platform Adapters

Load when working with platform kits, storage adapters, transactional outbox, or workflow infrastructure.

#### Scope & Components

Outbound platform adapters provide technical capabilities driven by the application:

| Module | Location | Responsibility |
|---|---|---|
| Storage Adapter | `storage-adapter-s3/` | Implements `FileStoragePort` using AWS S3 / MinIO. |
| Outbox Infrastructure | `outbox-infrastructure/` | Transactional outbox event publishing, persistence, and polling/processing. |
| Workflow Infrastructure | `workflow-infrastructure/` | Saga orchestrations, step execution, compensating transactions. |
| Logger Adapter | `logger-slf4j/` | Structured logging implementation. |

#### Rules

##### 1. Storage (`storage-adapter-s3`)
- Implements `FileStoragePort` declared in `framework`.
- Does not contain business logic. Handles S3/MinIO upload, download, presigned URLs, and bucket management.
- Wired into `store` via Spring `@Configuration`.

##### 2. Outbox (`outbox-infrastructure`)
- Outbox table lives in each bounded context's datasource.
- Events are written inside the business transaction.
- Outbox poller/processor publishes events asynchronously to consumers.

##### 3. Workflow (`workflow-infrastructure`)
- Three-layer platform kit: `framework.workflow` -> `workflow-infrastructure` -> `store/workflows`.
- No separate `workflow-domain` or `workflow-application`.
- Orchestrates multi-context seller/store flows without two-phase commit.

#### MUST
- Implement ports defined in `framework` or application layer.
- Keep platform adapters decoupled from specific domain models.
- Use module-specific datasources and transaction managers where persistence is needed.

#### MUST NOT
- Bypass ports to invoke infrastructure directly from domain aggregates or policies.
- Introduce direct dependencies between domain/application modules and specific cloud providers.

## Domain

### R13. Domain Aggregates

Load when changing domain models, invariants, or domain events.

#### MUST

- Stay framework-agnostic. No Spring, JPA, or MapStruct annotations.
- Route all state mutations through aggregate methods that enforce invariants.
- Accumulate domain events via `addEvent()`, pull via `pullEvents()`.
- Keep aggregates non-anemic. Invariants live on the aggregate.
- Put reusable / cross-cutting decision rules in domain policies, or domain services when stateful coordination is required.
- Handlers orchestrate aggregates + policies inside a transaction.

#### MUST NOT

- Put business rules in handlers.

### R14. Policies

Load when encoding a business rule or deciding where a rule belongs.

Business rules are controlled by policies, not by handlers, services, or controllers.

#### Domain policies

Path: `{name}-domain/.../policy/`

- Framework-agnostic pure domain decision rules: authorization of domain actions, placement, registration eligibility, delegation, approval criteria, and similar.
- Operate on domain types (aggregates, value objects, ids/codes).
- No Spring, no HTTP, no DTOs, no repositories.
- Examples: `RoleDelegationPolicy`, `AccessPlacementPolicy`, `MerchantApprovalPolicy`.

#### Application policies

Path: `store/.../{module}/internal/policy/`

- Application/use-case rules that need application context: security scope, actor/session context, cross-cutting access checks against already-loaded aggregates.
- May use Spring (`@Component`) and application-layer types.
- MUST NOT inject repositories. Receive needed data from the calling handler or other already-resolved inputs.
- Examples: `InventoryLocationAccessPolicy`, `MerchantApprovalAccessPolicy`.

#### Usage

- Handlers invoke policies, then apply aggregate mutations / persistence.
- Prefer domain policies when the rule is intrinsic to the bounded context.
- Use application policies when the rule depends on app, security, or orchestration context.

## Platform

### R15. Errors

Load when adding module errors, exception types, or API error responses.

#### MUST

- Module-specific `sealed interface {Module}ServiceError extends MessageSource` with error record subtypes.
- Each error record has `kind()` -> `ErrorCategory`, `code()` -> i18n key, `args()`.
- Error code: `{module-prefix}.{layer}.{entity}.{error_type}`, for example `inv.service.location.not_found`.
- Module exception extends `DomainException`, caught by `GlobalApiExceptionHandler` (`@RestControllerAdvice`).
- Error responses: RFC 7807 `ProblemDetail` with custom fields `code`, `args`, `traceId`, `module`, `retryable`, `retryAfterMs`.

### R16. Transactions

Load when adding transactional annotations or deciding where a transaction starts.

#### MUST

- Each module has its own `DataSource` + `TransactionManager` (multi-datasource).
- Custom meta-annotations: `@{Module}Transactional` (read-write), `@{Module}ReadTransactional` (read-only).
- Transactions at handler level and outbound API query adapter level only.
- Handler or outbound query adapter demarcates the transactional unit of work. Business decisions inside that unit belong to aggregates/policies.
- For outbound query adapters exposing APIs via shared interface ports, start the transaction on the adapter method with `@{Module}ReadTransactional` (or `@{Module}Transactional`).

#### MUST NOT

- Put `@Transactional` (or module equivalents) on service, controller, policy, or mapper.

### R17. Events

Load when adding domain events, listeners, or cross-module integration.

#### Event Dispatch & Listener Annotation Rules

The choice of listener annotation depends on the **event delivery mechanism and transaction lifecycle**, NOT the package folder:

| Event Source | Annotation | Rationale |
|---|---|---|
| **Outbox-dispatched events** (cross-module integration events, outbox domain events, workflow signals) | `@EventListener` | `AbstractOutboxProcessor` dispatches synchronously inside `publishTransactionTemplate`. `@EventListener` allows exceptions to be caught so the processor can mark the event as `FAILED` and schedule retries (guaranteeing at-least-once delivery). |
| **Direct in-memory events** (published inside an active DB transaction before commit, e.g. SSE UI notifications) | `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` | Defers execution until the active transaction commits, preventing phantom side effects or UI race conditions if the transaction rolls back. |

#### MUST

- Use `@EventListener` in `store/.../event/` for all outbox-dispatched events (cross-module integration events, outbox domain events, and workflow signals).
- Use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` ONLY for direct in-memory events published during an active database transaction (e.g. `WorkflowTerminalUiEventListener`).
- Cross-module communication: transactional outbox via `outbox-infrastructure` (at-least-once delivery), exposed through Modulith named interfaces. See `architecture/module-structure.md`.
- Event listener classes dispatch cascading commands via `CommandBus`.

#### MUST NOT

- Use `@TransactionalEventListener` on outbox-dispatched events. It defers execution until AFTER `AbstractOutboxProcessor` commits `markPublished()`, silently breaking outbox error handling and retries.
- Call command handlers or write domain repositories directly from event listeners.

### R18. Logging

Load when adding log statements in infrastructure or application layers.

#### MUST

- Infrastructure and application layers use `Loggers.getLogger(<ClassName>.class)` to initialize loggers.

## Conventions

### R19. Naming

Load when naming a new type, package, or test method.

| Artifact | Pattern | Location |
|---|---|---|
| Controller | `{Entity}Controller` | `store/.../{module}/internal/api/rest/controller/` |
| Command/Query Service | `{Entity}CommandService` / `{Entity}QueryService` | `store/.../{module}/internal/command/` or `internal/query/` |
| Request DTO | `{Action}{Entity}Request` | `store/.../{module}/internal/api/rest/dto/request/` |
| Response DTO | `{Entity}Response` | `store/.../{module}/internal/api/rest/dto/response/` |
| Command record | `{Action}{Entity}Command` | `{name}-application/.../model/write/` |
| Query record | `Get{Entity}Query` / `List{Entities}Query` | `{name}-application/.../model/read/` |
| Command/Query Handler | `{Action}{Entity}CommandHandler` / `{Action}{Entity}QueryHandler` | `store/.../{module}/internal/command/` or `internal/query/` |
| Mapper | `{Action}{Entity}RequestMapper` | `store/.../{module}/internal/api/rest/mapper/` |
| Model Assembler (entity) | `{Entity}ModelAssembler` | `store/.../{module}/internal/api/rest/assembler/` |
| Model Assembler (per-operation) | `{Action}{Entity}ModelAssembler` | `store/.../{module}/internal/api/rest/assembler/` |
| Domain Policy | `{Capability}Policy` | `{name}-domain/.../policy/` |
| Application Policy | `{Capability}Policy` | `store/.../{module}/internal/policy/` |
| **Inbound use case port** | `{Action}{Entity}UseCase` | `{name}-application/.../port/inbound/` |
| **Use case service** | `{Action}{Entity}Service` | `{name}-application/.../service/` |
| **Outbound Intra write port (Domain Repository)** | `{Domain}Repository` | `{name}-domain/.../port/outbound/` |
| **Outbound Intra read port (Application Query)** | `{Domain}QueryPort` | `{name}-application/.../port/outbound/` |
| **Outbound Inter port (Consumer ACL Port)** | `{TargetDomain}{Capability}Port` | `{name}-application/.../port/outbound/` |
| **Persistence write adapter (Intra only)** | `{Domain}RepositoryAdapter` | `{name}-adapter-persistence/.../adapter/` |
| **Persistence query adapter (Intra only)** | `{Domain}QueryAdapter` | `{name}-adapter-persistence/.../adapter/` |
| **Persistence executor** | `{Domain}PersistenceExecutor` | `{name}-adapter-persistence/.../adapter/` |
| Spring Data JPA | `{Domain}JpaRepository` | `{name}-adapter-persistence/.../repository/jpa/` |
| Query specification | `{Domain}*Specification` (or `{Domain}SearchCriteria`) | `{name}-adapter-persistence/.../specification/jpa/` |
| Read view | `{Domain}View` / `{Domain}Summary` | `{name}-application/.../model/read/` |
| API Root | `ApiRootController` | `store/.../` |
| Bounded Context Root | `{Context}RootController` | `store/.../{module}/` |
| Modulith named interface package (events) | `{module}.events` + `@NamedInterface("events")` | `store/.../{module}/events/` |
| Modulith named interface package (API links) | `{module}.api` + `@NamedInterface("api")` | `store/.../{module}/api/` |
| Cross-module HATEOAS link facade | `{Owner}ApiLinks` | `store/.../{owner}/api/` |
| **Provider Public Port (Inter-module contract)** | `{Capability}Port` or `{Capability}Query` | `store/.../{provider}/port/` |
| **Provider Public Port DTO** | Nested `record` inside the port interface | inside `store/.../{provider}/port/{Port}.java` |
| **Provider Port Adapter** | `{Capability}PortAdapter` or `{Capability}QueryAdapter` | `store/.../{provider}/internal/api/adapter/` |
| **Provider Port Mapper** | `{Capability}Mapper` | `store/.../{provider}/internal/api/adapter/mapper/` |
| **Consumer Outbound Adapter (Inter ACL)** | `{TargetDomain}{Capability}Adapter` | `store/.../{consumer}/internal/adapter/` |
| Test method | `{functionName}_{input}_{expectedBehavior}` | `src/test/java/...` |

### R20. Coding Style

Load when writing or refactoring Java in this repo.

#### MUST

- Extract intermediate variables.
- Use descriptive names for those intermediates.

#### MUST NOT

- Nest function invocations, for example `doSomething(doA(doB()))`.

## Before generating code (R21)


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

## Source review

- `R7` refers to `infrastructure/persistence.md`, and `R11` refers to `application/handlers.md`. The corresponding source rules are `outbound/persistence.md` and `inbound/handlers.md`.
- `R9` labels its HAL configuration subsection `(R18)`, while the rule index assigns R18 to Logging. The HAL requirements remain under R9 here.
- `R10` uses both `internal/api/adapter/mapper/` and `internal/api/query/mapper/` for provider mappers. `R5` uses `internal/api/query/mapper/`. The source does not specify which path takes precedence; both wordings are retained above.
