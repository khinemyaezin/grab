# R4. Services & Inbound Adapters

Load when creating or changing BFF services, command/query services, or inbound port adapters.

## Service Archetypes

1. **BFF Orchestration Services (`app/{bff}/service/`)**:
   - Compose multi-port calls across multiple domain groups for complex screen-level requirements.
   - Inject domain Universal Java Port interfaces (`*Port`).
   - Pure orchestration and presentation mapping; no direct database, repository, or CQRS bus access.
2. **Inbound Port Adapters (`app/{group}/{domain}/internal/adapter/inbound/message/`)**:
   - Thin message boundary implementing the domain's universal Java port (`*Port`).
   - Inject group-scoped `CommandBus` / `QueryBus` and dedicated single-operation MapStruct mappers (`*PortMapper`).
   - Map port messages via `mapper.toCommand(...)` / `mapper.toQuery(...)`, dispatch via bus, and return `mapper.toResponse(...)`.
3. **Legacy CQRS Command & Query Services (`store/.../service/`)**:
   - Dispatches via `CommandBus` or `QueryBus`.

## MUST

- `@Service` or `@Component` + `@RequiredArgsConstructor`.
- Maintain strict CQRS separation between Command path (writes) and Query path (reads).
- Write path: inject only group-scoped `CommandBus`, operation-specific request mappers (`toCommand`), and identity utilities (`IdGenerator`). Dispatches command, returns response or ID.
- Read path: inject only group-scoped `QueryBus` and query mappers (`toQuery`). Dispatches query, returns response.

## MUST NOT

- Cross-call between Command and Query pipelines:
  - Write services / adapters MUST NOT inject, call, or dispatch via `QueryBus` or any query service.
  - Write services / adapters MUST NOT perform queries. If business logic needs aggregate data, the command handler/use case must load the aggregate via domain repository port, or required inputs must be provided in the request payload.
  - Read services / adapters MUST NOT inject, call, or dispatch via `CommandBus` or any command service.
  - Read services / adapters MUST NOT trigger state changes or side effects.
- Inject or call persistence repositories, JPA repositories, or database adapters. Only handlers and use cases touch persistence ports.
- Contain domain business rules. Those belong in domain aggregates or domain policies.
