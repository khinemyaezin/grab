# Grab E-Commerce — Architectural Guideline Prompt Template

You are an expert software architect and AI coding agent for the **Grab E-Commerce** project. You must strictly follow the guidelines below for all code generation, refactoring, and feature implementation.

---

## 1. Project Overview

This is a **Java 21 / Spring Boot 3.3** multi-module Maven monorepo for an e-commerce platform. It is modular by domain (Catalog, Inventory) and uses **Spring Modulith** for module isolation.

**Tech Stack:**
- Java 21, Spring Boot 3.3.4, Spring Modulith 1.2.4
- Spring HATEOAS for hypermedia-driven REST APIs
- MapStruct 1.5.3 + Lombok 1.18.34 for mapping & boilerplate
- JPA / Hibernate for persistence
- Transactional Outbox pattern for reliable domain event publishing
- Custom CQRS framework (`framework` module)
- Custom Logger SPI (`logger-slf4j` module)

---

## 2. Module Structure

```text
grab/ (root POM — packaging: pom)
├── framework/                    # Shared kernel: CQRS buses, domain primitives, Id, exceptions, specifications
├── logger-slf4j/                 # Logger SPI implementation (SLF4J adapter)
├── outbox-infrastructure/        # Transactional outbox infrastructure (JPA-based)
├── catalog-domain/               # Catalog bounded context — domain layer (aggregates, entities, value objects, events, repos)
├── catalog-infrastructure/       # Catalog bounded context — infrastructure layer (JPA entities, mappers, repos)
├── inventory-domain/             # Inventory bounded context — domain layer
├── inventory-infrastructure/     # Inventory bounded context — infrastructure layer
└── store/                        # Application module — Spring Boot app, REST controllers, CQRS services, handlers
    └── com.grab.store
        ├── shared/               # Cross-cutting: GlobalApiExceptionHandler, CqrsConfiguration, OpenApiConfiguration
        ├── catalog/              # Catalog Spring Modulith module (@ApplicationModule)
        │   └── internal/
        │       ├── api/rest/     # Controllers, DTOs, Services, Mappers, Assemblers
        │       ├── command/      # Command records + handler/ subdirectory
        │       ├── query/        # Query records + handler/ subdirectory
        │       ├── config/       # Module-specific DataSource, @Transactional annotations
        │       ├── event/        # Domain event listeners
        │       └── exception/    # Module-specific error codes + exception class
        └── inventory/            # Inventory Spring Modulith module (@ApplicationModule)
            └── internal/         # (same structure as catalog)
```

### Module Dependency Graph (Maven)
```text
framework ← catalog-domain ← catalog-infrastructure ← store
framework ← inventory-domain ← inventory-infrastructure ← store
framework ← outbox-infrastructure ← store
framework ← logger-slf4j ← store
```

### Spring Modulith Rules
- Each module has a marker class annotated with `@ApplicationModule(allowedDependencies = "shared")`.
- All internal package contents are encapsulated under `internal/` — they are not accessible to other modules.
- Cross-module communication is done via domain events, NOT direct method calls.

---

## 3. Architectural Style: Layered + CQRS-Light

### 3.1 Layers (top to bottom)

| Layer | Location | Responsibility |
|---|---|---|
| **Controller** | `store/.../api/rest/controller/` | HTTP in/out. Delegates to services. Returns `ResponseEntity<EntityModel<T>>` or `ResponseEntity<PagedModel<EntityModel<T>>>`. No business logic. |
| **Service (Command/Query)** | `store/.../api/rest/service/` | Orchestrates DTO → Command/Query mapping, dispatches via bus, maps result → DTO response. |
| **Mapper (DTO ↔ CQRS)** | `store/.../api/rest/mapper/` | MapStruct abstract classes converting between REST DTOs and Command/Query/Result records. |
| **Command/Query Handler** | `store/.../command/handler/` or `store/.../query/handler/` | Contains business logic. Interacts with domain aggregates and repositories. Annotated with `@Component`. |
| **Domain** | `{module}-domain/` | Pure domain: aggregates, entities, value objects, domain events, repository interfaces, domain services. Framework-agnostic (no Spring annotations). |
| **Infrastructure** | `{module}-infrastructure/` | JPA entities, JPA ↔ domain mappers, repository implementations, outbox event producers. |

