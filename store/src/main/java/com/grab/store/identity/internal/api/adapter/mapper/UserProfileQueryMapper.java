package com.grab.store.identity.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.store.identity.port.UserProfileQuery.UserProfileResponse;
import com.identity.application.model.read.GetUserProfileResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UserProfileQueryMapper {

    @Mapping(target = "platformCodes", expression = "java(mapPlatformCodes(result.accessContexts()))")
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
