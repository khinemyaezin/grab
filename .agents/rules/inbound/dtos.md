# R8. DTOs & Message Types

Load when creating or changing request/response DTOs or port message contracts.

## DTO Taxonomy

1. **Platform BFF DTOs (`app/{bff}/dto/`)**:
   - Location: `app/{bff}/dto/request/` and `app/{bff}/dto/response/`.
   - Purpose: Client-facing payloads tailored to specific personas (Seller vs. Marketplace buyer/guest).
   - Validation: Request DTOs use Jakarta Bean Validation (`@NotBlank`, `@Min`, `@NotNull`, `@Pattern`, `@Size`).
   - Types: Primitives and `String` only on response DTOs.
2. **Universal Port Message DTOs (`app/{group}/{domain}/port/message/`)**:
   - Location: Declared **directly inside the port interface** (`*Port.java`) as nested Java records (e.g. `record CreateProductMessage(...)`, `record ProductFactsResult(...)`).
   - Purpose: Framework-agnostic message exchange contracts between BFFs and headless domain services. Designed as 1:1 drop-in equivalents for future Protobuf/gRPC message contracts.
   - Do NOT create standalone DTO files outside the port interface for universal ports.
3. **Application CQRS Records (`{bc}-application/model/`)**:
   - Commands: `model/write/{Action}{Entity}Command.java` implementing `Command<R>`.
   - Queries: `model/read/{QueryName}Query.java` implementing `Query<R>`.
   - Results: `model/write/*Result.java` or `model/read/*Result.java`.

## MUST

- All DTOs, messages, commands, queries, and results MUST be immutable Java `record` types.
- Request DTOs in Platform BFFs use Jakarta Bean Validation annotations.
- Response DTOs use primitive types, `String`, and framework `Id` only.
- Defensive copies for collections inside records: `items = items == null ? List.of() : List.copyOf(items)`.

## MUST NOT

- Put domain aggregate entities or JPA entities on response DTOs or port messages.
- Accept or return domain aggregates or JPA entities from controllers or port interfaces.
- Define standalone files for port message types outside the port interface.
