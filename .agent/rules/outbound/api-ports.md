# R10. Outbound API Rules (Shared Interface Ports)

Load when exposing APIs between modules via shared interface ports instead of gRPC.

## Context

When a module needs to expose query APIs to other modules synchronously (inter-module communication), shared interface ports are used in place of gRPC.

## Package & File Structure

| Component | Location | Example |
|---|---|---|
| Query Interface & DTOs | `store/.../{domain}/query/{Port}Query.java` (or `{domain}/internal/query/`) | `com.grab.store.identity.query.UserProfileQuery` |
| Query Adapter | `store/.../{domain}/internal/api/adapter/{Port}Adapter.java` (or `internal/api/query/`) | `com.grab.store.identity.internal.api.query.UserProfileQueryAdapter` |
| Query Mapper | `store/.../{domain}/internal/api/query/mapper/{QueryPortName}Mapper.java` | `com.grab.store.identity.internal.api.query.mapper.UserProfileQueryMapper` |

## Rules

### 1. Query Interface & DTOs
- The query interface must live in the module's query package (`store/.../{domain}/query/` or `{domain}/internal/query/`).
- **DTOs MUST only be declared inside the query interface** (e.g., as nested Java `record`s like `record UserProfileResponse(...)`).
- Do not create separate DTO files in `dto/` for shared outbound query ports.

### 2. Adapter Implementation
- The adapter implements the query interface and lives in `{domain}/internal/api/adapter/` or `{domain}/internal/api/query/`.
- **Must ONLY use inbound `*UseCase`** to retrieve data.
- **MUST NOT** inject outbound `*QueryPort`, `*Repository`, JPA repositories, or dispatch via `QueryBus` / `CommandBus`.
- **Transaction starts on this adapter method**: annotate the query method with `@{Module}ReadTransactional` (or `@{Module}Transactional` if writing).

### 3. Mapper
- Mapper lives in `store/.../{domain}/internal/api/query/mapper/{QueryPortName}Mapper.java`.
- MapStruct abstract class annotated with `@Mapper(config = CentralMapperConfig.class)`.
- Maps the use case's `*Result` to the query interface's DTO response.

## Reference Example

### 1. Interface with Embedded DTO: `UserProfileQuery.java`
```java
package com.grab.store.identity.query;

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
    ) {}
}
```

### 2. Adapter Implementation: `UserProfileQueryAdapter.java`
```java
package com.grab.store.identity.internal.api.query;

import com.grab.framework.id.Id;
import com.grab.store.identity.internal.api.query.mapper.UserProfileQueryMapper;
import com.grab.store.identity.internal.config.IdentityReadTransactional;
import com.grab.store.identity.query.UserProfileQuery;
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
package com.grab.store.identity.internal.api.query.mapper;

import com.grab.store.identity.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.store.identity.query.UserProfileQuery.UserProfileResponse;
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
