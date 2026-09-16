# R5. Mappers

Load when creating or changing API MapStruct mappers.

## MUST

- One mapper class per handler/operation.
- File: `store/.../api/rest/mapper/{Action}{Entity}RequestMapper.java`
- Annotate with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`
- Declare as `public abstract class` (not interface)
- Provide exactly two methods:
  - `toCommand(params...)` -> Command, or `toQuery(params...)` -> Query
  - `toResponse(Result)` -> ResponseDto
