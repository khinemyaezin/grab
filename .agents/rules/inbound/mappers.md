# R5. Mappers

Load when creating or changing MapStruct mappers.

## Single-Operation Mapper Invariant

**Every mapper class MUST strictly handle exactly one command or query operation and return one result class.** Monolithic mappers bundling multiple operations into a single class are strictly prohibited.

## MUST

### 1. Universal Inbound Port Mappers (`app/`)
- Exactly one mapper class per port operation: `app/{group}/{domain}/internal/adapter/inbound/message/mapper/{Action}PortMapper.java`.
- Annotate with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
- Declare as `public abstract class` (not an interface).
- Provide strictly two methods:
  - `toCommand(message...)` -> `Command<R>`, or `toQuery(message...)` -> `Query<R>`
  - `toResponse(Result)` -> Port Response Record
- Maximize MapStruct automatic mapping: use `@Mapping(...)` for field renamings (e.g. `@Mapping(source = "sellerId", target = "merchantId")`), identifier conversions, and collections. Avoid procedural loops.

### 2. Platform BFF REST Mappers (`app/{bff}/` or `store/`)
- One mapper class per REST endpoint/operation: `app/{bff}/api/rest/mapper/{Action}{Entity}RequestMapper.java`.
- Annotate with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
- Declare as `public abstract class`.
- Provide strictly:
  - `toCommand(dto, actorId...)` -> `Command<R>`, or `toQuery(dto, pageable...)` -> `Query<R>`
  - `toResponse(Result)` -> Response DTO

### 3. Outbound Query Mappers (Shared Interface Ports)
- File: `store/.../{domain}/internal/api/adapter/mapper/{QueryPortName}Mapper.java`.
- Annotate with `@Mapper(config = CentralMapperConfig.class)`.
- Declare as `public abstract class`.
- Map use case `*Result` to the query interface's embedded response DTO (`toResponse(Result)`).

## MUST NOT

- Combine multiple command or query operations inside a single mapper class.
- Put domain business logic or aggregate validation inside mappers.
- Write manual procedural collection copying when MapStruct can map the types automatically.