### 3.2 CQRS Data Flow

#### Command (Write) Flow:
```text
Controller
  → CommandService.method(RequestDto, ...)
    → XxxRequestMapper.toCommand(requestDto, ...) → XxxCommand
    → commandBus.dispatch(command)
      → XxxCommandHandler.handle(command)
        → domain aggregate operations
        → repository.save(aggregate)
        → returns XxxResult
    → XxxRequestMapper.toResponse(result) → ResponseDto
  → inventoryModelAssembler.toModel(responseDto) → EntityModel<ResponseDto>
```

#### Query (Read) Flow — Single Entity:
```text
Controller
  → QueryService.method(id)
    → XxxRequestMapper.toQuery(id) → XxxQuery
    → queryBus.dispatch(query)
      → XxxQueryHandler.handle(query)
        → repository.findById(...)
        → returns XxxResult
    → XxxRequestMapper.toResponse(result) → ResponseDto
  → modelAssembler.toModel(responseDto) → EntityModel<ResponseDto>
```

#### Query (Read) Flow — Paginated List:
```text
Controller
  → QueryService.method(filters, Pageable)
    → XxxRequestMapper.toQuery(filters, pageable) → XxxQuery (implements Query<Page<XxxResult>>)
    → queryBus.dispatch(query) → Page<XxxResult>
    → resultPage.map(mapper::toResponse) → Page<ResponseDto>
  → pagedResourcesAssembler.toModel(page, modelAssembler) → PagedModel<EntityModel<ResponseDto>>
```

---

## 4. Controller Layer Rules

- Annotated with `@RestController`, `@RequestMapping("/api/v1/{resource}")`, `@RequiredArgsConstructor`.
- Injects: `XxxCommandService`, `XxxQueryService`, and one or more `XxxModelAssembler` beans.
- For paginated endpoints, also injects `PagedResourcesAssembler<ResponseDto>` as a method parameter.
- **MUST return** `ResponseEntity<EntityModel<T>>` for single-entity responses.
- **MUST return** `ResponseEntity<PagedModel<EntityModel<T>>>` for collection/paginated responses.
- Uses `@Valid @RequestBody` for request validation.
- Uses `@RequestHeader(value = "X-Actor-Id")` for actor/seller identification.
- **MUST NOT contain any business logic.**

### Example Pattern:
```java
@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryCommandService inventoryCommandService;
    private final InventoryQueryService inventoryQueryService;
    private final InventoryModelAssembler inventoryModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<InventoryResponse>> createInventory(
            @Valid @RequestBody CreateInventoryRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        InventoryResponse response = inventoryCommandService.createInventory(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryModelAssembler.toModel(response));
    }

    @GetMapping("/{id}/movements")
    public ResponseEntity<PagedModel<EntityModel<StockMovementResponse>>> getMovements(
            @PathVariable String id,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<StockMovementResponse> pagedAssembler) {
        Page<StockMovementResponse> page = inventoryQueryService.getMovements(id, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, movementAssembler));
    }
}
```

---

## 5. Service Layer Rules

### 5.1 CommandService
- Annotated with `@Service`, `@RequiredArgsConstructor`.
- Injects: `CommandBus` and one `XxxRequestMapper` per command operation.
- Each method follows: `mapper.toCommand(dto) → commandBus.dispatch(command) → mapper.toResponse(result)`.
- Returns a single DTO response (`XxxResponse`).

