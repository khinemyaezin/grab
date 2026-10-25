# R19. Naming

Load when naming a new type, package, or test method.

| Artifact | Pattern | Location |
|---|---|---|
| Controller | `{Entity}Controller` | `app/.../{module}/internal/api/rest/controller/` |
| Command/Query Service | `{Entity}CommandService` / `{Entity}QueryService` | `app/.../{module}/internal/command/` or `internal/query/` |
| Request DTO | `{Action}{Entity}Request` | `app/.../{module}/internal/api/rest/dto/request/` |
| Response DTO | `{Entity}Response` | `app/.../{module}/internal/api/rest/dto/response/` |
| Command record | `{Action}{Entity}Command` | `{name}-application/.../model/write/` |
| Query record | `Get{Entity}Query` / `List{Entities}Query` | `{name}-application/.../model/read/` |
| Command/Query Handler | `{Action}{Entity}CommandHandler` / `{Action}{Entity}QueryHandler` | `app/.../{module}/internal/command/` or `internal/query/` |
| Mapper | `{Action}{Entity}RequestMapper` | `app/.../{module}/internal/api/rest/mapper/` |
| Model Assembler (entity) | `{Entity}ModelAssembler` | `app/.../{module}/internal/api/rest/assembler/` |
| Model Assembler (per-operation) | `{Action}{Entity}ModelAssembler` | `app/.../{module}/internal/api/rest/assembler/` |
| Domain Policy | `{Capability}Policy` | `{name}-domain/.../policy/` |
| Application Policy | `{Capability}Policy` | `app/.../{module}/internal/policy/` |
| **Inbound use case port** | `{Action}{Entity}UseCase` | `{name}-application/.../port/inbound/` |
| **Use case service** | `{Action}{Entity}Service` | `{name}-application/.../service/` |
| **Outbound Intra write port (Domain Repository)** | `{Domain}Repository` | `{name}-domain/.../port/outbound/` |
| **Outbound Intra read port (Application Query)** | `{Domain}QueryPort` | `{name}-application/.../port/outbound/` |
| **Outbound Inter port (Consumer ACL Port)** | `{TargetDomain}{Capability}Port` | `{name}-application/.../port/outbound/` |
| **Persistence write adapter (Intra only)** | `{Domain}RepositoryAdapter` | `{name}-adapter-persistence/.../adapter/` |
| **Persistence query adapter (Intra only)** | `{Domain}QueryAdapter` | `{name}-adapter-persistence/.../adapter/` |
| **Persistence executor** | `{Domain}PersistenceExecutor` | `{name}-adapter-persistence/.../adapter/` |
| Spring Data JPA | `{Domain}JpaRepository` | `{name}-adapter-persistence/.../repository/jpa/` |
| Query specification | `{Domain}*Specification` (or `{Domain}SearchCriteria`) | `{name}-adapter-persistence/.../specification/jpa/` |
| Read view | `{Domain}View` / `{Domain}Summary` | `{name}-application/.../model/read/` |
| API Root | `ApiRootController` | `app/.../` |
| Bounded Context Root | `{Context}RootController` | `app/.../{module}/` |
| Modulith named interface package (events) | `{module}.events` + `@NamedInterface("events")` | `app/.../{module}/events/` |
| Modulith named interface package (API links) | `{module}.api` + `@NamedInterface("api")` | `app/.../{module}/api/` |
| Cross-module HATEOAS link facade | `{Owner}ApiLinks` | `app/.../{owner}/api/` |
| **Provider Public Port (Inter-module contract)** | `{Capability}Port` or `{Capability}Query` | `app/.../{provider}/port/` |
| **Provider Public Port DTO** | Nested `record` inside the port interface | inside `app/.../{provider}/port/{Port}.java` |
| **Provider Port Adapter** | `{Capability}PortAdapter` or `{Capability}QueryAdapter` | `app/.../{provider}/internal/api/adapter/` |
| **Provider Port Mapper** | `{Capability}Mapper` | `app/.../{provider}/internal/api/adapter/mapper/` |
| **Consumer Outbound Adapter (Inter ACL)** | `{TargetDomain}{Capability}Adapter` | `app/.../{consumer}/internal/adapter/` |
| Test method | `{functionName}_{input}_{expectedBehavior}` | `src/test/java/...` |
