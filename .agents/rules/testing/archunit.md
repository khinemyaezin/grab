# R22b. Architecture Conformance Testing with ArchUnit

Load when setting up, extending, or maintaining architectural conformance tests across any module.

## Core Principle

Architectural invariants defined in `AGENTS.md` and `.agents/rules/` MUST NOT rely solely on manual code reviews. They MUST be codified and enforced as fast, automated static tests using **ArchUnit** (`archunit-junit5`) during every `mvn test` execution.

---

## Required ArchUnit Invariant Rules

### 1. Pure Domain Isolation (R1, R13)
The domain layer (`com..domain..`) MUST be framework-agnostic. It must have zero dependencies on Spring, JPA/Hibernate, Jackson, or outer layers (application, persistence, store).

```java
@ArchTest
static final ArchRule domain_layer_should_not_depend_on_frameworks =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework..",
            "jakarta.persistence..",
            "com.fasterxml.jackson..",
            "..application..",
            "..adapter..",
            "..store..", "..app.."
        );
```

### 2. Hexagonal Dependency Direction (R1, R2)
Inner layers must never depend on outer layers:
- `domain` is depended on by `application`, `adapter-persistence`, and `app`.
- `application` depends only on `domain` (and framework commons), never on `adapter-persistence` or `app`.

```java
@ArchTest
static final ArchRule application_layer_should_not_depend_on_outer_layers =
    noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..adapter.persistence..", "..store..", "..app..");
```

### 3. Strict CQRS Separation & Isolation (R2, R4, R25)
- `CommandService` classes MUST NOT depend on `QueryBus`, `QueryService`, or `*QueryPort`.
- `QueryService` classes MUST NOT depend on `CommandBus`, `CommandService`, or domain write `*Repository`.

```java
@ArchTest
static final ArchRule command_service_must_not_access_query_bus_or_query_service =
    noClasses()
        .that().haveSimpleNameEndingWith("CommandService")
        .should().dependOnClassesThat()
        .haveSimpleNameEndingWith("QueryService")
        .orShould().dependOnClassesThat()
        .haveSimpleNameEndingWith("QueryBus");

@ArchTest
static final ArchRule query_service_must_not_access_command_bus_or_command_service =
    noClasses()
        .that().haveSimpleNameEndingWith("QueryService")
        .should().dependOnClassesThat()
        .haveSimpleNameEndingWith("CommandService")
        .orShould().dependOnClassesThat()
        .haveSimpleNameEndingWith("CommandBus");
```

### 4. Controller Boundary Constraints (R3, Checklist #1, #14)
Controllers reside at the HTTP boundary. They:
- MUST only inject `CommandService`, `QueryService`, and `ModelAssembler`.
- MUST NOT inject `CommandBus`, `QueryBus`, CQRS Handlers, Use Cases, Ports, or Repositories.

```java
@ArchTest
static final ArchRule controllers_must_not_inject_repositories_or_handlers =
    noClasses()
        .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
        .should().dependOnClassesThat()
        .haveSimpleNameEndingWith("Repository")
        .orShould().dependOnClassesThat()
        .haveSimpleNameEndingWith("CommandHandler")
        .orShould().dependOnClassesThat()
        .haveSimpleNameEndingWith("QueryHandler")
        .orShould().dependOnClassesThat()
        .haveSimpleNameEndingWith("UseCase");
```

### 5. No Native Queries in JPA Repositories (R11, Checklist #20)
Spring Data JPA repository methods MUST NOT use `@Query(nativeQuery = true)`.

```java
@ArchTest
static final ArchRule no_native_queries_in_jpa_repositories =
    methods()
        .that().areAnnotatedWith(org.springframework.data.jpa.repository.Query.class)
        .should(new ArchCondition<JavaMethod>("not have nativeQuery set to true") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                Query queryAnnotation = method.getAnnotationOfType(Query.class);
                if (queryAnnotation != null && queryAnnotation.nativeQuery()) {
                    events.add(SimpleConditionEvent.violated(
                        method,
                        "Method " + method.getFullName() + " uses nativeQuery = true which violates R11"
                    ));
                }
            }
        });
```

### 6. Transaction Demarcation Rules (R16, Checklist #4)
`@Transactional` MUST NOT be placed on Controllers, Services, Mappers, Assemblers, or Policies. It belongs strictly on CQRS Handlers and provider shared-interface adapters.

```java
@ArchTest
static final ArchRule transactional_prohibited_on_controllers_and_services =
    classes()
        .that().haveSimpleNameEndingWith("Controller")
        .or().haveSimpleNameEndingWith("Service")
        .or().haveSimpleNameEndingWith("Mapper")
        .or().haveSimpleNameEndingWith("Assembler")
        .or().haveSimpleNameEndingWith("Policy")
        .should().notBeAnnotatedWith(org.springframework.transaction.annotation.Transactional.class);
```

### 7. Record Immutability for DTOs and CQRS Messages (R6, R8, Checklist #9)
Commands, Queries, Results, and DTOs MUST be Java `record` types.

```java
@ArchTest
static final ArchRule commands_and_queries_must_be_records =
    classes()
        .that().haveSimpleNameEndingWith("Command")
        .or().haveSimpleNameEndingWith("Query")
        .or().haveSimpleNameEndingWith("Result")
        .or().resideInAPackage("..dto..")
        .should().beRecords();
```

---

## Test Placement & Execution

- Place ArchUnit architecture test suites in the root test module (`app/src/test/java/com/grab/architecture/ArchitectureTest.java`).
- Group tests with descriptive names referencing the invariant or rule ID (e.g. `R1_DomainIsolationTest`, `R2_CqrsIsolationTest`, `R11_NoNativeQueriesTest`).
- Ensure ArchUnit runs as part of standard `mvn test` without requiring external services or Docker containers.
