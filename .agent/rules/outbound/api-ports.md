# R10. Outbound API Rules (Shared Interface Ports)

Load when exposing APIs between modules via shared interface ports instead of gRPC.

## Context

When a module needs to expose query APIs to other modules synchronously (inter-module communication), shared interface ports are used in place of gRPC.

## Package & File Structure

### 1. Provider Side (Exposing the Port)

| Component | Location | Example |
|---|---|---|
| Public Port & DTOs | `store/.../{provider}/port/{Capability}Port.java` (or `{Capability}Query.java`) | `com.grab.store.identity.port.UserProfileQuery` |
| Provider Port Adapter | `store/.../{provider}/internal/api/adapter/{Capability}PortAdapter.java` (or `{Capability}QueryAdapter.java`) | `com.grab.store.identity.internal.api.adapter.UserProfileQueryAdapter` |
| Provider Port Mapper | `store/.../{provider}/internal/api/adapter/mapper/{Capability}Mapper.java` | `com.grab.store.identity.internal.api.adapter.mapper.UserProfileQueryMapper` |

### 2. Consumer Side (Consuming the Port via Anti-Corruption Layer)

| Component | Location | Example |
|---|---|---|
| Consumer Outbound Port | `{consumer}-application/.../port/outbound/{TargetDomain}{Capability}Port.java` | `com.customer.application.port.outbound.UserProfileQueryPort` |
| Consumer Outbound Adapter | `store/.../{consumer}/internal/adapter/{TargetDomain}{Capability}Adapter.java` | `com.grab.store.customer.internal.adapter.CustomerUserProfileQueryPortAdapter` |

> [!IMPORTANT]
> **Persistence is strictly INTRA**: `{name}-adapter-persistence` communicates only with its local database via JPA. It MUST NOT contain `{Inter}Adapter` or call other modules. All cross-module inter-communication adapters live in `store/.../{consumer}/internal/adapter/`.

## Rules

### 1. Provider Public Port & DTOs
- The public port interface lives in the module's public port package (`store/.../{provider}/port/`).
- **DTOs MUST only be declared inside the port interface** (e.g., as nested Java `record`s like `record UserProfileResponse(...)` or `record GrantAccessRequest(...)`).
- Do not create separate DTO files in `dto/` for shared outbound ports.

### 2. Provider Adapter Implementation
- The adapter implements the provider port interface and lives in `store/.../{provider}/internal/api/adapter/`.
- **Must ONLY use inbound `*UseCase`** to retrieve data or execute commands.
- **MUST NOT** inject outbound `*QueryPort`, `*Repository`, JPA repositories, or dispatch via `QueryBus` / `CommandBus`.
- **Transaction starts on this adapter method**: annotate the query method with `@{Module}ReadTransactional` (or `@{Module}Transactional` if writing).

### 3. Provider Mapper
- Mapper lives in `store/.../{provider}/internal/api/adapter/mapper/{Capability}Mapper.java`.
- MapStruct abstract class annotated with `@Mapper(config = CentralMapperConfig.class)`.
- Maps the use case's `*Result` to the port interface's DTO response.

### 4. Consumer Outbound Port & Adapter (ACL)
- The consumer application defines an outbound port in `{consumer}-application/.../port/outbound/{TargetDomain}{Capability}Port.java` in terms of its own domain needs.
- The consumer implements this port in `store/.../{consumer}/internal/adapter/{TargetDomain}{Capability}Adapter.java`.
- The adapter injects the provider's public port (`store/.../{provider}/port/*`) and maps the response to the consumer's model.

## Reference Example

### 1. Interface with Embedded DTO: `UserProfileQuery.java`

```java
package com.grab.store.identity.port;

import com.grab.framework.id.Id;

import java.util.List;

public interface UserProfileQuery {
    UserProfileResponse getUserProfile(Id userId);

    record UserProfileResponse(
            String id,
            String email,
            String status,
            String createdAt,
            List<String> platformCode
    ) {
    }
}
```

### 2. Adapter Implementation: `UserProfileQueryAdapter.java`

```java
package com.grab.store.identity.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.store.identity.internal.api.adapter.mapper.UserProfileQueryMapper;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.port.UserProfileQuery;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import com.identity.application.port.inbound.GetUserProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileQueryAdapter implements UserProfileQuery {
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UserProfileQueryMapper userProfileQueryMapper;

    @Override
    @IdentityReadTransactional
    public UserProfileResponse getUserProfile(Id userId) {
        GetUserProfileResult profile = getUserProfileUseCase.execute(new GetUserProfileQuery(userId));
        return userProfileQueryMapper.toResponse(profile);
    }
}
```

### 3. Mapper: `UserProfileQueryMapper.java`

```java
package com.grab.store.identity.internal.api.adapter.mapper;

import com.grab.store.identity.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.store.identity.port.UserProfileQuery.UserProfileResponse;
import com.identity.application.model.read.GetUserProfileResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper(config = CentralMapperConfig.class)
public abstract class UserProfileQueryMapper {

    @Mapping(target = "platformCode", expression = "java(mapPlatformCodes(result.accessContexts()))")
    public abstract UserProfileResponse toResponse(GetUserProfileResult result);

    protected List<String> mapPlatformCodes(List<GetUserProfileResult.AccessContextInfo> accessContexts) {
        if (accessContexts == null) {
            return Collections.emptyList();
        }
        return accessContexts.stream()
                .map(GetUserProfileResult.AccessContextInfo::platformCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
```

## MUST
- Declare response DTOs as records inside the query interface.
- Implement the query interface in `{domain}/internal/api/adapter/{Port}Adapter` or `{domain}/internal/api/query/{Port}Adapter`.
- Inject only `*UseCase` in the adapter to fetch results.
- Demarcate transaction boundary on the adapter method with `@{Module}ReadTransactional`.
- Place mappers in `{domain}/internal/api/query/mapper/{QueryPortName}Mapper`.

## MUST NOT
- Inject outbound `*QueryPort`, `*Repository`, or JPA repositories into the query adapter.
- Dispatch via `QueryBus` or `CommandBus` from the query adapter.
- Define standalone DTO files outside the query interface.
- Omit the transaction annotation on the adapter query method.
