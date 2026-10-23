# R22a. JPA Repository & Persistence Adapter Testing with Testcontainers

Load when creating, updating, or reviewing tests in `{bc}-adapter-persistence` modules, including JPA repositories, Criteria API specifications, and persistence adapters.

## Core Principle

All JPA repository and persistence adapter tests MUST run against a containerized instance of the module's **target database engine** (e.g., PostgreSQL, MySQL, MariaDB) using **Testcontainers**. 

Using a mismatched in-memory database (such as H2 when targeting PostgreSQL or MySQL) is prohibited because database dialect discrepancies (JSON/JSONB support, sequence generation vs auto-increment, locking semantics, nested-set operations, UUID types, case-sensitivity) hide syntax bugs and produce false test results.

---

## Flexible Container Patterns

You can choose between two flexible Testcontainers approaches depending on your module's needs:

### Approach 1: Generic or Specialized `JdbcDatabaseContainer<?>` (Recommended for Custom Config)

Extend an abstract base configuration class that manages a shared `JdbcDatabaseContainer<?>` instance (such as `PostgreSQLContainer<?>`, `MySQLContainer<?>`, or `MariaDBContainer<?>`) across tests in the module.

```java
package com.grab.adapter.persistence.test.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer; // or MySQLContainer, MariaDBContainer, etc.

public abstract class AbstractDatabaseTestContainer {

    // Instantiate the appropriate container for the module's target database:
    protected static final JdbcDatabaseContainer<?> DATABASE_CONTAINER;

    static {
        DATABASE_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        // Or for MySQL:
        // DATABASE_CONTAINER = new MySQLContainer<>("mysql:8.0.36")
        //         .withDatabaseName("testdb")
        //         .withUsername("test")
        //         .withPassword("test");

        DATABASE_CONTAINER.start();
    }

    @DynamicPropertySource
    protected static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE_CONTAINER::getUsername);
        registry.add("spring.datasource.password", DATABASE_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", DATABASE_CONTAINER::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }
}
```

### Approach 2: Zero-Boilerplate Testcontainers JDBC URL

Testcontainers supports special JDBC URLs that automatically spin up and manage database containers without writing container Java code:

- **PostgreSQL**: `spring.datasource.url=jdbc:tc:postgresql:16-alpine:///testdb`
- **MySQL**: `spring.datasource.url=jdbc:tc:mysql:8.0:///testdb`
- **MariaDB**: `spring.datasource.url=jdbc:tc:mariadb:10.11:///testdb`

In `src/test/resources/application-test.yml`:
```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:16-alpine:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

---

## Test Configuration Class

Regardless of the container approach chosen, test configurations must use `@DataJpaTest` with embedded database replacement disabled:

```java
package com.catalog.adapter.persistence.repository.config;

import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = TestComponentConfig.class)
@Transactional
public abstract class RepositoryTestConfig extends AbstractDatabaseTestContainer {
}
```

---

## Test Implementation Guidelines

### 1. Spring Data JPA Repositories
Test derived query methods, JPQL `@Query`, and interface/record projections against the target database dialect. Always flush and clear the `EntityManager` after populating test data:

```java
@Autowired
private EntityManager entityManager;

@Autowired
private ProductJpaRepository repository;

@BeforeEach
void setUp() {
    entityManager.persist(createProduct("p-1", "Laptop"));
    entityManager.flush();
    entityManager.clear();
}

@Test
void findByUuid_shouldReturnPersistedProduct() {
    Optional<ProductEntity> result = repository.findByUuid("p-1");
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Laptop");
}
```

### 2. Criteria API Specifications & Paged Search
Verify dynamic predicates, joins, and `Pageable` sorting against the real database engine:

```java
@Test
void findAll_withSpecification_shouldFilterCorrectly() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
    Specification<ProductEntity> spec = ProductSearchSpecification.withFilter(criteria);

    Page<ProductEntity> page = repository.findAll(spec, pageable);
    assertThat(page.getContent()).isNotEmpty();
}
```

---

## MUST

- **MUST** run all repository and adapter integration tests against a containerized database matching the module's target production database engine.
- **MUST** disable Spring Boot embedded test database replacement using `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`.
- **MUST** reuse container instances across tests in a module (via shared static container or Testcontainers reusable mode) to avoid container startup overhead per test.
- **MUST** isolate test data using `@Transactional` rollback or explicit teardown.
- **MUST** flush and clear the `EntityManager` before querying in tests to avoid hitting Hibernate's 1st-level cache.
- **MUST** assert against domain identifiers (`uuid` or `Id`), never surrogate database IDs.

---

## MUST NOT

- **MUST NOT** use an in-memory database (e.g. H2) when the production target is a different database (e.g. PostgreSQL or MySQL).
- **MUST NOT** use `@Query(nativeQuery = true)`. Spring Data queries must use derived method names, JPQL, or Criteria Specifications.
- **MUST NOT** hardcode database ports or hostnames in test configurations.
- **MUST NOT** start a fresh Docker container per test method.
