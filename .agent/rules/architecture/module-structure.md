# R1. Module Structure and Dependencies

Load when adding a module, changing package layout, or wiring Modulith dependencies.

## Layout

- Module layout: `{name}-domain/` then `{name}-infrastructure/` then `store/`
- Dependency direction:
  - `framework` <- `domain` <- `infrastructure` <- `store`
  - `framework` <- `outbox-infrastructure` <- `store`
  - `framework` <- `workflow-infrastructure` <- `store`
  - `framework` <- `logger-slf4j` <- `store`

## Spring Modulith

- Each bounded context has an `@ApplicationModule(allowedDependencies = "shared")` marker class, for example `CatalogModule`, `InventoryModule`.
- `shared` is OPEN: `@ApplicationModule(type = ApplicationModule.Type.OPEN)` on `com.grab.store.shared` (`package-info.java`).
- Internal package goes under `internal/`.
- Cross-module communication is via domain/integration events and published named interfaces only. No direct method calls into another module's `internal/` packages.

## Named interfaces

- Events: publish from a public named-interface package, for example `com.grab.store.merchant.events` with `@NamedInterface("events")`. Consuming modules declare the dependency explicitly, for example `@ApplicationModule(allowedDependencies = {"shared", "merchant::events"})`.
- HATEOAS links: publish link facades from a public named-interface package, for example `com.grab.store.catalog.api` with `@NamedInterface("api")`. Consuming modules declare `{module}::api` in `allowedDependencies`. See `api/hateoas.md`.

## Layer contents

- Domain (`{name}-domain/`): no Spring, JPA, or MapStruct annotations. Contains aggregates, entities, value objects, events, repository interfaces, domain services, domain policies.
- Infrastructure (`{name}-infrastructure/`): JPA entities, JPA-to-domain mappers (MapStruct), repository implementations, outbox event producers.
- Application (`store/`): controllers, services, command/query handlers, mappers, assemblers, event listeners, application policies.
