# R1. Module Structure and Dependencies

Load when adding a module, changing package layout, or wiring Modulith dependencies.

## Layout

- **Multi-Module Topology**:
  - Pure Domain: `{name}-domain/` (e.g. `catalog-domain`, `cart-domain`, `inventory-domain`)
  - Application: `{name}-application/` (e.g. `catalog-application`, `pricing-application`)
  - Persistence Adapter: `{name}-adapter-persistence/` (e.g. `catalog-adapter-persistence`)
  - Infrastructure & Platform: `storage-adapter-s3/`, `outbox-infrastructure/`, `manifest-adapter-persistence/`, `workflow-infrastructure/`, `logger-slf4j/`
  - Application Assembly & Runtime (`app/`):
    - Platform BFFs:
      - `sellercentral/`: Seller Platform BFF (REST, JWT authentication, OpenAPI, seller persona DTOs & HATEOAS).
      - `marketplace/`: Marketplace Platform BFF (REST, edge security, buyer/guest persona DTOs & HATEOAS).
    - Headless Domain Service Groups (Zero REST Controllers):
      - `account/`: `identity`, `merchant`, `customer` domains.
      - `merchandising/`: `catalog`/product, `inventory`, `pricing` domains.
      - `channel/`: `saleschannel`, `region`, `discovery` domains.
      - `checkout/`: `cart`, `order`, `payment` domains.
      - `workflow/`: Core saga workflow orchestration engine.

- **Dependency Direction (Hexagonal / Ports & Adapters)**:
  - `framework` <- `{name}-domain`
  - `{name}-domain` <- `{name}-application`
  - `{name}-domain` + `{name}-application` <- `{name}-adapter-persistence`
  - Headless Domain Service Groups in `app/` wire application use cases, group-scoped CQRS buses, and universal Java port adapters (`port.message.*Port` + `internal.adapter.inbound.message.*PortAdapter`).
  - Platform BFFs in `app/` depend on universal Java port interfaces (`port.message.*Port`).
  - **BFFs NEVER import domain `internal/` packages or domain JPA persistence adapters.**
  - **Domain modules are strictly headless and NEVER depend on Platform BFFs or Spring Web MVC (`@RestController`, `@RequestMapping`).**

## Spring Modulith

- Each bounded context has an `@ApplicationModule(allowedDependencies = "shared")` marker class or package-info.
- `shared` is OPEN: `@ApplicationModule(type = ApplicationModule.Type.OPEN)` on `com.grab.app.shared` (and `com.grab.store.shared`).
- Internal implementation details reside strictly under `internal/`.
- Cross-module communication is permitted via:
  1. Universal Inbound Java Ports (`com.grab.app.{group}.{domain}.port.message.*Port`).
  2. Integration Events published via schema-isolated transactional outbox and consumed via `@EventListener`.
  3. Published named interfaces (`@NamedInterface`). No direct method calls into another module's `internal/` packages.

## Named interfaces

- Events: cross-module integration events MUST be written under `store/src/main/java/com/grab/store/shared/events/{module}/` (or `app/src/main/java/com/grab/app/shared/events/{module}/`) organized by module folder name. Since `shared` is OPEN, consuming modules access integration events via `shared`. Module-internal domain events stay inside their respective domain.
- HATEOAS links: publish link facades from a public named-interface package (e.g. `com.grab.app.merchandising.product.api` with `@NamedInterface("api")`). Consuming modules declare `{module}::api` in `allowedDependencies`. See `inbound/hateoas.md`.

## Layer contents

- **Domain (`{name}-domain/`)**: Pure domain logic. No Spring, JPA, or MapStruct annotations. Contains aggregates, entities, value objects, domain events, domain policies, and outbound write repository ports (`port/outbound/{Domain}Repository`).
- **Application (`{name}-application/`)**: Inbound use case ports (`port/inbound/*UseCase`), use case services (`service/*Service`), outbound intra query ports (`port/outbound/*QueryPort`), outbound inter-module ports (`port/outbound/{TargetDomain}*Port`), application commands/queries, and read views (`model/read/*View`).
- **Persistence Adapter (`{name}-adapter-persistence/`)**: Strictly INTRA-module persistence. Outbound persistence adapters (`adapter/*RepositoryAdapter`, `adapter/*QueryAdapter`, `adapter/*PersistenceExecutor`), JPA entities, Spring Data JPA repositories (`repository/jpa/*JpaRepository`), specifications (`specification/jpa/`), mappers/assemblers (`mapper/`), outbox producers/processors (`outbox/`), and Spring bean configuration (`config/*PersistenceConfig`). Never contains cross-module adapters.
- **Storage Adapter (`storage-adapter-s3/`)**: Outbound file storage adapter implementing `FileStoragePort` using AWS S3 / MinIO.
- **Platform BFFs (`app/src/main/java/com/grab/app/{bff}/`)**: REST controllers, request/response DTOs, mappers, HATEOAS model assemblers, security perimeter configuration, and OpenAPI specifications. Delegates to universal Java ports.
- **Headless Domain Groups (`app/src/main/java/com/grab/app/{group}/{domain}/`)**: Universal Java port interfaces (`port/message/*Port`), single-operation MapStruct mappers (`internal/adapter/inbound/message/mapper/*PortMapper`), inbound port adapters (`internal/adapter/inbound/message/*PortAdapter`), CQRS command/query handlers (`internal/command/handler/`, `internal/query/handler/`), group CQRS bus configurations (`internal/config/*BusConfig`), and multi-schema datasource configurations (`config/*DataSourceConfig`).
