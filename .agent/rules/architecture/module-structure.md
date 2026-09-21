# R1. Module Structure and Dependencies

Load when adding a module, changing package layout, or wiring Modulith dependencies.

## Layout

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

## Spring Modulith

- Each bounded context has an `@ApplicationModule(allowedDependencies = "shared")` marker class in `store`, for example `CatalogModule`, `InventoryModule`.
- `shared` is OPEN: `@ApplicationModule(type = ApplicationModule.Type.OPEN)` on `com.grab.store.shared` (`package-info.java`).
- Internal package goes under `internal/`.
- Cross-module communication is via domain/integration events and published named interfaces only. No direct method calls into another module's `internal/` packages.

## Named interfaces

- Events: publish from a public named-interface package, for example `com.grab.store.merchant.events` with `@NamedInterface("events")`. Consuming modules declare the dependency explicitly, for example `@ApplicationModule(allowedDependencies = {"shared", "merchant::events"})`.
- HATEOAS links: publish link facades from a public named-interface package, for example `com.grab.store.catalog.api` with `@NamedInterface("api")`. Consuming modules declare `{module}::api` in `allowedDependencies`. See `api/hateoas.md`.

## Layer contents

- Domain (`{name}-domain/`): Pure domain logic. No Spring, JPA, or MapStruct annotations. Contains aggregates, entities, value objects, domain events, domain policies, and outbound write repository ports (`port/outbound/{Domain}Repository`).
- Application (`{name}-application/`): Inbound use case ports (`port/inbound/*UseCase`), use case services (`service/*Service`), outbound query ports (`port/outbound/*QueryPort`), application commands/queries, and read views (`model/read/*View`).
- Persistence Adapter (`{name}-adapter-persistence/`): Outbound persistence adapters (`adapter/*RepositoryAdapter`, `adapter/*QueryAdapter`, `adapter/*PersistenceExecutor`), JPA entities, Spring Data JPA repositories (`repository/jpa/*JpaRepository`), specifications (`specification/jpa/`), mappers/assemblers (`mapper/`), outbox producers/processors (`outbox/`), and Spring bean configuration (`config/*PersistenceConfig`).
- Storage Adapter (`storage-adapter-s3/`): Outbound file storage adapter implementing `FileStoragePort` using AWS S3 / MinIO.
- Web & App Assembly (`store/`): REST controllers, request/response DTOs, mappers, HATEOAS model assemblers, CQRS command/query handlers delegating to use cases, event listeners, application policies, and application configuration.

