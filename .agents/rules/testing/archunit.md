# R22b. Architecture Conformance Testing with ArchUnit

Load when setting up, extending, or maintaining architectural conformance tests across any module.

## Core Principle

Architectural invariants defined in `AGENTS.md` and `.agents/rules/` MUST NOT rely solely on manual code reviews. They MUST be codified and enforced as fast, automated static tests using **ArchUnit** (`archunit-junit5`) during every `mvn test` execution.

---

## Required ArchUnit Invariant Rules

### 1. Pure Domain Isolation (R1, R13)
The domain layer (`com..domain..`) MUST be framework-agnostic. It must have zero dependencies on Spring, JPA/Hibernate, Jackson, or outer layers (application, persistence, store, app).

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
            "..store..",
            "..app.."
        );
```

### 2. Hexagonal Dependency Direction (R1, R2)
Inner layers must never depend on outer layers:
- `domain` is depended on by `application`, `adapter-persistence`, and `app`.
- `application` depends only on `domain` (and framework commons), never on `adapter-persistence`, `store`, or `app`.

```java
@ArchTest
static final ArchRule application_layer_should_not_depend_on_outer_layers =
    noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..adapter.persistence..", "..store..", "..app..");
```

### 3. Headless Domain Invariant (R1, R3)
Classes in headless domain service groups (`com.grab.app.account..`, `com.grab.app.merchandising..`, `com.grab.app.channel..`, `com.grab.app.checkout..`, `com.grab.app.workflow..`) MUST NOT depend on Spring Web MVC annotations. REST controllers reside strictly in Platform BFFs (`sellercentral`, `marketplace`).

```java
@ArchTest
static final ArchRule headless_domains_must_not_use_web_annotations =
    noClasses()
        .that().resideInAnyPackage(
            "com.grab.app.account..",
            "com.grab.app.merchandising..",
            "com.grab.app.channel..",
            "com.grab.app.checkout..",
            "com.grab.app.workflow.."
        )
        .should().dependOnClassesThat()
        .resideInAPackage("org.springframework.web.bind.annotation..");
```

### 4. Strict CQRS Separation & Group Bus Isolation (R2, R4, R25)
- `CommandService` classes and write adapters MUST NOT depend on `QueryBus`, `QueryService`, or `*QueryPort`.
- `QueryService` classes and read adapters MUST NOT depend on `CommandBus`, `CommandService`, or domain write `*Repository`.
- CQRS handlers must register strictly with their respective group-scoped CQRS buses.

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

### 5. Controller Boundary Constraints (R3, Checklist #1, #14)
Controllers reside strictly within Platform BFFs (`sellercentral`, `marketplace`). They:
- MUST only inject `*Port` interfaces, BFF orchestration services, and `ModelAssembler`.
- MUST NOT inject `CommandBus`, `QueryBus`, CQRS Handlers, Use Cases, Repositories, or Persistence Adapters.

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

### 6. No Native Queries in JPA Repositories (R11, Checklist #20)
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

### 7. Multi-Schema JPA Isolation (R11, Checklist #27)
JPA entities in one persistence package MUST NOT reference JPA entities in another module or schema via object references (`@OneToMany`, `@ManyToOne`, `@ManyToMany`). References across schemas must use logical IDs.

```java
@ArchTest
static final ArchRule entities_must_not_cross_schema_boundaries =
    classes()
        .that().areAnnotatedWith(jakarta.persistence.Entity.class)
        .should(new ArchCondition<JavaClass>("only reference entities within the same bounded context") {
            @Override
            public void check(JavaClass entityClass, ConditionEvents events) {
                // Verify no field or relationship references entities outside the module's persistence package
            }
        });
```

### 8. Transaction Demarcation Rules (R16, Checklist #4)
`@Transactional` MUST NOT be placed on Controllers, Services, Mappers, Assemblers, or Policies. It belongs strictly on CQRS Handlers and provider shared-interface / port adapters.

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

### 9. Record Immutability for DTOs and CQRS Messages (R6, R8, Checklist #9)
Commands, Queries, Results, Port Messages, and DTOs MUST be Java `record` types.

```java
@ArchTest
static final ArchRule commands_and_queries_must_be_records =
    classes()
        .that().haveSimpleNameEndingWith("Command")
        .or().haveSimpleNameEndingWith("Query")
        .or().haveSimpleNameEndingWith("Result")
        .or().haveSimpleNameEndingWith("Message")
        .or().resideInAPackage("..dto..")
        .should().beRecords();
```

---

## Test Placement & Execution

- Place ArchUnit architecture test suites in `app/src/test/java/com/grab/app/` and root test suites.
- Group tests with descriptive names referencing the invariant or rule ID (e.g. `R1_DomainIsolationTest`, `R2_CqrsIsolationTest`, `R3_HeadlessDomainTest`, `R11_NoNativeQueriesTest`).
- Ensure ArchUnit runs as part of standard `mvn test` without requiring external services or Docker containers.
