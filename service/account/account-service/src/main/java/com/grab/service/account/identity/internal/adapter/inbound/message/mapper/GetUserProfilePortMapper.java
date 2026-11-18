package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.IdentityAdminPort.UserDetailResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.read.GetUserProfileQuery;
import com.identity.application.model.read.GetUserProfileResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetUserProfilePortMapper {

    public GetUserProfileQuery toQuery(Id userId) {
        return userId != null ? new GetUserProfileQuery(userId) : null;
    }

    public abstract UserDetailResult toResponse(GetUserProfileResult result);

    protected abstract UserDetailResult.AccessContextInfo toContextInfo(GetUserProfileResult.AccessContextInfo info);
}

