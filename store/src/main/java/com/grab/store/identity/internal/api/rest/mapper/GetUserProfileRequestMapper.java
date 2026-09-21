package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import org.mapstruct.Mapper;
import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetUserProfileRequestMapper {
    public abstract GetUserProfileQuery toQuery(String userId);
    public abstract UserProfileResponse toResponse(GetUserProfileResult result);
}
