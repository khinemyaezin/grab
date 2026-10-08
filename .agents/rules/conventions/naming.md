# R19. Naming

Load when naming a new type, package, or test method.

| Artifact | Pattern | Location |
|---|---|---|
| Controller | `{Entity}Controller` | `store/.../{module}/internal/api/rest/controller/` |
| Command/Query Service | `{Entity}CommandService` / `{Entity}QueryService` | `store/.../{module}/internal/command/` or `internal/query/` |
| Request DTO | `{Action}{Entity}Request` | `store/.../{module}/internal/api/rest/dto/request/` |
| Response DTO | `{Entity}Response` | `store/.../{module}/internal/api/rest/dto/response/` |
| Command record | `{Action}{Entity}Command` | `{name}-application/.../model/write/` |
| Query record | `Get{Entity}Query` / `List{Entities}Query` | `{name}-application/.../model/read/` |
| Command/Query Handler | `{Action}{Entity}CommandHandler` / `{Action}{Entity}QueryHandler` | `store/.../{module}/internal/command/` or `internal/query/` |
| Mapper | `{Action}{Entity}RequestMapper` | `store/.../{module}/internal/api/rest/mapper/` |
| Model Assembler (entity) | `{Entity}ModelAssembler` | `store/.../{module}/internal/api/rest/assembler/` |
| Model Assembler (per-operation) | `{Action}{Entity}ModelAssembler` | `store/.../{module}/internal/api/rest/assembler/` |
| Domain Policy | `{Capability}Policy` | `{name}-domain/.../policy/` |
| Application Policy | `{Capability}Policy` | `store/.../{module}/internal/policy/` |
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
| API Root | `ApiRootController` | `store/.../` |
| Bounded Context Root | `{Context}RootController` | `store/.../{module}/` |
| Modulith named interface package (events) | `{module}.events` + `@NamedInterface("events")` | `store/.../{module}/events/` |
| Modulith named interface package (API links) | `{module}.api` + `@NamedInterface("api")` | `store/.../{module}/api/` |
| Cross-module HATEOAS link facade | `{Owner}ApiLinks` | `store/.../{owner}/api/` |
| **Provider Public Port (Inter-module contract)** | `{Capability}Port` or `{Capability}Query` | `store/.../{provider}/port/` |
| **Provider Public Port DTO** | Nested `record` inside the port interface | inside `store/.../{provider}/port/{Port}.java` |
| **Provider Port Adapter** | `{Capability}PortAdapter` or `{Capability}QueryAdapter` | `store/.../{provider}/internal/api/adapter/` |
| **Provider Port Mapper** | `{Capability}Mapper` | `store/.../{provider}/internal/api/adapter/mapper/` |
| **Consumer Outbound Adapter (Inter ACL)** | `{TargetDomain}{Capability}Adapter` | `store/.../{consumer}/internal/adapter/` |
| Test method | `{functionName}_{input}_{expectedBehavior}` | `src/test/java/...` |