### 5.2 QueryService
- Annotated with `@Service`, `@RequiredArgsConstructor`.
- Injects: `QueryBus` and one `XxxRequestMapper` per query operation.
- **Single entity**: `mapper.toQuery(id) → queryBus.dispatch(query) → mapper.toResponse(result)` → returns `XxxResponse`.
- **Paginated list**: `mapper.toQuery(filters, pageable) → queryBus.dispatch(query)` → returns `Page<XxxResult>`, then `resultPage.map(mapper::toResponse)` → returns `Page<XxxResponse>`.

### 5.3 FacadeService (Catalog variant)
- The Catalog module uses a `FacadeService` pattern (e.g., `ProductFacadeService`) that combines both command and query service orchestration with model assembler wrapping, returning `EntityModel<T>` directly.
- For new Inventory-style modules, prefer the separated `CommandService` / `QueryService` pattern.

---

## 6. Mapper Layer Rules

- Located at: `store/.../api/rest/mapper/`
- **One mapper class per handler/operation** (e.g., `CreateInventoryRequestMapper`, `GetInventoryMovementsRequestMapper`).
- Annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
- Declared as `public abstract class` (not interface), allowing MapStruct to generate implementations.
- Provides exactly **two methods**:
  - `toCommand(RequestDto, ...) → Command` or `toQuery(params...) → Query`
  - `toResponse(Result) → ResponseDto`
- `IdMapper` handles `String ↔ Id` conversions automatically via MapStruct's `uses` mechanism.

### CentralMapperConfig:
```java
@MapperConfig(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface CentralMapperConfig {}
```

> **Note:** Each module (`store/catalog`, `store/inventory`, `catalog-infrastructure`, `inventory-infrastructure`) has its own `CentralMapperConfig` in its respective mapper package.

### Example:
```java
@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateInventoryRequestMapper {
    public abstract CreateInventoryCommand toCommand(CreateInventoryRequest request, String createdBy);
    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
```

---

## 7. Command / Query Records

### 7.1 Command Record
- Located at: `store/.../command/`
- Java `record` implementing `Command<R>` where `R` is the result type.
- Uses `Id` (from framework) for entity identifiers, not raw `String`.

```java
public record CreateInventoryCommand(
    String sku, Id sellerId, Id productVariantId, Id locationId,
    int initialQuantity, Integer safetyStock, ...
) implements Command<InventoryItemResult> {}
```

### 7.2 Query Record
- Located at: `store/.../query/`
- Java `record` implementing `Query<R>` where `R` is the result type.
- For paginated queries: `R = Page<XxxResult>`, and the query record also implements `PageableQueryRequest`.

```java
public record ListLocationsQuery(
    Id sellerId, Boolean active, LocationType type, Pageable pageable
) implements Query<Page<ListLocationsResult>>, PageableQueryRequest {}
```

### 7.3 Result Record
- Located alongside commands/queries at: `store/.../command/` or `store/.../query/`.
- Plain Java `record`. Uses primitive types / `String` / `Id` — NOT domain aggregates.

---

## 8. Handler Rules

### 8.1 CommandHandler
- Located at: `store/.../command/handler/`
- Annotated with `@Component`, `@RequiredArgsConstructor`.
- Implements `CommandHandler<C extends Command<R>, R>`.
- Must implement `handle(C command)` and `getCommandType()`.
- Uses `@InventoryTransactional` (or `@CatalogTransactional`) for write operations.
- Interacts with **domain aggregates** and **domain repository interfaces**.

### 8.2 QueryHandler
- Located at: `store/.../query/handler/`
- Annotated with `@Component`, `@RequiredArgsConstructor`.
- Implements `QueryHandler<Q extends Query<R>, R>`.
- Must implement `handle(Q query)` and `getQueryType()`.
- Uses `@InventoryReadTransactional` (or `@CatalogReadTransactional`) for read-only operations.

---

## 9. Model Assembler Rules (HATEOAS)

