# Architectural Rules (Grab E-Commerce)

You MUST follow every rule below when generating, reviewing, or refactoring code. These rules override any conflicting general coding conventions.

---

## R1. Module Structure & Dependencies

- **Module layout:** `{name}-domain/` → `{name}-infrastructure/` → `store/`
- **Dependency direction:** `framework ← domain ← infrastructure ← store`, `framework ← outbox-infrastructure ← store`, `framework ← logger-slf4j ← store`
- **Spring Modulith:** Each module has `@ApplicationModule(allowedDependencies = "shared")` marker class. Internal package goes under `internal/`. Cross-module communication is via domain events only — no direct method calls.
- **Domain layer** (`{name}-domain/`): NO Spring, JPA, or MapStruct annotations. Contains aggregates, entities, value objects, events, repository interfaces, domain services.
- **Infrastructure layer** (`{name}-infrastructure/`): JPA entities, JPA↔domain mappers (MapStruct), repository implementations, outbox event producers.
- **Application layer** (`store/`): Controllers, services, command/query handlers, mappers, assemblers, event listeners.

---

## R2. Layered Architecture + CQRS-Light

### Layers

| Layer | Location | Responsibility |
|---|---|---|
| Controller | `store/.../api/rest/controller/` | HTTP in/out. Delegates to services. Returns `ResponseEntity<EntityModel<T>>` or `ResponseEntity<PagedModel<EntityModel<T>>>`. **No business logic.** |
| Service | `store/.../api/rest/service/` | Orchestrates DTO→Command/Query mapping, dispatches via bus, maps result→DTO response. |
| Mapper | `store/.../api/rest/mapper/` | MapStruct abstract class: DTO ↔ Command/Query/Result. |
| Handler | `store/.../command/handler/` or `store/.../query/handler/` | Contains business logic. Interacts with domain aggregates and repositories. `@Component`. |
| Domain | `{name}-domain/` | Pure domain. Framework-agnostic. |
| Infrastructure | `{name}-infrastructure/` | JPA + persistence concerns. |

### CQRS Data Flow

1. Controller → Service → Mapper(→Command) → CommandBus.dispatch → Handler.handle → aggregate ops → repository.save → Result
2. Controller → Service → Mapper(→Query) → QueryBus.dispatch → Handler.handle → repository.findById → Result
3. For paginated queries: Mapper produces record implementing `Query<Page<Result>>` + `PageableQueryRequest`. Handler returns `Page<Result>`. Service maps `resultPage.map(mapper::toResponse)`.

---

## R3. Controller Rules

- `@RestController` + `@RequestMapping("/api/v1/{resource}")` + `@RequiredArgsConstructor`
- Inject `XxxCommandService`, `XxxQueryService`, `XxxModelAssembler`
- Inject `PagedResourcesAssembler<ResponseDto>` as method param for paginated endpoints
- **Return types:** `ResponseEntity<EntityModel<T>>` (single), `ResponseEntity<PagedModel<EntityModel<T>>>` (paginated), or `ResponseEntity<Void>` + `Location` header (creation without body)
- Use `@Valid @RequestBody`, `@RequestHeader(value = "X-Actor-Id")`
- **NO business logic** — delegate everything to services

---

## R4. Service Rules

- `@Service` + `@RequiredArgsConstructor`
- **CommandService:** inject `CommandBus` + `XxxRequestMapper`. Flow: `mapper.toCommand(dto) → commandBus.dispatch(command) → mapper.toResponse(result)`. Returns `XxxResponse`.
- **QueryService:** inject `QueryBus` + `XxxRequestMapper`. Single: `mapper.toQuery(id) → queryBus.dispatch(query) → mapper.toResponse(result)`. Paginated list: `mapper.toQuery(filters, pageable) → queryBus.dispatch(query) → resultPage.map(mapper::toResponse)`.

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

---

## R8. HATEOAS & Model Assembler Rules

### Implementation
- `@Component`, implements `RepresentationModelAssembler<ResponseDto, EntityModel<ResponseDto>>`
- Use `linkTo(methodOn(XxxController.class).methodName(...))` — never manual URL strings (except Tier 2 root endpoints)
- Pass `null` for `@RequestBody`, `@RequestHeader`, `Pageable`, `PagedResourcesAssembler` params in `methodOn()`

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
- **Strict DDD:** Aggregates must NOT be anemic. Business rules live in Aggregate or Domain Service, NEVER in handlers.
- Complex cross-aggregate rules → Domain Policy classes or Domain Services

---

## R11. Infrastructure Rules

- **EntityMapper** (MapStruct): JPA Entity ↔ Domain Aggregate fields
- **JpaAssembler** (manual): complex assembly from multiple JPA entities into domain aggregate
- Overall **Mapper** coordinates EntityMapper + JpaAssembler

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
- **Transactions at handler level only** — not at service or controller

---

## R14. Event Handling

- Intra-module: `@TransactionalEventListener` in `store/.../event/`
- Cross-module: Transactional Outbox via `outbox-infrastructure` (at-least-once delivery)
- Event listener classes dispatch cascading commands via `CommandBus`

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
| Model Assembler | `{Entity}ModelAssembler` |
| API Root | `ApiRootController` |
| Bounded Context Root | `{Context}RootController` |
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

## R19. Before Generating Code — Verify

1. Controller delegates to CommandService/QueryService — no inline logic
2. MapStruct mapper abstract class exists per Command/Query operation, annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`
3. Command/Query records use `Id` for identifiers (not `String`)
4. Handlers are `@Component` implementing `CommandHandler`/`QueryHandler` with proper transactional annotations
5. No business logic in controller, service, or mapper layers
6. Controller returns correct type (`EntityModel<T>` / `PagedModel<EntityModel<T>>`)
7. Model assemblers add HATEOAS links with correct rel naming
8. Every `PagedModel` exposes `list-{entities}` (+ `create-{entity}` when applicable)
9. DTOs are Java records in proper `dto/request/` / `dto/response/` packages
10. Errors follow sealed interface pattern with ErrorCategory + i18n codes
11. All files in correct packages per module structure
12. Non-`self` rel names are inline string literals (no `LinkRelations` class)
13. New bounded contexts have Tier 2 root endpoint + linked from `ApiRootController`
14. Controllers inject assemblers directly (call `.toModel()` inline)
15. Logging in infra/app layers uses `Loggers.getLogger()`
16. Intermediate variables extracted (no nested function invocations)
17. No business logic leaks into handlers — aggregate or domain service must own business rules
