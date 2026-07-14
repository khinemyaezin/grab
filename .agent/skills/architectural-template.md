# Architectural Rules (Grab E-Commerce)

You MUST follow every rule below when generating, reviewing, or refactoring code. These rules override any conflicting general coding conventions.

---

## R1. Module Structure & Dependencies

- **Module layout:** `{name}-domain/` → `{name}-infrastructure/` → `store/`
- **Dependency direction:** `framework ← domain ← infrastructure ← store`, `framework ← outbox-infrastructure ← store`, `framework ← logger-slf4j ← store`
- **Spring Modulith:**
  - Each bounded context has `@ApplicationModule(allowedDependencies = "shared")` marker class (e.g. `CatalogModule`, `InventoryModule`).
  - **`shared`** is declared OPEN: `@ApplicationModule(type = ApplicationModule.Type.OPEN)` on `com.grab.store.shared` (`package-info.java`).
  - Internal package goes under `internal/`. Cross-module communication is via domain/integration events only — no direct method calls into another module's internals.
  - **Named interfaces for cross-module events:** publish events from a public named-interface package (e.g. `com.grab.store.merchant.events` with `@NamedInterface("events")`). Consuming modules declare the dependency explicitly, e.g. `@ApplicationModule(allowedDependencies = {"shared", "merchant::events"})` on `IdentityModule`.
- **Domain layer** (`{name}-domain/`): NO Spring, JPA, or MapStruct annotations. Contains aggregates, entities, value objects, events, repository interfaces, domain services, **domain policies**.
- **Infrastructure layer** (`{name}-infrastructure/`): JPA entities, JPA↔domain mappers (MapStruct), repository implementations, outbox event producers.
- **Application layer** (`store/`): Controllers, services, command/query handlers, mappers, assemblers, event listeners, **application policies**.

---

## R2. Layered Architecture + CQRS-Light

### Layers

| Layer | Location | Responsibility |
|---|---|---|
| Controller | `store/.../api/rest/controller/` | HTTP in/out. Delegates to services. Returns `ResponseEntity<EntityModel<T>>` or `ResponseEntity<PagedModel<EntityModel<T>>>`. **No business logic.** |
| Service | `store/.../api/rest/service/` | Orchestrates DTO→Command/Query mapping, dispatches via bus, maps result→DTO response. **No repository access. No business rules.** |
| Mapper | `store/.../api/rest/mapper/` | MapStruct abstract class: DTO ↔ Command/Query/Result. |
| Handler | `store/.../command/handler/` or `store/.../query/handler/` | Owns the **transaction boundary**. Command handlers use domain `{Domain}Repository`; query handlers use `{Domain}QueryRepository` for reads/search. Delegates business rules to aggregates/policies, returns Result. `@Component`. |
| Policy | `{name}-domain/.../policy/` or `store/.../policy/` | Encodes business rules (see R19). |
| Domain | `{name}-domain/` | Pure domain. Framework-agnostic. |
| Infrastructure | `{name}-infrastructure/` | JPA + persistence concerns. |

### CQRS Data Flow

1. Controller → Service → Mapper(→Command) → CommandBus.dispatch → Handler.handle → (policy/aggregate) → repository.save → Result
2. Controller → Service → Mapper(→Query) → QueryBus.dispatch → Handler.handle → repository.findById → Result
3. For paginated queries: Mapper produces record implementing `Query<Page<Result>>` + `PageableQueryRequest`. Handler returns `Page<Result>`. Service maps `resultPage.map(mapper::toResponse)`.

---

## R3. Controller Rules

- `@RestController` + `@RequestMapping("/api/v1/{resource}")` + `@RequiredArgsConstructor`
- Inject `XxxCommandService`, `XxxQueryService`, `XxxModelAssembler` (entity-level or per-operation)
- Inject `PagedResourcesAssembler<ResponseDto>` as method param for paginated endpoints
- **Return types:** `ResponseEntity<EntityModel<T>>` (single), `ResponseEntity<PagedModel<EntityModel<T>>>` (paginated), or `ResponseEntity<Void>` + `Location` header (creation without body)
- Use `@Valid @RequestBody`, `@RequestHeader(value = "X-Actor-Id")`
- **NO business logic** — delegate everything to services