- Located at: `store/.../api/rest/assembler/`
- Annotated with `@Component`.
- Implements `RepresentationModelAssembler<ResponseDto, EntityModel<ResponseDto>>`.
- Uses static imports from `WebMvcLinkBuilder`: `linkTo(methodOn(...))`.
- **One assembler per response DTO** — named `{Entity}ModelAssembler`.

### 9.1 Required Imports
```java
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
```

### 9.2 Link Types

Every model assembler MUST add appropriate links following these conventions:

| Link Type | Rel Name | When to Add |
|---|---|---|
| **Self link** | `self` | Always — points to the GET endpoint for this specific resource. Use `.withSelfRel()`. |
| **CRUD action links** | `create`, `update`, `delete` | When the resource supports these operations. Pass `null` for request body / header params in `methodOn(...)`. |
| **Related resource links** | Descriptive name (e.g., `zones`, `movements`) | When the resource has navigable sub-resources or related collections. May link to a different controller. |
| **Conditional state links** | Action name (e.g., `activate`, `deactivate`) | When an action is available only in a specific entity state (e.g., `active` vs `inactive`). Use `if/else` on response fields. |

### 9.3 Rel Naming Convention

The `withRel("...")` name is the **API contract** for link discovery. Follow these rules strictly:

| Rule | Convention | Good ✅ | Bad ❌ |
|---|---|---|---|
| **Self link** | Always `self` via `.withSelfRel()` | `self` | `inventory`, `getInventory` |
| **Navigation (single resource)** | Singular noun — the related resource type | `location`, `zone`, `inventory` | `getLocation`, `fetchZone` |
| **Navigation (collection)** | Plural noun — the related collection type | `zones`, `movements`, `bins` | `getZones`, `listMovements` |
| **Mutating actions** | Imperative verb (or verb-noun if ambiguous) | `create`, `update`, `delete`, `activate` | `creating`, `doUpdate` |
| **No `get` prefix** | The HTTP method already implies read | `location` | `getLocation` |
| **Multi-word rels** | Lowercase with hyphens (kebab-case) | `stock-movements` | `stockMovements`, `StockMovements` |
| **Never empty** | Every link MUST have a meaningful rel | `reservations` | `""` |

> **Tip:** Think of rels as the _relationship between the current resource and the linked resource_, not as an action the client performs.

### 9.4 Link Building Rules

1. **Use `methodOn(...)` for type-safe links** — always reference the controller method directly; never build URLs manually.
2. **Pass `null` for parameters you don't have** — request bodies (`@RequestBody`), headers (`@RequestHeader`), `Pageable`, and `PagedResourcesAssembler` should be passed as `null` in `methodOn(...)` calls.
3. **Cross-controller links are allowed** — a model assembler may link to endpoints in a different controller (e.g., `LocationModelAssembler` linking to `ZoneController.listZones()`).
4. **Conditional links based on entity state** — use `if/else` on response DTO fields to add state-dependent action links (e.g., only show `"deactivate"` when `active == true`).
5. **Always include a `self` link** — every `toModel()` must add `.withSelfRel()` pointing to the GET endpoint for the resource.

### 9.5 Simple Example (Single Entity)
```java
@Component
public class InventoryModelAssembler
        implements RepresentationModelAssembler<InventoryResponse, EntityModel<InventoryResponse>> {
    @Override
    public EntityModel<InventoryResponse> toModel(InventoryResponse response) {
        return EntityModel.of(response,
            linkTo(methodOn(InventoryController.class).getInventory(response.id())).withSelfRel()
        );
    }
}
```

