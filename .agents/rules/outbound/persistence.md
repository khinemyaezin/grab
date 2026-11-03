# R11. Persistence Adapters & Multi-Schema JPA

Load when adding or changing JPA entities, mappers, persistence adapters, specifications, or persistence configuration.

## 1. Multi-Schema Database Isolation & Connection Budgeting

The persistence layer enforces strict physical and logical multi-schema isolation:

- **Dedicated Schemas per Service Group**:
  - `account`: Identity, Merchant, Customer persistence.
  - `catalog`: Merchandising (Products, Variants, Categories, Pricing, Inventory).
  - `channel`: Sales Channels, API Keys, Regions, Discovery projections.
  - `"order"`: Checkout (Cart, Order, Payment).
  - `discovery`: Dedicated high-throughput read-only connection pool.
- **Connection Budgeting**:
  - Each service group configures an isolated HikariCP connection pool with explicit sizing limits (`maximum-pool-size`, `minimum-idle`, `connection-timeout`, `leak-detection-threshold`).
  - Connection exhaustion in one service group (e.g. `catalog`) CANNOT starve or degrade another group (e.g. `order`).
- **Zero Cross-Schema Coupling**:
  - **Zero Cross-Schema Foreign Keys**: Foreign references must be stored as logical identifier columns (`String` or `Id`), never as database-level foreign key constraints across schemas.
  - **Zero Cross-Schema JPA Relationships**: A JPA entity in one module MUST NOT have `@OneToMany`, `@ManyToOne`, or `@ManyToMany` relationships pointing to entities in another module or schema.
  - **Zero Cross-Schema SQL Joins**: SQL queries cannot join tables across schema boundaries.

---

## 2. Adapter and Port Layout

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

---

## 3. Write Repository Adapter (`{Domain}RepositoryAdapter`)

- Implements domain outbound port `{Domain}Repository` (aggregates in / aggregates out).
- Resides in `com.{name}.adapter.persistence.adapter`.
- Injects `{Domain}JpaRepository` + mapper/assembler, `DomainEventProducer`, and `PersistenceExecutor`.
- Used by command use cases and command handlers. Query handlers use it only when loading a full aggregate for a single-get that maps from domain.

---

## 4. Query Adapter (`{Domain}QueryAdapter`)

- Implements application outbound port `{Domain}QueryPort`.
- Resides in `com.{name}.adapter.persistence.adapter`.
- Has its own class. Do not fold query methods into the write repository adapter.
- Injects `{Domain}JpaRepository` and uses it for fixed JPQL / derived queries defined on the JPA interface.
- For paged search / filtered list: injects a specification class and MUST build the query via Criteria API through that specification. Never ad-hoc criteria inside the handler.
- Returns view record types (`{Domain}View` / summary records). Never domain aggregates or JPA entities to the application layer.

---

## 5. Specification (Paged Search)

- Every paged search/list query MUST go through a specification class injected into the query adapter.
- Specification encapsulates Criteria API (`CriteriaBuilder` / predicates / joins), or an equivalent dedicated search-query helper under `specification/jpa/`, given filter criteria + `Pageable`.
- Result shape is a view/summary record suitable for mapping to Query Result then Response DTO.

---

## 6. Spring Data JPA Repositories (`{Domain}JpaRepository`)

- Spring Data interfaces reside in `com.{name}.adapter.persistence.repository.jpa`.
- Extend `JpaRepository<{Domain}Entity, Long>` and optionally `JpaSpecificationExecutor<{Domain}Entity>`.
- Query methods query by `uuid` (`findByUuid(String uuid)`), never by DB numeric `id`.
- **MUST NOT use native queries (`nativeQuery = true`)**:
  - Always use Spring Data derived method names, JPQL (`@Query("SELECT ...")`), or Criteria API Specifications.
  - Never use `@Query(value = "...", nativeQuery = true)`. Database-specific SQL dialects compromise portability, bypass JPA entity lifecycle management and type checking, and couple persistence adapters to specific database implementations.

---

## 7. Configuration & Bean Exposure

- `{Domain}PersistenceConfig` exposes beans returning **port interfaces**, not concrete adapter types:
  - `public {Domain}Repository {domain}Repository(...) { return new {Domain}RepositoryAdapter(...); }`
  - `public {Domain}QueryPort {domain}QueryPort(...) { return new {Domain}QueryAdapter(...); }`

---

## 8. Handler and Use Case Usage

- Command handlers / use cases -> domain write port `{Domain}Repository` only.
- Query handlers / use cases (list/search/paged) -> application query port `{Domain}QueryPort` only.
- Handlers and use cases MUST NOT inject `{Domain}JpaRepository`, `EntityManager`, or concrete `*Adapter` classes directly.
- Controllers, services, mappers, assemblers, and policies MUST NOT use any repository, port, or adapter.