---

## R4. Service Rules

- `@Service` + `@RequiredArgsConstructor`
- **CommandService:** inject `CommandBus` + `XxxRequestMapper`. Flow: `mapper.toCommand(dto) → commandBus.dispatch(command) → mapper.toResponse(result)`. Returns `XxxResponse`.
- **QueryService:** inject `QueryBus` + `XxxRequestMapper`. Single: `mapper.toQuery(id) → queryBus.dispatch(query) → mapper.toResponse(result)`. Paginated list: `mapper.toQuery(filters, pageable) → queryBus.dispatch(query) → resultPage.map(mapper::toResponse)`.
- **MUST NOT** inject or call repositories (domain or infrastructure).
- **MUST NOT** contain business rules — those belong in policies / aggregates.

---

## R5. Mapper Rules

- **One mapper class per handler/operation.** File: `store/.../api/rest/mapper/{Action}{Entity}RequestMapper.java`
- Annotate with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`
- Declare as `public abstract class` (not interface)
- Provide exactly 2 methods: `toCommand(params...) → Command` / `toQuery(params...) → Query`, and `toResponse(Result) → ResponseDto`

---

## R6. Command / Query / Result Records

- Java `record` implementing `Command<R>` or `Query<R>`
- Use `Id` (from framework) for entity identifiers, not raw `String`
- For paginated queries: `R = Page<XxxResult>`, record also implements `PageableQueryRequest`
- Result records use primitives / `String` / `Id` — **NOT** domain aggregates

---

## R7. Handler Rules

- **CommandHandler:** `@Component` + `@RequiredArgsConstructor`, implements `CommandHandler<C,R>`. Use `@{Module}Transactional` for writes.
- **QueryHandler:** `@Component` + `@RequiredArgsConstructor`, implements `QueryHandler<Q,R>`. Use `@{Module}ReadTransactional` for reads.
- Must implement both `handle(...)` and `getCommandType()` / `getQueryType()`.
- **Handlers own the transaction — and only that orchestration.** They load aggregates, invoke policies/aggregate methods, persist, and map to Result. They do **not** embed business rules inline.
- **A handler MUST NOT call another handler.** Compose work by dispatching another command/query via `CommandBus` / `QueryBus` (typically from an event listener), or by calling domain/application policies and repositories directly within the same handler.
- **Only handlers may use repositories.** Controllers, services, mappers, assemblers, and policies MUST NOT inject or call repositories. Event listeners cascade via `CommandBus`/`QueryBus`, not via repositories.
- **Command handlers** inject domain `{Domain}Repository`. **Query handlers** for list/search/paged reads inject `{Domain}QueryRepository` (see R11). Neither injects Spring Data `*JpaRepository` directly.

---

## R8. HATEOAS & Model Assembler Rules

### Implementation
- `@Component`, implements `RepresentationModelAssembler<ResponseDto, EntityModel<ResponseDto>>`
- Use `linkTo(methodOn(XxxController.class).methodName(...))` — never manual URL strings (except Tier 2 root endpoints)
- Pass `null` for `@RequestBody`, `@RequestHeader`, `Pageable`, `PagedResourcesAssembler` params in `methodOn()`

### Assembler granularity
- **Entity-level:** `{Entity}ModelAssembler` — shared links for a resource representation reused across endpoints (e.g. `CategoryModelAssembler`, `LocationModelAssembler`).
- **Per-operation:** `{Action}{Entity}ModelAssembler` — when the response DTO or link set is operation-specific (catalog pattern: `DeleteVariantModelAssembler`, `UpdateProductModelAssembler`, `GetProductBySlugModelAssembler`). Prefer per-operation assemblers when command/query results have distinct shapes or navigation.
- Both styles are valid; choose based on whether links/DTO are shared or operation-specific.

### Rel Naming (INLINE string literals — no `LinkRelations` constants class)
| Link Type | Rel Pattern |
|---|---|
| Self | `self` via `.withSelfRel()` |
| Retrieve | `get-{entity}` |
| List collection | `list-{entities}` |
| Search/filter | `search-{entities}` |
| Create | `create-{entity}` |
| Update | `update-{entity}` |
| Delete | `delete-{entity}` |
| State transition | `{action}-{entity}` (e.g., `activate-xxx`, `deactivate-xxx`) |
| Qualified action | `{action}-{entity}-{qualifier}` |
| Subresource | `get-{entity}-{subresource}`, `list-{entity}-{subresources}` |

Rules: kebab-case, action-first, plural for collections, never `paged-*`, never `edit-*`, never bare entity nouns.

### Self-link rule
- `self` only for canonical GET endpoint that returns the exact representation
- Command-result DTOs: use `get-{entity}` rel instead

### PagedModel links
- Every `PagedModel` MUST expose `list-{entities}` link
- Add `create-{entity}` when creation is available
- Add links on `PagedModel` after calling `pagedAssembler.toModel()`

### 3-Tier API Discovery
1. **Tier 1** (`ApiRootController` at `GET /api/v1`): links to all bounded context roots
2. **Tier 2** (`{Context}RootController` at `GET /api/v1/{context}`): links to top-level resources
3. **Tier 3**: Individual resource endpoints
- New bounded context = new Tier 2 root + update `ApiRootController`
- Root endpoints return `ResponseEntity<RepresentationModel<?>>` with `MediaTypes.HAL_JSON_VALUE`

### Bare EntityModel
- `EntityModel.of(dto)` without links is acceptable for bulk/utility/audit endpoints
- Prefer adding a meaningful navigation link

---

## R9. DTO Rules

- All DTOs are Java `record` types
- Request DTOs: Jakarta Bean Validation annotations (`@NotBlank`, `@Min`, etc.)
- Response DTOs: primitive types + `String` only — no domain types
- Controllers MUST accept/return DTOs, never domain aggregates or JPA entities

---

## R10. Domain Rules

- Framework-agnostic (no Spring/JPA/MapStruct annotations)
- All state mutations go through aggregate methods that enforce invariants
- Domain events accumulated via `addEvent()`, pulled via `pullEvents()`
- **Strict DDD:** Aggregates must NOT be anemic. Invariants live on the Aggregate; reusable / cross-cutting decision rules live in **Domain Policies** (or Domain Services when stateful coordination is required).
- Handlers MUST NOT contain business rules — they orchestrate aggregates + policies inside a transaction.

---

## R11. Infrastructure Rules

### Mapping
- **EntityMapper** (MapStruct): JPA Entity ↔ Domain Aggregate fields
- **JpaAssembler** (manual): complex assembly from multiple JPA entities into domain aggregate
- Overall **Mapper** coordinates EntityMapper + JpaAssembler

### Repository package layout (`{name}-infrastructure/.../repository/jpa/`)

Separate **write** and **query** concerns. For each aggregate/entity root:

| Artifact | Location | Role |
|---|---|---|
| Domain write port | `{name}-domain/.../repository/{Domain}Repository` | Aggregate load/save/delete API (framework-agnostic) |
| Write impl | `.../repository/jpa/impl/Default{Domain}Repository` (or `{Domain}RepositoryImpl`) | Implements the **domain** `{Domain}Repository` |
| Query port | `.../repository/jpa/{Domain}QueryRepository` | Read/search API returning **view** records (not aggregates) |
| Query impl | `.../repository/jpa/impl/Default{Domain}QueryRepository` (or `{Domain}QueryRepositoryImpl`) | Implements `{Domain}QueryRepository` |
| Spring Data JPA | `.../repository/jpa/{Domain}JpaRepository` | `JpaRepository` (+ optional `@Query` JPQL). Used **inside** write/query impls only |
| Specification | `.../specification/jpa/{Domain}*Specification` (or search criteria + criteria builder) | Criteria API predicates for paged search |
| View | `.../view/{Domain}View` (or `{Domain}Summary`, etc.) | Read-model record returned by query repos |

### Write repository (`{Domain}Repository`)
- Domain module declares `{Domain}Repository` (aggregates in / aggregates out).
- Infrastructure provides the **only** implementation of that domain interface.
- Impl injects `{Domain}JpaRepository` + mapper/assembler (+ `PersistenceExecutor` / event producer as needed).
- Used by **command handlers** (and query handlers only when loading a full aggregate for a single-get that maps from domain).

### Query repository (`{Domain}QueryRepository`)
- Declared and implemented entirely in infrastructure for separation of concern — **not** a domain interface.
- Has its **own** implementation class (do not fold query methods into the write repository impl).
- Impl injects `{Domain}JpaRepository` and uses it for fixed JPQL / derived queries defined on the JPA interface.
- For **paged search / filtered list** queries: inject a **specification** class and **must** build the query via Criteria API through that specification — never ad-hoc criteria inside the handler.
- Returns **view record** types (`{Domain}View` / summary records), never domain aggregates or JPA entities to the application layer.

### Specification (paged search)
- Every paged search/list query **MUST** go through a specification class injected into the query repository impl.
- Specification encapsulates Criteria API (`CriteriaBuilder` / predicates / joins) — or an equivalent dedicated search-query helper under `specification/jpa/` — given filter criteria + `Pageable`.
- Result shape is a view/summary record suitable for mapping to `Query` Result → Response DTO.

### Handler usage
- **Command handlers** → domain `{Domain}Repository` only.
- **Query handlers** (list/search/paged) → `{Domain}QueryRepository` interface only.
- Handlers MUST NOT inject `{Domain}JpaRepository` or EntityManager directly.
- Controllers, services, mappers, assemblers, and policies MUST NOT use any repository (see R7).

---

## R12. Error Handling Rules

- Module-specific `sealed interface {Module}ServiceError extends MessageSource` with error record subtypes
- Each error record has `kind()` → `ErrorCategory`, `code()` → i18n key, `args()`
- Error code convention: `{module-prefix}.{layer}.{entity}.{error_type}` (e.g., `inv.service.location.not_found`)
- Module exception extends `DomainException`, caught by `GlobalApiExceptionHandler` (`@RestControllerAdvice`)
- Error responses: RFC 7807 `ProblemDetail` with custom fields (`code`, `args`, `traceId`, `module`, `retryable`, `retryAfterMs`)

---

## R13. Transaction Rules

- Each module has its own `DataSource` + `TransactionManager` (multi-datasource)
- Custom meta-annotations: `@{Module}Transactional` (read-write), `@{Module}ReadTransactional` (read-only)
- **Transactions at handler level only** — not at service, controller, policy, or mapper
- A handler's job is to demarcate the transactional unit of work; business decisions inside that unit belong to aggregates/policies

---

## R14. Event Handling

- Intra-module: `@TransactionalEventListener` in `store/.../event/`
- Cross-module: Transactional Outbox via `outbox-infrastructure` (at-least-once delivery), exposed through Modulith **named interfaces** (see R1)
- Event listener classes dispatch cascading commands via `CommandBus` — they MUST NOT call handlers or repositories directly

---

## R15. Naming Conventions

| Artifact | Pattern |
|---|---|
| Controller | `{Entity}Controller` |
| Command/Query Service | `{Entity}CommandService` / `{Entity}QueryService` |
| Request DTO | `{Action}{Entity}Request` |
| Response DTO | `{Entity}Response` |
| Command record | `{Action}{Entity}Command` |
| Query record | `Get{Entity}Query` / `List{Entities}Query` |
| Command/Query Handler | `{Action}{Entity}CommandHandler` / `{Action}{Entity}QueryHandler` |
| Mapper | `{Action}{Entity}RequestMapper` |
| Model Assembler (entity) | `{Entity}ModelAssembler` |
| Model Assembler (per-operation) | `{Action}{Entity}ModelAssembler` |
| Domain Policy | `{Capability}Policy` under `{name}-domain/.../policy/` |
| Application Policy | `{Capability}Policy` under `store/.../{module}/internal/policy/` |
| Domain write repository | `{Domain}Repository` (domain) / `Default{Domain}Repository` (infra impl) |
| Query repository | `{Domain}QueryRepository` / `Default{Domain}QueryRepository` |
| Spring Data JPA | `{Domain}JpaRepository` |
| Query specification | `{Domain}*Specification` (or `{Domain}SearchCriteria` + criteria helper) under `specification/jpa/` |
| Read view | `{Domain}View` / `{Domain}Summary` under `view/` |
| API Root | `ApiRootController` |
| Bounded Context Root | `{Context}RootController` |
| Modulith named interface package | `{module}.events` + `@NamedInterface("events")` |
| Test method | `{functionName}_{input}_{expectedBehavior}` (e.g., `save_withValidValue_shouldSaveToDatabase`) |

---

## R16. Logging

- Infrastructure and application layers use: `private static final Logger log = Loggers.getLogger(<ClassName>.class);`

---

## R17. Coding Style

- **Extract intermediate variables** — no nested function invocations like `doSomething(doA(doB()))`. Use descriptive intermediate variables for readability.

---

## R18. HATEOAS Configuration Constants

- No custom `HalConfiguration` bean, no `CurieProvider`, no Affordances API usage
- Only relevant config: `server.forward-headers-strategy: framework` (for correct URLs behind proxies)
- Dependency: `spring-boot-starter-hateoas`

---

## R19. Policy Rules

Business rules are controlled by **policies**, not by handlers, services, or controllers. There are two policy types:

### Domain policies (`{name}-domain/.../policy/`)
- Framework-agnostic pure domain decision rules (authorization of domain actions, placement, registration eligibility, delegation, approval criteria, etc.).
- Operate on domain types (aggregates, value objects, ids/codes) — no Spring, no HTTP, no DTOs, no repositories.
- Example: `RoleDelegationPolicy`, `AccessPlacementPolicy`, `MerchantApprovalPolicy`.

### Application policies (`store/.../{module}/internal/policy/`)
- Application/use-case rules that need application context (security scope, actor/session context, cross-cutting access checks against already-loaded aggregates).
- May use Spring (`@Component`) and application-layer types.
- **MUST NOT** inject repositories — receive needed data from the calling handler (or other already-resolved inputs).
- Example: `InventoryLocationAccessPolicy`, `MerchantApprovalAccessPolicy`.

### Usage
- Handlers invoke policies, then apply aggregate mutations / persistence.
- Prefer domain policies when the rule is intrinsic to the bounded context; use application policies when the rule depends on app/security/orchestration context.

---

## R20. Before Generating Code — Verify

1. Controller delegates to CommandService/QueryService — no inline logic
2. MapStruct mapper abstract class exists per Command/Query operation, annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`
3. Command/Query records use `Id` for identifiers (not `String`)
4. Handlers are `@Component` implementing `CommandHandler`/`QueryHandler` with proper transactional annotations
5. No business logic in controller, service, or mapper layers — rules live in aggregates/policies
6. Controller returns correct type (`EntityModel<T>` / `PagedModel<EntityModel<T>>`)
7. Model assemblers add HATEOAS links with correct rel naming (entity-level or per-operation)
8. Every `PagedModel` exposes `list-{entities}` (+ `create-{entity}` when applicable)
9. DTOs are Java records in proper `dto/request/` / `dto/response/` packages
10. Errors follow sealed interface pattern with ErrorCategory + i18n codes
11. All files in correct packages per module structure
12. Non-`self` rel names are inline string literals (no `LinkRelations` class)
13. New bounded contexts have Tier 2 root endpoint + linked from `ApiRootController`
14. Controllers inject assemblers directly (call `.toModel()` inline)
15. Logging in infra/app layers uses `Loggers.getLogger()`
16. Intermediate variables extracted (no nested function invocations)
17. No business logic leaks into handlers — aggregate or policy must own business rules
18. Handler does not call another handler; cascading work goes through CommandBus/QueryBus (usually from event listeners)
19. Only handlers inject/use repositories — never services, controllers, mappers, assemblers, or policies
20. Write path: command handler → domain `{Domain}Repository` impl; query list/search → `{Domain}QueryRepository` (not JpaRepository)
21. Paged search uses a specification class injected into the query repository impl; results are view records
22. Cross-module events use named interfaces (`{module}::events`) and consuming modules list them in `allowedDependencies`
23. `shared` remains `@ApplicationModule(type = OPEN)`