### 9.6 Advanced Example (CRUD + Conditional + Cross-Controller Links)
```java
@Component
public class ZoneModelAssembler
        implements RepresentationModelAssembler<ZoneResponse, EntityModel<ZoneResponse>> {

    @Override
    public EntityModel<ZoneResponse> toModel(ZoneResponse response) {
        EntityModel<ZoneResponse> entity = EntityModel.of(response);

        // Self link
        entity.add(linkTo(methodOn(ZoneController.class)
                .getZoneById(response.id())).withSelfRel());

        // CRUD action links (pass null for @RequestBody and @RequestHeader params)
        entity.add(linkTo(methodOn(ZoneController.class)
                .createZone(response.id(), null, null)).withRel("create"));
        entity.add(linkTo(methodOn(ZoneController.class)
                .updateZone(response.id(), null, null)).withRel("update"));

        // Related resource link (cross-controller: links back to parent's zone list)
        entity.add(linkTo(methodOn(ZoneController.class)
                .listZones(response.locationId(), null, null)).withRel("zones"));

        // Conditional links based on entity state
        if (response.active()) {
            entity.add(linkTo(methodOn(ZoneController.class)
                    .deactivateZone(response.id(), null)).withRel("deactivate"));
        } else {
            entity.add(linkTo(methodOn(ZoneController.class)
                    .activateZone(response.id(), null)).withRel("activate"));
        }

        return entity;
    }
}
```

### 9.7 Cross-Controller Links Example
```java
@Component
public class LocationModelAssembler
        implements RepresentationModelAssembler<LocationResponse, EntityModel<LocationResponse>> {

    @Override
    public EntityModel<LocationResponse> toModel(LocationResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(LocationController.class).getLocation(response.id())).withSelfRel(),
                linkTo(methodOn(ZoneController.class).listZones(response.id(), null, null)).withRel("zones")
        );
    }
}
```

### 9.8 Usage with `PagedResourcesAssembler`

For paginated endpoints, the model assembler is passed to `PagedResourcesAssembler.toModel()` in the controller. The `PagedResourcesAssembler` automatically adds pagination links (`first`, `prev`, `next`, `last`) and delegates per-item link creation to the model assembler.

```java
// In the Controller:
@GetMapping("/{locationId}/zones")
public ResponseEntity<PagedModel<EntityModel<ZoneResponse>>> listZones(
        @PathVariable String locationId,
        @PageableDefault(size = 20) Pageable pageable,
        PagedResourcesAssembler<ZoneResponse> pagedAssembler) {
    Page<ZoneResponse> page = zoneQueryService.listZones(locationId, pageable);
    return ResponseEntity.ok(pagedAssembler.toModel(page, zoneModelAssembler));
}
```

---

## 10. DTO Rules

- Located at: `store/.../api/rest/dto/request/` and `store/.../api/rest/dto/response/`
- All DTOs are Java `record` types.
- Request DTOs use Jakarta Bean Validation annotations (`@NotBlank`, `@Min`, etc.).
- Response DTOs use primitive types and `String` only — no domain types.
- Controllers MUST accept and return DTOs, **never** domain aggregates or JPA entities.

---

## 11. Domain Layer Rules

### 11.1 Package Structure (`{module}-domain/`)
```text
com.{module}.domain
├── aggregate/        # AggregateRoot subclasses (e.g., InventoryItem, Location, Product)
├── entity/           # Domain entities (e.g., StockMovement, InventoryReservation)
├── valueobject/      # Value objects (e.g., InventoryQuantity, ReorderConfig, Address)
├── enums/            # Domain enums (e.g., InventoryStatus, StockMovementType)
├── repository/       # Repository interfaces (implemented in infrastructure)
├── service/          # Domain services (interfaces + impl/)
├── specification/    # Specification pattern classes
├── event/            # Domain events (emitted by aggregates via addEvent())
└── exception/        # Domain-specific error codes + validation exceptions
```

### 11.2 Domain Primitives (from `framework`)
- **`Entity<ID>`**: Base entity with identity-based equality.
- **`AggregateRoot<ID>`**: Extends Entity, adds domain event collection (`addEvent()`, `pullEvents()`).
- **`Id`**: Typed identifier wrapper (`Id.getValue() → String`). Generated via `IdGenerator`.
- **`ValueObject`**: Marker for value objects.
- **`Event`**: Marker for domain events.

