# R19. Naming

Load when naming a new type, package, or test method.

| Artifact | Pattern |
|---|---|
| Controller | `{Entity}Controller` |
| Command/Query Service | `{Entity}CommandService` / `{Entity}QueryService` |
| Request DTO | `{Action}{Entity}Request` |
| Response DTO | `{Entity}Response` |
| Command record | `{Action}{Entity}Command` |
| Query record | `Get{Entity}Query` / `List{Entities}Query` |
| Command/Query Handler | `{Action}{Entity}CommandHandler` / `{Action}{Entity}QueryHandler` |
| Mapper | `{Action}{Entity}RequestMapper` |
| Model Assembler (entity) | `{Entity}ModelAssembler` |
| Model Assembler (per-operation) | `{Action}{Entity}ModelAssembler` |
| Domain Policy | `{Capability}Policy` under `{name}-domain/.../policy/` |
| Application Policy | `{Capability}Policy` under `store/.../{module}/internal/policy/` |
| Inbound use case port | `{Action}{Entity}UseCase` under `{name}-application/.../port/inbound/` |
| Use case service | `{Action}{Entity}Service` under `{name}-application/.../service/` |
| Domain write repository port | `{Domain}Repository` under `{name}-domain/.../port/outbound/` |
| Application query port | `{Domain}QueryPort` under `{name}-application/.../port/outbound/` |
| Persistence write adapter | `{Domain}RepositoryAdapter` under `{name}-adapter-persistence/.../adapter/` |
| Persistence query adapter | `{Domain}QueryAdapter` under `{name}-adapter-persistence/.../adapter/` |
| Persistence executor | `{Domain}PersistenceExecutor` under `{name}-adapter-persistence/.../adapter/` |
| Spring Data JPA | `{Domain}JpaRepository` |
| Query specification | `{Domain}*Specification` (or `{Domain}SearchCriteria` + criteria helper) under `specification/jpa/` |
| Read view | `{Domain}View` / `{Domain}Summary` under `{name}-application/.../model/read/` |
| API Root | `ApiRootController` |
| Bounded Context Root | `{Context}RootController` |
| Modulith named interface package (events) | `{module}.events` + `@NamedInterface("events")` |
| Modulith named interface package (API links) | `{module}.api` + `@NamedInterface("api")` |
| Cross-module HATEOAS link facade | `{Owner}ApiLinks` under `{owner}.api` |
| Outbound Query Port (shared interface) | `{Domain}{Capability}Query` under `store/.../{domain}/query/` |
| Outbound Query Adapter | `{Domain}{Capability}QueryAdapter` under `store/.../{domain}/internal/api/adapter/` or `internal/api/query/` |
| Outbound Query Mapper | `{QueryPortName}Mapper` under `store/.../{domain}/internal/api/query/mapper/` |
| Outbound Query Response DTO | Declared inside the query interface (nested `record`) |
| Test method | `{functionName}_{input}_{expectedBehavior}` |
