---
name: create-cqrs-operation
description: >-
  Step-by-step procedure and recipes for creating CQRS commands, queries, use cases, handlers,
  DTOs, MapStruct mappers, controllers, and HATEOAS model assemblers.
---

# Create CQRS Operation Guide

Use this skill when adding or refactoring an HTTP endpoint, write command, read query, or CQRS pipeline.

---

## Data Flow Overview

### 1. Write Flow (Command)
```text
Controller (POST/PUT/PATCH/DELETE)
  -> XxxCommandService
    -> XxxRequestMapper.toCommand(dto)
    -> CommandBus.dispatch(command)
      -> XxxCommandHandler (@{Module}Transactional)
        -> XxxUseCase.execute(command)
          -> Domain Aggregate / Domain Repository Port
        <- Result
    <- Result
    -> XxxRequestMapper.toResponse(result)
  <- ResponseEntity<EntityModel<ResponseDto>> (or 201 Created / 204 No Content)
```

### 2. Read Flow (Query)
```text
Controller (GET)
  -> XxxQueryService
    -> XxxRequestMapper.toQuery(params / filters)
    -> QueryBus.dispatch(query)
      -> XxxQueryHandler (@{Module}ReadTransactional)
        -> XxxUseCase.execute(query)
          -> Outbound Query Port / View Projection
        <- Result
    <- Result
    -> result.map(mapper::toResponse) or mapper.toResponse(result)
  <- ResponseEntity<EntityModel<ResponseDto>> or ResponseEntity<PagedModel<EntityModel<ResponseDto>>>
```

---

## File Creation Checklist

### 1. Application Layer (`{bc}-application/`)
- [ ] **Command / Query Record**: `model/write/{Action}{Entity}Command.java` or `model/read/Get{Entity}Query.java`.
  - Must implement `com.grab.framework.cqrs.Command<R>` or `Query<R>`.
  - Paginated queries implement `Query<Page<R>>` and `PageableQueryRequest`.
  - Must use `Id` for entity references, never raw `String`.
- [ ] **Result Record**: `model/write/{Action}{Entity}Result.java` or `model/read/{Entity}Result.java`.
  - Simple Java record using primitives, `String`, and `Id`. Never domain aggregates.
- [ ] **Inbound Port (UseCase)**: `port/inbound/{Action}{Entity}UseCase.java`.
  - Interface declaring `Result execute(Command command);`.
- [ ] **UseCase Service Implementation**: `service/{Action}{Entity}Service.java`.
  - Implements the UseCase interface. Framework-free (no Spring annotations). Injects domain repository or query port.

### 2. Assembly & Inbound Web Layer (`store/.../{bc}/`)
- [ ] **Request / Response DTOs**: `internal/api/rest/dto/request/` and `dto/response/`.
  - Java records. Request DTOs have Jakarta validation (`@NotBlank`, etc.). Response DTOs have primitive/String types.
- [ ] **MapStruct Request Mapper**: `internal/api/rest/mapper/{Action}{Entity}RequestMapper.java`.
  - Abstract class with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
  - Methods: `toCommand(...)` / `toQuery(...)` and `toResponse(...)`.
- [ ] **Handler**: `internal/command/handler/{Action}{Entity}CommandHandler.java` or `internal/query/handler/`.
  - Annotated with `@Component` and `@{Module}Transactional` (writes) or `@{Module}ReadTransactional` (reads).
  - Implements `CommandHandler<C, R>` or `QueryHandler<Q, R>`.
  - Delegates to `useCase.execute(...)`.
- [ ] **Wire UseCase Bean**: In `{Bc}UseCaseConfig.java`, declare `@Bean` instantiating `{Action}{Entity}Service`.
- [ ] **Command / Query Service**: `internal/api/rest/service/{Bc}CommandService.java` or `{Bc}QueryService.java`.
  - `@Service` injecting `CommandBus` or `QueryBus` + mapper.
  - Strict isolation: `CommandService` never touches queries. `QueryService` never touches commands.
- [ ] **HATEOAS Model Assembler**: `internal/api/rest/hateoas/{Entity}ModelAssembler.java` or per-operation assembler.
  - Implements `RepresentationModelAssembler<ResponseDto, EntityModel<ResponseDto>>`.
  - Uses `linkTo(methodOn(...))` with inline kebab-case rels (`self`, `get-{entity}`, `list-{entities}`, `create-{entity}`).
- [ ] **Controller**: `internal/api/rest/controller/{Entity}Controller.java`.
  - `@RestController` at `/api/v1/{resources}`.
  - Injects `CommandService`, `QueryService`, and assembler.
  - Returns `ResponseEntity<EntityModel<ResponseDto>>` or `ResponseEntity<PagedModel<EntityModel<ResponseDto>>>`.