### 11.3 Rules
- Domain layer is **framework-agnostic** — no Spring, JPA, or MapStruct annotations.
- All state mutations go through aggregate methods that enforce invariants.
- Domain events are accumulated via `addEvent()` and pulled via `pullEvents()`.
- Repository interfaces define contracts; implementations live in `{module}-infrastructure/`.

---

## 12. Infrastructure Layer Rules

### 12.1 Package Structure (`{module}-infrastructure/`)
```text
com.{module}.infrastructure
├── config/           # Spring config (DataSource, etc.)
├── entity/           # JPA entities (e.g., InventoryItemEntity, LocationEntity)
│   └── meta/         # JPA static metamodel classes (Entity_ classes)
├── mapper/jpa/       # JPA ↔ Domain mappers (MapStruct-based)
│   └── impl/         # Manual JpaAssembler implementations for complex mappings
├── repository/jpa/   # Spring Data JPA repositories + query repositories
│   └── impl/         # Repository interface implementations (adapt JPA repos to domain repos)
├── view/             # Read-only projection/view records (used by query repositories)
├── outbox/           # Outbox event processing (OutboxEventProducer, OutboxEventProcessor)
└── exception/        # Infrastructure-specific errors
```

### 12.2 Mapping Layers in Infrastructure
- **EntityMapper**: MapStruct interface — maps between JPA Entity ↔ Domain Aggregate fields.
- **JpaAssembler**: Complex assembly logic (e.g., combining multiple JPA entities into a domain aggregate).
- **Mapper (overall)**: Coordinates EntityMapper + JpaAssembler for full conversion.

---

## 13. Exception & Error Handling

### 13.1 Error Code Pattern
- Each module defines a `sealed interface XxxServiceError extends MessageSource` with specific error `record` subtypes.
- Each error record provides `kind()` (→ `ErrorCategory`), `code()` (→ i18n key like `"inv.service.location.not_found"`), and `args()`.
- Error categories map to HTTP statuses: `NOT_FOUND → 404`, `CONFLICT → 409`, `BUSINESS_RULE → 422`, `BAD_REQUEST → 400`, `INTERNAL → 500`.

### 13.2 Exception Classes
- Module-specific exception (e.g., `InventoryServiceException`) extends `DomainException`.
- Thrown in handlers, caught globally by `GlobalApiExceptionHandler` (`@RestControllerAdvice`).
- Error responses use RFC 7807 `ProblemDetail` format with custom fields: `code`, `args`, `traceId`, `module`, `retryable`, `retryAfterMs`.

### 13.3 Error Code Convention
```text
{module-prefix}.{layer}.{entity}.{error_type}
Examples:
  inv.service.location.not_found
  inv.service.inventory.already_exists
  cat.service.product.not_found
  shr.internal.unexpected
```

---

## 14. Transaction Management

- Each module has its **own `DataSource` and `TransactionManager`** (multi-datasource setup).
- Custom meta-annotations for transactions:
  - `@InventoryTransactional` — wraps `@Transactional("inventoryTransactionManager")` (read-write).
  - `@InventoryReadTransactional` — wraps `@Transactional(transactionManager = "inventoryTransactionManager", readOnly = true)`.
  - Same pattern for Catalog: `@CatalogTransactional`, `@CatalogReadTransactional`.
- **Transactions are applied at the handler level**, not at the service or controller level.

---

## 15. Event Handling

- Domain events are emitted by aggregates (`addEvent()`), pulled during persistence, and published.
- **Intra-module**: `@TransactionalEventListener` in `store/.../event/` package listens for domain events.
- **Cross-module**: Transactional Outbox pattern via `outbox-infrastructure` module ensures at-least-once delivery.
- Event listener classes dispatch cascading commands via the `CommandBus`.

---

## 16. CQRS Framework (`framework` module)

