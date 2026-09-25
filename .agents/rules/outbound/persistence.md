# R11. Persistence Adapters

Load when adding or changing JPA entities, mappers, persistence adapters, specifications, or persistence configuration.

## Mapping

- EntityMapper (MapStruct): JPA entity <-> domain aggregate fields.
- JpaAssembler (manual): complex assembly from multiple JPA entities into a domain aggregate.
- Overall Mapper coordinates EntityMapper + JpaAssembler.

## Adapter and Port Layout

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

## Write repository adapter (`{Domain}RepositoryAdapter`)

- Implements domain outbound port `{Domain}Repository` (aggregates in / aggregates out).
- Resides in `com.{name}.adapter.persistence.adapter`.
- Injects `{Domain}JpaRepository` + mapper/assembler, `DomainEventProducer`, and `PersistenceExecutor`.
- Used by command use cases and command handlers. Query handlers use it only when loading a full aggregate for a single-get that maps from domain.

## Query adapter (`{Domain}QueryAdapter`)

- Implements application outbound port `{Domain}QueryPort`.
- Resides in `com.{name}.adapter.persistence.adapter`.
- Has its own class. Do not fold query methods into the write repository adapter.
- Injects `{Domain}JpaRepository` and uses it for fixed JPQL / derived queries defined on the JPA interface.
- For paged search / filtered list: injects a specification class and MUST build the query via Criteria API through that specification. Never ad-hoc criteria inside the handler.
- Returns view record types (`{Domain}View` / summary records). Never domain aggregates or JPA entities to the application layer.

## Specification (paged search)

- Every paged search/list query MUST go through a specification class injected into the query adapter.
- Specification encapsulates Criteria API (`CriteriaBuilder` / predicates / joins), or an equivalent dedicated search-query helper under `specification/jpa/`, given filter criteria + `Pageable`.
- Result shape is a view/summary record suitable for mapping to Query Result then Response DTO.

## Configuration & Bean Exposure

- `{Domain}PersistenceConfig` exposes beans returning **port interfaces**, not concrete adapter types:
  - `public {Domain}Repository {domain}Repository(...) { return new {Domain}RepositoryAdapter(...); }`
  - `public {Domain}QueryPort {domain}QueryPort(...) { return new {Domain}QueryAdapter(...); }`

## Handler and Use Case Usage

- Command handlers / use cases -> domain write port `{Domain}Repository` only.
- Query handlers / use cases (list/search/paged) -> application query port `{Domain}QueryPort` only.
- Modulith `{module}::query` adapters in `store` -> inbound `*UseCase` only, never outbound `{Domain}QueryPort` directly.
- Handlers and use cases MUST NOT inject `{Domain}JpaRepository`, `EntityManager`, or concrete `*Adapter` classes directly.
- Controllers, services, mappers, assemblers, and policies MUST NOT use any repository, port, or adapter. See `application/handlers.md`.

