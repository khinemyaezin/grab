package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.UserProfileResponse;
import com.identity.application.model.write.ChangeUserStatusCommand;
import com.identity.application.model.write.UserProfileResult;
import com.identity.domain.enums.UserStatus;
import org.mapstruct.Mapper;
import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeUserStatusRequestMapper {
    public abstract ChangeUserStatusCommand toCommand(String userId, UserStatus status);
    public abstract UserProfileResponse toResponse(UserProfileResult result);
}
