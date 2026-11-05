# R10. Service Communication & Universal Inbound Port Protocols

Load when exposing APIs between modules, creating Universal Java Port interfaces, or implementing inbound message adapters.

## 1. Universal Inbound Java Port Protocol (Tier 2)

Headless domain service groups in `app/` decouple themselves from HTTP REST controllers by exposing **Universal Inbound Java Port Interfaces**. In-process Java method calls are used today for zero-overhead communication between Platform BFFs and domain groups; however, ports are architected as 1:1 drop-in equivalents for future gRPC/Protobuf service stubs.

### Package & File Structure

| Component | Location | Example |
|---|---|---|
| Universal Port Interface & Nested DTOs | `app/src/main/java/com/grab/app/{group}/{domain}/port/message/{Capability}Port.java` | `com.grab.app.account.identity.port.message.AuthenticationPort` |
| Inbound Port Adapter | `app/src/main/java/com/grab/app/{group}/{domain}/internal/adapter/inbound/message/{Capability}PortAdapter.java` | `com.grab.app.account.identity.internal.adapter.inbound.message.AuthenticationPortAdapter` |
| Single-Operation Port Mappers | `app/src/main/java/com/grab/app/{group}/{domain}/internal/adapter/inbound/message/mapper/{Action}PortMapper.java` | `com.grab.app.account.identity.internal.adapter.inbound.message.mapper.LoginPortMapper` |

### Rules for Universal Ports

1. **Port Interface & Embedded DTOs**:
   - Resides in public port package: `com.grab.app.{group}.{domain}.port.message.{Capability}Port`.
   - **All message DTOs MUST be declared inside the port interface** as nested immutable Java `record`s (e.g. `record LoginMessage(...)`, `record AuthTokenResult(...)`).
   - Do NOT create standalone DTO files outside the port interface.
   - Designed for future Protobuf compatibility: methods take exactly one message parameter (or identifier) and return a result record or `Optional<Result>`.

2. **Inbound Port Adapter Implementation**:
   - Resides in `internal/adapter/inbound/message/{Capability}PortAdapter.java`.
   - Thin boundary: injects group-scoped `CommandBus` / `QueryBus` (or inbound `*UseCase`s) and dedicated single-operation MapStruct mappers.
   - Maps incoming message to command/query via `mapper.toCommand(...)` / `mapper.toQuery(...)`, dispatches via the bus (or calls use case), and returns `mapper.toResponse(...)`.
   - MUST NOT contain business rules, JPA repository calls, or direct SQL execution.

3. **Single-Operation Port Mappers**:
   - Resides in `internal/adapter/inbound/message/mapper/{Action}PortMapper.java`.
   - **Strictly 1:1**: exactly one MapStruct mapper class per operation.
   - Abstract class annotated with `@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)`.
   - Methods named strictly `toCommand(...)` / `toQuery(...)` and `toResponse(...)`.

---

## 2. Shared Interface Ports (Inter-Module Outbound Queries)

When a domain module needs to query another domain synchronously within the same runtime assembly:

| Component | Location | Example |
|---|---|---|
| Provider Public Port | `{provider}/port/{Capability}Query.java` | `com.grab.store.identity.port.UserProfileQuery` |
| Provider Port Adapter | `{provider}/internal/api/adapter/{Capability}QueryAdapter.java` | `com.grab.store.identity.internal.api.adapter.UserProfileQueryAdapter` |
| Provider Port Mapper | `{provider}/internal/api/adapter/mapper/{Capability}Mapper.java` | `com.grab.store.identity.internal.api.adapter.mapper.UserProfileQueryMapper` |
| Consumer Outbound Port | `{consumer}-application/.../port/outbound/{TargetDomain}{Capability}Port.java` | `com.customer.application.port.outbound.UserProfileQueryPort` |
| Consumer Outbound Adapter | `{consumer}/internal/adapter/{TargetDomain}{Capability}Adapter.java` | `com.grab.store.customer.internal.adapter.CustomerUserProfileQueryPortAdapter` |

> [!IMPORTANT]
> **Persistence is strictly INTRA**: `{name}-adapter-persistence` communicates only with its local database schema via JPA. It MUST NOT contain cross-module adapters or call other modules. All cross-module inter-communication adapters live in application or assembly internal packages.

---

## MUST

- Declare response and request DTOs as records inside the port interface.
- Implement the port interface in `{domain}/internal/adapter/inbound/message/{Port}Adapter`.
- Inject group-scoped CQRS buses or inbound `*UseCase` in the adapter.
- Use single-operation MapStruct mappers for mapping between port messages and application commands/queries.
- Demarcate transaction boundary on CQRS handlers or port adapter methods.

## MUST NOT

- Inject outbound persistence repositories or JPA repositories into port adapters.
- Define standalone DTO files outside the port interface for universal ports.
- Put business logic inside port adapters or mappers.
- Allow cross-module calls into another module's `internal/` packages.
