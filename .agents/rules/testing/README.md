# Testing Rules & Guidelines

Load this directory when writing, refactoring, or reviewing unit, integration, and architecture tests across any module.

## Category Overview

Testing in this repository mirrors the **Hexagonal / Clean Architecture + CQRS + Spring Modulith** design. Each layer has strict test boundaries, frameworks, and isolation rules:

| Rule File | Topic | Scope | Primary Framework |
|:---|:---|:---|:---|
| [jpa-testcontainers.md](jpa-testcontainers.md) | JPA Repository & Adapter Integration | `{bc}-adapter-persistence` | Testcontainers (`JdbcDatabaseContainer`, e.g. PostgreSQL, MySQL), `@DataJpaTest`, AssertJ |
| [archunit.md](archunit.md) | Architecture & Invariant Conformance | All modules / `store` | ArchUnit (`archunit-junit5`), Spring Modulith |

---

## Testing Strategy by Layer

### 1. Pure Domain (`{bc}-domain`)
- **Type**: Pure Unit Tests.
- **Framework**: JUnit 5 + AssertJ.
- **Rules**:
  - **ZERO Spring Context**: Never annotate domain tests with `@SpringBootTest`, `@ExtendWith(SpringExtension.class)`, or any Spring framework class.
  - Test aggregate invariants, business policies, value object immutability, validation, and domain event publication (`getDomainEvents()`).
  - Execution speed MUST be sub-millisecond per test.

### 2. Application Layer (`{bc}-application`)
- **Type**: Use Case & Handler Unit Tests.
- **Framework**: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) or lightweight domain stubs.
- **Rules**:
  - Mock outbound port interfaces (`{Domain}Repository`, `{Domain}QueryPort`).
  - Verify use case orchestration, domain event publishing, and sealed error propagation.
  - Do NOT load Spring context for simple handler logic.

### 3. Persistence Adapters (`{bc}-adapter-persistence`)
- **Type**: Integration Tests with Real Containerized Database.
- **Framework**: **Testcontainers** (`JdbcDatabaseContainer` matching module database: PostgreSQL, MySQL, etc.) + Spring Boot `@DataJpaTest`.
- **Rules**:
  - Detailed in [jpa-testcontainers.md](jpa-testcontainers.md).
  - All repository tests MUST run against the module's target database engine via Testcontainers. Do not use mismatched in-memory databases (e.g. H2) that hide dialect differences.
  - Test Spring Data derived queries, JPQL `@Query`, Criteria Specifications, and entity-to-view mapping.

### 4. Web & Assembly (`store`)
- **Type**: Slice Tests (`@WebMvcTest`) & Modulith Verification.
- **Framework**: MockMvc + `@MockBean` + Spring Modulith `ApplicationModules`.
- **Rules**:
  - Controllers tested via `@WebMvcTest`: verify HTTP status codes, Bean Validation, and HATEOAS link creation.
  - Modularity verification: `ApplicationModules.of(StoreApplication.class).verify()`.

### 5. Architectural Invariants
- **Type**: Automated Static Bytecode Architecture Tests.
- **Framework**: **ArchUnit**.
- **Rules**:
  - Detailed in [archunit.md](archunit.md).
  - Automatically enforce all non-negotiable repository invariants (CQRS isolation, dependency direction, no native queries, no `@Transactional` on controllers/services).
