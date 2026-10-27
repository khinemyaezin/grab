package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.AccessContext;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.CurrentUserProfileResponse;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetCurrentUserProfileRequestMapper {
    public abstract GetUserProfileQuery toQuery(String userId);

    public abstract CurrentUserProfileResponse toResponse(GetUserProfileResult result, AccessContext currentAccessContext);
}
