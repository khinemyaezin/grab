# R5. Mappers

Load when creating or changing API MapStruct mappers.

## MUST

### REST Mappers
- One mapper class per handler/operation.
- File: `store/.../api/rest/mapper/{Action}{Entity}RequestMapper.java`
- Annotate with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`
- Declare as `public abstract class` (not interface)
- Provide exactly two methods:
  - `toCommand(params...)` -> Command, or `toQuery(params...)` -> Query
  - `toResponse(Result)` -> ResponseDto

### Outbound Query Mappers (Shared Interface Ports)
- One mapper class per outbound query port.
- File: `store/.../{domain}/internal/api/query/mapper/{QueryPortName}Mapper.java`
- Annotate with `@Mapper(config = CentralMapperConfig.class)`
- Declare as `public abstract class` (not interface)
- Map use case `*Result` to the query interface's embedded response DTO (`toResponse(Result) -> ResponseDto`).
