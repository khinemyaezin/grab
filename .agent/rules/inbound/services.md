# R4. Services

Load when creating or changing command/query services.

## MUST

- `@Service` + `@RequiredArgsConstructor`
- Maintain strict separation between CommandService (writes) and QueryService (reads).
- CommandService: inject only `CommandBus`, operation-specific request mappers (`XxxRequestMapper`), and identity utilities (e.g. `IdGenerator`). Flow: `mapper.toCommand(dto)` then `commandBus.dispatch(command)` then `mapper.toResponse(result)`. Returns `XxxResponse` or ID.
- QueryService: inject only `QueryBus` and query mappers (`XxxRequestMapper`).
  - Single: `mapper.toQuery(id)` then `queryBus.dispatch(query)` then `mapper.toResponse(result)`
  - Paginated list: `mapper.toQuery(filters, pageable)` then `queryBus.dispatch(query)` then `resultPage.map(mapper::toResponse)`

## MUST NOT

- Mess with each other (no cross-calling between command and query services):
  - `CommandService` MUST NOT inject, call, or invoke `QueryBus` or any `QueryService`.
  - `CommandService` MUST NOT perform queries. If business logic needs aggregate data, the command handler/use case must load the aggregate via domain repository port, or required inputs must be provided in the request payload.
  - `QueryService` MUST NOT inject, call, or invoke `CommandBus` or any `CommandService`.
  - `QueryService` MUST NOT trigger state changes or side effects.
- Inject or call ports, repositories, or adapters. Only handlers and use cases touch ports.
- Contain business rules. Those belong in policies or aggregates.
