# R11. Infrastructure Persistence

Load when adding or changing JPA entities, mappers, repositories, specifications, or read views.

## Mapping

- EntityMapper (MapStruct): JPA entity <-> domain aggregate fields.
- JpaAssembler (manual): complex assembly from multiple JPA entities into a domain aggregate.
- Overall Mapper coordinates EntityMapper + JpaAssembler.

## Repository layout

Path base: `{name}-infrastructure/.../repository/jpa/`

Separate write and query concerns for each aggregate root.

| Artifact | Location | Role |
|---|---|---|
| Domain write port | `{name}-domain/.../repository/{Domain}Repository` | Aggregate load/save/delete API. Framework-agnostic. |
| Write impl | `.../repository/jpa/impl/Default{Domain}Repository` (or `{Domain}RepositoryImpl`) | Implements the domain `{Domain}Repository` |
| Query port | `.../repository/jpa/{Domain}QueryRepository` | Read/search API returning view records, not aggregates |
| Query impl | `.../repository/jpa/impl/Default{Domain}QueryRepository` (or `{Domain}QueryRepositoryImpl`) | Implements `{Domain}QueryRepository` |
| Spring Data JPA | `.../repository/jpa/{Domain}JpaRepository` | `JpaRepository` plus optional `@Query` JPQL. Used inside write/query impls only |
| Specification | `.../specification/jpa/{Domain}*Specification` (or search criteria + criteria builder) | Criteria API predicates for paged search |
| View | `.../view/{Domain}View` (or `{Domain}Summary`) | Read-model record returned by query repos |

## Write repository (`{Domain}Repository`)

- Domain module declares `{Domain}Repository` (aggregates in / aggregates out).
- Infrastructure provides the only implementation of that domain interface.
- Impl injects `{Domain}JpaRepository` + mapper/assembler, plus `PersistenceExecutor` / event producer as needed.
- Used by command handlers. Query handlers use it only when loading a full aggregate for a single-get that maps from domain.

## Query repository (`{Domain}QueryRepository`)

- Declared and implemented entirely in infrastructure. Not a domain interface.
- Has its own implementation class. Do not fold query methods into the write repository impl.
- Impl injects `{Domain}JpaRepository` and uses it for fixed JPQL / derived queries defined on the JPA interface.
- For paged search / filtered list: inject a specification class and MUST build the query via Criteria API through that specification. Never ad-hoc criteria inside the handler.
- Returns view record types (`{Domain}View` / summary records). Never domain aggregates or JPA entities to the application layer.

## Specification (paged search)

- Every paged search/list query MUST go through a specification class injected into the query repository impl.
- Specification encapsulates Criteria API (`CriteriaBuilder` / predicates / joins), or an equivalent dedicated search-query helper under `specification/jpa/`, given filter criteria + `Pageable`.
- Result shape is a view/summary record suitable for mapping to Query Result then Response DTO.

## Handler usage

- Command handlers -> domain `{Domain}Repository` only.
- Query handlers (list/search/paged) -> `{Domain}QueryRepository` interface only.
- Handlers MUST NOT inject `{Domain}JpaRepository` or `EntityManager` directly.
- Controllers, services, mappers, assemblers, and policies MUST NOT use any repository. See `application/handlers.md`.
