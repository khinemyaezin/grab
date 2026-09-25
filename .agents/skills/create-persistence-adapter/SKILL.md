---
name: create-persistence-adapter
description: >-
  Step-by-step procedure for creating intra-module JPA persistence adapters, entity mappers,
  JPA assemblers, specifications, and repository adapters.
---

# Create Persistence Adapter Guide

Use this skill when implementing or modifying intra-module persistence in `{bc}-adapter-persistence/`.

---

## Directory Structure
```text
{bc}-adapter-persistence/src/main/java/com/{bc}/adapter/persistence/
├── adapter/
│   ├── {Domain}RepositoryAdapter.java    # Outbound adapter implementing domain {Domain}Repository
│   ├── {Domain}QueryAdapter.java         # Outbound adapter implementing application {Domain}QueryPort
│   └── {Domain}PersistenceExecutor.java   # Exception translation executor
├── config/
│   └── {Domain}PersistenceConfig.java    # Spring config exposing beans as PORT interfaces
├── entity/
│   └── {Domain}Entity.java               # JPA @Entity mapping table
├── mapper/
│   ├── CentralMapperConfig.java
│   ├── {Domain}EntityMapper.java         # MapStruct interface (entity <-> domain fields)
│   └── impl/
│       └── {Domain}JpaAssembler.java     # Coordinates entity mapper + complex aggregate graphs
├── repository/
│   └── jpa/
│       └── {Domain}JpaRepository.java    # Spring Data interface extending JpaRepository
└── specification/
    └── jpa/
        └── {Domain}QuerySpecification.java # Criteria API search & filter predicates
```

---

## Implementation Checklist

### 1. JPA Entity
- [ ] Annotate with `@Entity`, `@Table(name = "...")`.
- [ ] Must have `uuid` column (UUID string mapped to domain `Id`).
- [ ] Include `@Version private Long version;` for optimistic locking.
- [ ] Proper audit timestamps: `createdAt`, `updatedAt`.

### 2. Spring Data Repository
- [ ] In `repository/jpa/`, extend `JpaRepository<{Domain}Entity, Long>` and `JpaSpecificationExecutor`.
- [ ] Define query methods querying by `uuid` (`findByUuid(String uuid)`), never by DB numeric `id`.

### 3. Mappers & Assemblers
- [ ] **EntityMapper**: Abstract class annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
- [ ] **JpaAssembler**: Coordinates `EntityMapper` and builds complete aggregate domain instances (`toDomain(entity)`) and entity trees (`toEntity(aggregate, destination)`).

### 4. Write Repository Adapter (`{Domain}RepositoryAdapter`)
- [ ] Implements domain outbound port `{Domain}Repository`.
- [ ] Injects:
  1. `{Domain}JpaRepository`
  2. `{Domain}JpaAssembler` (or mapper)
  3. `DomainEventProducer`
  4. `PersistenceExecutor`
- [ ] Wrap queries: `executor.query("EntityName", () -> ...);`
- [ ] Wrap writes: `executor.command("EntityName", () -> ...);`
- [ ] In `save()`:
  - Assemble and save entity.
  - Pull domain events: `List<Event> events = aggregate.pullEvents();`
  - Produce events: `domainEventProducer.produce(aggregate.getClass().getSimpleName(), aggregate.getId().getValue(), events);`

### 5. Query Adapter (`{Domain}QueryAdapter`)
- [ ] Implements application outbound port `{Domain}QueryPort`.
- [ ] Separate class from the write repository adapter.
- [ ] Returns view records (`{Domain}View`), never aggregates or JPA entities.
- [ ] Paged queries use injected `Specification` with Criteria API.

### 6. Persistence Configuration (`{Domain}PersistenceConfig`)
- [ ] `@Configuration` class.
- [ ] Exposes beans as **port interfaces**, not concrete adapter classes:
  ```java
  @Bean
  public {Domain}Repository {domain}Repository(...) {
      return new {Domain}RepositoryAdapter(...);
  }

  @Bean
  public {Domain}QueryPort {domain}QueryPort(...) {
      return new {Domain}QueryAdapter(...);
  }
  ```