### Key Interfaces:
| Interface | Type Parameter | Method |
|---|---|---|
| `Command<R>` | R = result type | Marker interface |
| `CommandHandler<C, R>` | C extends Command<R> | `R handle(C)`, `Class<C> getCommandType()` |
| `CommandBus` | — | `<R> R dispatch(Command<R>)` |
| `Query<R>` | R = result type | Marker interface |
| `QueryHandler<Q, R>` | Q extends Query<R> | `R handle(Q)`, `Class<Q> getQueryType()` |
| `QueryBus` | — | `<R> R dispatch(Query<R>)` |

### Bus Implementations:
- `DefaultCommandBus` and `DefaultQueryBus` auto-discover all handlers via constructor injection (`List<CommandHandler<?, ?>>`) and register them by command/query type class.
- Duplicate handler registration for the same command/query type throws `IllegalStateException`.

---

## 17. Naming Conventions

| Artifact | Naming Pattern | Example |
|---|---|---|
| Controller | `{Entity}Controller` | `InventoryController` |
| Command Service | `{Entity}CommandService` | `InventoryCommandService` |
| Query Service | `{Entity}QueryService` | `InventoryQueryService` |
| Facade Service | `{Entity}FacadeService` | `ProductFacadeService` |
| Request DTO | `{Action}{Entity}Request` | `CreateInventoryRequest` |
| Response DTO | `{Entity}Response` | `InventoryResponse` |
| Command Record | `{Action}{Entity}Command` | `CreateInventoryCommand` |
| Command Result | `{Entity}Result` or `{Action}Result` | `InventoryItemResult` |
| Query Record | `Get{Entity}Query` / `List{Entities}Query` | `GetInventoryQuery`, `ListLocationsQuery` |
| Query Result | `Get{Entity}Result` / `List{Entities}Result` | `GetInventoryResult`, `ListLocationsResult` |
| Command Handler | `{Action}{Entity}CommandHandler` | `CreateInventoryCommandHandler` |
| Query Handler | `{Action}{Entity}QueryHandler` | `GetInventoryQueryHandler` |
| Mapper | `{Action}{Entity}RequestMapper` | `CreateInventoryRequestMapper` |
| Model Assembler | `{Entity}ModelAssembler` | `InventoryModelAssembler` |
| Service Error | `{Module}ServiceError` (sealed interface) | `InventoryServiceError` |
| Service Exception | `{Module}ServiceException` | `InventoryServiceException` |

### 17.1 Test Case Naming Convention

Test method names MUST follow the pattern: `{functionName}_{input}_{expectedBehavior}`

| Part | Description | Example |
|---|---|---|
| `functionName` | The name of the method under test | `save` |
| `input` | A short description of the input/condition | `withValidValue` |
| `expectedBehavior` | What should happen, prefixed with `should` | `shouldSaveToDatabase` |

**Full example:** `save_withValidValue_shouldSaveToDatabase`

---

## 18. Code Generation Checklist

Before generating or outputting any code, verify:

1. ✅ The controller delegates to the appropriate `CommandService` or `QueryService` — no inline logic.
2. ✅ A MapStruct mapper abstract class exists for every Command/Query operation.
3. ✅ The mapper is annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
4. ✅ Command/Query records use `Id` for identifiers, not raw `String`.
5. ✅ Handlers are `@Component` classes implementing `CommandHandler<C, R>` or `QueryHandler<Q, R>`.
6. ✅ Write handlers use `@{Module}Transactional`; read handlers use `@{Module}ReadTransactional`.
7. ✅ No business logic leaks into the controller, service, or mapper layers.
8. ✅ Controller returns `ResponseEntity<EntityModel<T>>` (single) or `ResponseEntity<PagedModel<EntityModel<T>>>` (paginated).
9. ✅ Model assemblers implement `RepresentationModelAssembler` and add HATEOAS links.
10. ✅ DTOs are Java records in the appropriate `dto/request/` or `dto/response/` package.
11. ✅ Errors follow the sealed interface pattern with `ErrorCategory` and i18n error codes.
12. ✅ All files are placed in the correct package according to the module structure.