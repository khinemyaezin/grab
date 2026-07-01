# Architectural Guideline Prompt Template

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

### 1.1 Core Engineering Philosophy

All generated code and refactoring must enforce **SOLID principles** and appropriate **Design Patterns**, but these MUST be strictly balanced with the **PonyTail** methodology ([PonyTail.md](../../PonyTail.md)):

---

## 2. Module Structure

```text
grab/ (root POM — packaging: pom)
├── framework/                    # Shared kernel: CQRS buses, domain primitives, Id, exceptions, specifications
├── logger-slf4j/                 # Logger SPI implementation (SLF4J adapter)
├── outbox-infrastructure/        # Transactional outbox infrastructure (JPA-based)
├── {name}-domain/                # Bounded context — domain layer (aggregates, entities, value objects, events, repos)
├── {name}-infrastructure/        # Bounded bounded context — infrastructure layer (JPA entities, mappers, repos)
└── store/                        # Application module — Spring Boot app, REST controllers, CQRS services, handlers
    └── com.grab.store
        ├── ApiRootController.java    # Tier 1 API root — hypermedia entry point (GET /api)
        ├── shared/                   # Cross-cutting: GlobalApiExceptionHandler, CqrsConfiguration, OpenApiConfiguration
        ├── {domain-name}/                  # Spring Modulith module (@ApplicationModule)
        │   ├── {domain-name}RootApi.java   # Tier 2 Catalog root (GET /api/v1/{domain-name})
        │   └── internal/
        │       ├── api/rest/     # Controllers, DTOs, Services, Mappers, Assemblers
        │       ├── command/      # Command records + handler/ subdirectory
        │       ├── query/        # Query records + handler/ subdirectory
        │       ├── config/       # Module-specific DataSource, @Transactional annotations
        │       ├── event/        # Domain event listeners
        │       └── exception/    # Module-specific error codes + exception class

```

