# R4. Services

Load when creating or changing command/query services.

## MUST

- `@Service` + `@RequiredArgsConstructor`
- CommandService: inject `CommandBus` and `XxxRequestMapper`. Flow: `mapper.toCommand(dto)` then `commandBus.dispatch(command)` then `mapper.toResponse(result)`. Returns `XxxResponse`.
- QueryService: inject `QueryBus` and `XxxRequestMapper`.
  - Single: `mapper.toQuery(id)` then `queryBus.dispatch(query)` then `mapper.toResponse(result)`
  - Paginated list: `mapper.toQuery(filters, pageable)` then `queryBus.dispatch(query)` then `resultPage.map(mapper::toResponse)`

## MUST NOT

- Inject or call repositories (domain or infrastructure).
- Contain business rules. Those belong in policies or aggregates.
