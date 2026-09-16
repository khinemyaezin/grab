# R15. Naming

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
| Domain write repository | `{Domain}Repository` (domain) / `Default{Domain}Repository` (infra impl) |
| Query repository | `{Domain}QueryRepository` / `Default{Domain}QueryRepository` |
| Spring Data JPA | `{Domain}JpaRepository` |
| Query specification | `{Domain}*Specification` (or `{Domain}SearchCriteria` + criteria helper) under `specification/jpa/` |
| Read view | `{Domain}View` / `{Domain}Summary` under `view/` |
| API Root | `ApiRootController` |
| Bounded Context Root | `{Context}RootController` |
| Modulith named interface package (events) | `{module}.events` + `@NamedInterface("events")` |
| Modulith named interface package (API links) | `{module}.api` + `@NamedInterface("api")` |
| Cross-module HATEOAS link facade | `{Owner}ApiLinks` under `{owner}.api` |
| Test method | `{functionName}_{input}_{expectedBehavior}` |