### Module Dependency Graph (Maven)
```text
framework ← domain ← infrastructure ← store
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
  → xxxModelAssembler.toModel(responseDto) → EntityModel<ResponseDto>
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
- Injects: `XxxCommandService`, `XxxQueryService`, and one or more `XxxModelAssembler` 
- For paginated endpoints, also injects `PagedResourcesAssembler<ResponseDto>` as a method parameter.
- **MUST return** `ResponseEntity<EntityModel<T>>` for single-entity responses.
- **MUST return** `ResponseEntity<PagedModel<EntityModel<T>>>` for collection/paginated responses.
- **MAY return** `ResponseEntity<Void>` with `Location` header for creation endpoints that don't need a response body
- Uses `@Valid @RequestBody` for request validation.
- Uses `@RequestHeader(value = "X-Actor-Id")` for actor/seller identification.
- **MUST NOT contain any business logic.**

### Example Pattern:
```java
@RestController
@RequestMapping("/api/v1/{resources}")
@RequiredArgsConstructor
public class XxxController {
    private final XxxCommandService xxxCommandService;
    private final XxxQueryService xxxQueryService;
    private final XxxModelAssembler xxxModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<XxxResponse>> createXxx(
            @Valid @RequestBody CreateXxxRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        XxxResponse response = xxxCommandService.createXxx(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(xxxModelAssembler.toModel(response));
    }

    @GetMapping("/{id}/yyy")
    public ResponseEntity<PagedModel<EntityModel<YyyResponse>>> listYyy(
            @PathVariable String id,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<YyyResponse> pagedAssembler) {
        Page<YyyResponse> page = xxxQueryService.listYyy(id, pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(page, yyyModelAssembler));
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
public abstract class CreateXxxRequestMapper {
    public abstract CreateXxxCommand toCommand(CreateXxxRequest request, String createdBy);
    public abstract XxxResponse toResponse(XxxResult result);
}
```

---

## 7. Command / Query Records

### 7.1 Command Record
- Located at: `store/.../command/`
- Java `record` implementing `Command<R>` where `R` is the result type.
- Uses `Id` (from framework) for entity identifiers, not raw `String`.

```java
public record CreateXxxCommand(
    String field1, Id field2Id, Id field3Id,
    int quantity, Integer threshold, ...
) implements Command<XxxResult> {}
```

### 7.2 Query Record
- Located at: `store/.../query/`
- Java `record` implementing `Query<R>` where `R` is the result type.
- For paginated queries: `R = Page<XxxResult>`, and the query record also implements `PageableQueryRequest`.

```java
public record ListXxxQuery(
    Id parentId, Boolean active, XxxType type, Pageable pageable
) implements Query<Page<ListXxxResult>>, PageableQueryRequest {}
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
- Uses `@{Module}Transactional` (e.g., `@InventoryTransactional`, `@CatalogTransactional`) for write operations.
- Interacts with **domain aggregates** and **domain repository interfaces**.

### 8.2 QueryHandler
- Located at: `store/.../query/handler/`
- Annotated with `@Component`, `@RequiredArgsConstructor`.
- Implements `QueryHandler<Q extends Query<R>, R>`.
- Must implement `handle(Q query)` and `getQueryType()`.
- Uses `@{Module}ReadTransactional` (e.g., `@InventoryReadTransactional`, `@CatalogReadTransactional`) for read-only operations.

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

This project uses a **hybrid HATEOAS model**: links are rendered in HAL, but HTTP method affordances are not included. Therefore, every non-`self` rel MUST state the client action explicitly.

| Link Type | Rel Pattern |
|---|---|
| **Self link** | `self` |
| **Retrieve one** | `get-{entity}` |
| **List collection** | `list-{entities}` |
| **Search/filter collection** | `search-{entities}` |
| **Create** | `create-{entity}` |
| **Update** | `update-{entity}` |
| **Delete** | `delete-{entity}` |
| **State transition** | `{action}-{entity}` |
| **Qualified action** | `{action}-{entity}-{qualifier}` |
| **Subresource read** | `get-{entity}-{subresource}` |
| **Subresource collection** | `list-{entity}-{subresources}` |

### 9.3 Rel Naming Convention

The `withRel("...")` name is the **API contract** for link discovery. Rel names use **inline string literals** — there is no shared `LinkRelations` constants class. Follow these rules strictly:

| Rule | Convention |
|---|---|
| **Grammar** | `{action}-{resource}[-{qualifier}]` |
| **Self link** | `self` via `.withSelfRel()`; only for the canonical current representation |
| **Action first** | Start with the client intent: `get`, `list`, `search`, `create`, `update`, `delete`, or a domain action |
| **Single resource** | Use a singular resource noun: `get-{entity}`, `update-{entity}` |
| **Collection** | Use a plural resource noun: `list-{entities}`, `search-{entities}` |
| **Pagination** | Never encode pagination in the rel; use `list-{entities}`, not `paged-{entity}` |
| **Filtering** | Use `search-{entities}` for a distinct search operation; use `list-{entities}` when filters are optional on the canonical collection |
| **Subresource** | Put the parent resource before the subresource: `get-{entity}-{subresource}`, `list-{entity}-{subresources}` |
| **Qualified action** | Put the qualifier last: `{action}-{entity}-{qualifier}` |
| **Multi-word rels** | Lowercase with hyphens (kebab-case) |
| **Never empty** | Every link MUST have a meaningful rel |

Rel names describe API intent, not Java method names or raw HTTP verbs. A method that searches by name therefore uses `search-{entities}`, not a rel copied from the method name.

#### Standard Action Vocabulary

| Client intent | Action |
|---|---|
| Retrieve a single representation | `get` |
| Browse a canonical collection | `list` |
| Find/filter through a distinct search operation | `search` |
| Create a resource | `create` |
| Replace or modify a resource | `update` |
| Remove a resource | `delete` |
| Domain transition | Domain verb such as `approve`, `reject`, `suspend`, `restore`, `activate`, `deactivate`, or `sync` |
| Calculation or generation | Domain verb such as `generate` or `calculate` |

Do not use synonyms for the same intent. In particular, use `update`, not `edit`; `list`, not `paged`; and `get`, not `view` or `fetch`.

If listing and searching share one endpoint with optional filters, advertise only `list-{entities}`. If search is a distinct operation or endpoint, advertise both `list-{entities}` and `search-{entities}`.

### 9.4 Link Building Rules

1. **Use `methodOn(...)` for type-safe links** — always reference the controller method directly; never build URLs manually.
2. **Pass `null` for parameters you don't have** — request bodies (`@RequestBody`), headers (`@RequestHeader`), `Pageable`, and `PagedResourcesAssembler` should be passed as `null` in `methodOn(...)` calls.
3. **Cross-controller links for related entities** — use the same action-explicit grammar: `get-{entity}`, `list-{entities}`, and `create-{entity}`.
4. **Conditional links based on entity state** — use `if/else` on response DTO fields to add state-dependent action links.
5. **Use `self` only for the current representation** — it MUST point to the canonical GET endpoint that returns the represented resource. A command-result DTO, deleted resource, or mutation endpoint MUST NOT be labeled `self`; link to the affected resource with `get-{entity}` instead.
6. **Use inline string literals for rel names** — do NOT create a shared `LinkRelations` constants class. Rel names are written directly as string literals in `withRel("...")` calls.
7. **Do not encode transport details** — pagination, HTTP methods, controller names, and Java method names do not belong in rel names.
8. **Avoid unavailable actions** — only include command links the client is currently allowed to follow.

### 9.5 Per-Item Assembler Example (CRUD + Conditional + Cross-Controller)
```java
@Component
public class XxxModelAssembler
        implements RepresentationModelAssembler<XxxResponse, EntityModel<XxxResponse>> {

    @Override
    public EntityModel<XxxResponse> toModel(XxxResponse response) {
        EntityModel<XxxResponse> entity = EntityModel.of(response);

        // Self link
        entity.add(linkTo(methodOn(XxxController.class)
                .getXxx(response.id())).withSelfRel());

        // CRUD action links: the rel states the client action
        entity.add(linkTo(methodOn(XxxController.class)
                .updateXxx(response.id(), null, null)).withRel("update-xxx"));

        entity.add(linkTo(methodOn(XxxController.class)
                .deleteXxx(response.id(), null)).withRel("delete-xxx"));

        // Conditional links based on entity state
        if (response.active()) {
            entity.add(linkTo(methodOn(XxxController.class)
                    .deactivateXxx(response.id(), null)).withRel("deactivate-xxx"));
        } else {
            entity.add(linkTo(methodOn(XxxController.class)
                    .activateXxx(response.id(), null)).withRel("activate-xxx"));
        }

        // Cross-controller links: related entity "Yyy"
        entity.add(linkTo(methodOn(YyyController.class)
                .listYyy(response.id(), null, null)).withRel("list-yyys"));

        entity.add(linkTo(methodOn(YyyController.class)
                .getYyyById(null)).withRel("get-yyy"));

        entity.add(linkTo(methodOn(YyyController.class)
                .createYyy(response.id(), null, null)).withRel("create-yyy"));

        return entity;
    }
}
```

### 9.6 Minimal Canonical-Resource Assembler

When an entity has a canonical GET endpoint but no available actions or related resources, the assembler adds only `self`:

```java
@Component
public class XxxModelAssembler
        implements RepresentationModelAssembler<XxxResponse, EntityModel<XxxResponse>> {
    @Override
    public EntityModel<XxxResponse> toModel(XxxResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(XxxController.class).getXxx(response.id())).withSelfRel()
        );
    }
}
```

### 9.7 Collection Links on `PagedModel`

Every paginated endpoint that returns a `PagedModel` MUST expose the canonical collection action as `list-{entities}`. When creation is available, it MUST also add `create-{entity}` directly on the `PagedModel`.

#### Rules
1. After calling `pagedAssembler.toModel(page, xxxModelAssembler)`, ensure the result has a `list-{entities}` link that represents the collection request.
2. Add `create-{entity}` only when the caller is allowed to create a resource in that collection.
3. Use `linkTo(methodOn(...))` pointing to the endpoint of the **same controller** or a **related controller** for subresource collections.
4. Pass `null` for `@RequestBody` and `@RequestHeader` parameters in `methodOn(...)`.
5. Never use `paged-{entity}`; pagination is represented by the URI parameters and `PagedModel` metadata.

#### Example
```java
@GetMapping
public ResponseEntity<PagedModel<EntityModel<XxxResponse>>> listXxx(
        @RequestHeader(value = "X-Actor-Id") String actorId,
        @RequestParam(value = "active", required = false) Boolean active,
        @PageableDefault(size = 20) Pageable pageable,
        PagedResourcesAssembler<XxxResponse> pagedResourcesAssembler
) {
    Page<XxxResponse> response = xxxQueryService.listXxx(actorId, active, pageable);
    PagedModel<EntityModel<XxxResponse>> pageModel = pagedResourcesAssembler.toModel(response, xxxModelAssembler);

    pageModel.add(linkTo(methodOn(XxxController.class)
            .listXxx(null, null, null, null))
            .withRel("list-xxxs"));

    pageModel.add(linkTo(methodOn(XxxController.class)
            .createXxx(null, null))
            .withRel("create-xxx"));

    return ResponseEntity.ok(pageModel);
}
```

#### Sub-Resource Collection Example
When the paginated list is scoped under a parent resource (e.g., listing child entities under a parent ID), the `create-{entity}` link includes the parent ID:

```java
@GetMapping("/{parentId}/yyy")
public ResponseEntity<PagedModel<EntityModel<YyyResponse>>> listYyy(
        @PathVariable String parentId,
        @PageableDefault(size = 20) Pageable pageable,
        PagedResourcesAssembler<YyyResponse> pagedResourcesAssembler
) {
    Page<YyyResponse> response = yyyQueryService.listYyy(parentId, pageable);
    PagedModel<EntityModel<YyyResponse>> pageModel = pagedResourcesAssembler.toModel(response, yyyModelAssembler);

    pageModel.add(linkTo(methodOn(YyyController.class)
            .listYyy(parentId, null, null))
            .withRel("list-parent-yyys"));

    pageModel.add(linkTo(methodOn(YyyController.class)
            .createYyy(parentId, null, null))
            .withRel("create-yyy"));

    return ResponseEntity.ok(pageModel);
}
```

### 9.8 Cross-Controller and Related-Resource Strategy

When an entity has relationships with other entities, the assembler MUST include the applicable cross-controller entry points so that HATEOAS clients can **discover and crawl** related entities.

| Link | Rel Pattern | Purpose |
|---|---|---|
| **Get by ID** | `get-{entity}` | Discover a specific related entity |
| **Collection** | `list-{entities}` or `list-{parent}-{entities}` | Browse related entities |
| **Search** | `search-{entities}` | Use a distinct filtered/search operation |
| **Create** | `create-{entity}` | Create a new related entity under the current resource |

#### Rules
1. Applicable related-resource links are placed on the **parent entity's assembler** so the related entity is discoverable from the parent resource.
2. The `get-{entity}` link passes the known related ID. Do not advertise a non-templated link with a `null` path variable.
3. The `list-{entities}` link passes the current resource's ID as the parent ID, along with `null` for pageable parameters.
4. The `create-{entity}` link passes the current resource's ID as the parent ID, along with `null` for request body / header parameters.
5. If get-by-ID is not possible because no related ID is known, expose the collection/search link instead of constructing an incomplete link.

### 9.9 API Root & Discovery Endpoints (3-Tier Hierarchy)

The API implements a **3-tier hypermedia discovery hierarchy** that allows clients to navigate the entire API from a single entry point. All root endpoints produce `application/hal+json`.

#### Tier 1: API Root (`/api/v1`)
- **Class**: `ApiRootController` at `store/.../ApiRootController.java`
- Returns links to each bounded context root.
- Uses `WebMvcLinkBuilder` for type-safe links.
- Rel names are **inline string literals**.

```java
@RestController
@RequestMapping("/api/v1")
public class ApiRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(ApiRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(ModuleARootApi.class).root()).withRel("get-module-a-root"));
        model.add(linkTo(methodOn(ModuleBRootController.class).root()).withRel("get-module-b-root"));
        return ResponseEntity.ok(model);
    }
}
```

#### Tier 2: Bounded Context Roots
Each bounded context has its own root endpoint listing all top-level resources within that context.

```java
@RestController
@RequestMapping("/api/v1/{context}")
public class ModuleRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(Link.of("/api/v1/{context}").withSelfRel());
        model.add(Link.of("/api/v1/{context}/{resources-a}").withRel("list-resources-a"));
        model.add(Link.of("/api/v1/{context}/{resources-b}").withRel("list-resources-b"));
        return ResponseEntity.ok(model);
    }
}
```

#### Rules for Root Endpoints
1. **Every new bounded context MUST have a Tier 2 root endpoint** listing its top-level resources.
2. **The API root (`ApiRootController`) MUST be updated** to link to any new bounded context root.
3. Root endpoints MUST produce `MediaTypes.HAL_JSON_VALUE`.
4. Root endpoints return `ResponseEntity<RepresentationModel<?>>` (not `EntityModel`).
5. Use `Link.of(path)` for simple static paths in Tier 2 roots; use `WebMvcLinkBuilder` in Tier 1 root for type-safe cross-controller linking.
6. Rel names are **inline string literals** — no `LinkRelations` constants class.
7. Root discovery links also use generalized action-explicit patterns: `get-{context}-root`, `list-{entities}`, `search-{entities}`, and `create-{entity}`.

#### Discovery Flow
```text
GET /api/v1                       → { self, get-module-a-root, get-module-b-root }
  GET /api/v1/module-a            → { self, list-resources-a, list-resources-b, ... }
  GET /api/v1/module-b            → { self, list-resources-c, list-resources-d, ... }
```

### 9.10 Controller Integration Pattern

Controllers inject assemblers directly and call `.toModel()` inline. This is the **preferred pattern** for all modules.

```java
@RestController
@RequestMapping("/api/v1/{resources}")
@RequiredArgsConstructor
public class XxxController {
    private final XxxCommandService xxxCommandService;
    private final XxxQueryService xxxQueryService;
    private final XxxModelAssembler xxxModelAssembler;

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<XxxResponse>> getXxx(@PathVariable String id) {
        XxxResponse response = xxxQueryService.getXxx(id);
        return ResponseEntity.ok(xxxModelAssembler.toModel(response));
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<XxxResponse>>> listXxx(
            @RequestHeader(value = "X-Actor-Id") String actorId,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<XxxResponse> pagedAssembler) {
        Page<XxxResponse> page = xxxQueryService.listXxx(actorId, pageable);
        PagedModel<EntityModel<XxxResponse>> pageModel = pagedAssembler.toModel(page, xxxModelAssembler);

        pageModel.add(linkTo(methodOn(XxxController.class)
                .listXxx(null, null, null))
                .withRel("list-xxxs"));

        pageModel.add(linkTo(methodOn(XxxController.class)
                .createXxx(null, null))
                .withRel("create-xxx"));

        return ResponseEntity.ok(pageModel);
    }
}
```

### 9.11 Bare `EntityModel.of()` — No-Link Responses

Some endpoints (typically write-only or utility endpoints) may return `EntityModel.of(dto)` **without any links**. This is acceptable for:

- Bulk operations
- Utility responses with no meaningful navigable context
- Audit/log responses

> **Guideline:** Prefer adding a meaningful navigation link. Add `self` only when a canonical GET endpoint returns that exact representation; otherwise use an action-explicit rel such as `get-{entity}`.

For a command-result DTO, prefer a link back to the affected canonical resource instead of a misleading `self` link:

```java
return EntityModel.of(response,
        linkTo(methodOn(XxxController.class)
                .getXxx(response.xxxId()))
                .withRel("get-xxx")
);
```

### 9.12 Creation Endpoints — `ResponseEntity<Void>` with `Location` Header

Creation endpoints MAY return `201 Created` with a `Location` header instead of an `EntityModel` body. This is a valid REST pattern when the response body is not needed.

```java
@PostMapping
public ResponseEntity<Void> createXxx(@Valid @RequestBody CreateXxxRequest request) {
    String id = xxxCommandService.createXxx(request);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri();
    return ResponseEntity.created(location).build();
}
```

### 9.13 HATEOAS Configuration

The project relies entirely on **Spring Boot auto-configuration** from `spring-boot-starter-hateoas`. There is:
- **No custom `HalConfiguration` bean**
- **No `CurieProvider`** (no CURIE support)
- **No Affordances API** usage
- **No HATEOAS-specific `application.yml` settings**

The only relevant server setting is `server.forward-headers-strategy: framework`, which ensures `WebMvcLinkBuilder` generates correct absolute URLs behind proxies/load balancers.

#### Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

### 9.14 Complete HATEOAS Checklist

Before generating any assembler, controller, or root endpoint, verify:

1. ✅ Non-`self` rel names use `{action}-{resource}[-{qualifier}]` in kebab-case — inline string literals, no `LinkRelations` constants class.
2. ✅ `self` is used only for the canonical GET representation; command results link back with `get-{entity}`.
3. ✅ Links are built using `linkTo(methodOn(...))` — never manual URL strings (except in Tier 2 root endpoints).
4. ✅ `null` is passed for `@RequestBody`, `@RequestHeader`, `Pageable`, and `PagedResourcesAssembler` params in `methodOn(...)`.
5. ✅ Conditional links expose only currently available actions, with rels such as `activate-{entity}` and `deactivate-{entity}`.
6. ✅ Cross-controller links use `get-{entity}`, `list-{entities}`, `search-{entities}`, and `create-{entity}` as applicable.
7. ✅ Every `PagedModel` exposes `list-{entities}`; `create-{entity}` is added only when creation is available.
8. ✅ New bounded contexts have a Tier 2 root endpoint and are linked from `ApiRootController`.
9. ✅ Root endpoints produce `MediaTypes.HAL_JSON_VALUE` and return `RepresentationModel<?>`.
10. ✅ Controllers inject assemblers directly and call `.toModel()` inline.
11. ✅ Rel names never use `paged-*`, `edit-*`, bare entity nouns, Java method names, or HTTP method names.

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
- **Strict DDD Enforcement:** Aggregates must not be anemic. All business rules and state validations must reside within the Aggregate or a Domain Service, NEVER in the Command/Query handlers (Application Service).
- **Domain Policies:** Complex rules that don't fit naturally into a single aggregate (like Role Delegation or Scope resolution) must be encapsulated in Domain Policy classes (`*Policy`) or Domain Services.

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
| API Root Controller | `ApiRootController` | `ApiRootController` |
| Bounded Context Root | `{Context}RootController` | `OrderRootController` |
| Link Rel Names | Action-explicit inline strings using `{action}-{resource}[-{qualifier}]` | `"update-{entity}"`, `"list-{entities}"` |
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

## 18. Logging Guidelines

- Logging MUST use the custom logger framework (`logger-slf4j` module).
- All classes requiring logging in the **infrastructure** and **application** layers MUST declare the logger using this exact pattern:
  ```java
  private static final Logger log = Loggers.getLogger(<ClassName>.class);
  ```
---

## 19. Code Generation Checklist

Before generating or outputting any code, verify:

1. ✅ The controller delegates to the appropriate `CommandService` or `QueryService` — no inline logic.
2. ✅ A MapStruct mapper abstract class exists for every Command/Query operation.
3. ✅ The mapper is annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
4. ✅ Command/Query records use `Id` for identifiers, not raw `String`.
5. ✅ Handlers are `@Component` classes implementing `CommandHandler<C, R>` or `QueryHandler<Q, R>`.
6. ✅ Write handlers use `@{Module}Transactional`; read handlers use `@{Module}ReadTransactional`.
7. ✅ No business logic leaks into the controller, service, or mapper layers.
8. ✅ Controller returns `ResponseEntity<EntityModel<T>>` (single) or `ResponseEntity<PagedModel<EntityModel<T>>>` (paginated).
9. ✅ Model assemblers implement `RepresentationModelAssembler` and add HATEOAS links with proper rel naming (§9.3).
10. ✅ Every `PagedModel` exposes `list-{entities}` and adds `create-{entity}` when creation is available (§9.7).
11. ✅ DTOs are Java records in the appropriate `dto/request/` or `dto/response/` package.
12. ✅ Errors follow the sealed interface pattern with `ErrorCategory` and i18n error codes.
13. ✅ All files are placed in the correct package according to the module structure.
14. ✅ All non-`self` link rel names use `{action}-{resource}[-{qualifier}]` as inline string literals (§9.3) — no `LinkRelations` constants class.
15. ✅ New bounded contexts include a Tier 2 root endpoint and are linked from `ApiRootController` (§9.9).
16. ✅ Controllers inject assemblers directly and call `.toModel()` inline (§9.10).
17. ✅ Nested function invocations are avoided; intermediate variables are extracted for readability.
18. ✅ Logging in infra and application layers uses the custom logger SPI (`Loggers.getLogger()`).

---

## 20. Coding Style

### 20.1 Function Invocations
- **Extract intermediate variables**: Do not use nested function invocations (e.g., `doSomething(doA(doB()))`). Instead, extract the intermediate results into variables with descriptive names to improve readability and debuggability.

