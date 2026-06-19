package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.CurrentUserProfileResponse;
import com.grab.store.identity.internal.query.GetUserProfileQuery;
import com.grab.store.identity.internal.query.GetUserProfileResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCurrentUserProfileRequestMapper {
    public abstract GetUserProfileQuery toQuery(String userId);

    public abstract CurrentUserProfileResponse toResponse(GetUserProfileResult result);
}
