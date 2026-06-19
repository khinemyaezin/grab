package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.response.UserMutationResponse;
import com.grab.store.identity.internal.command.ChangeUserStatusCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.identity.domain.enums.UserStatus;
import org.mapstruct.Mapper;
import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeUserStatusRequestMapper {
    public abstract ChangeUserStatusCommand toCommand(String userId, UserStatus status);
    public abstract UserMutationResponse toResponse(UserProfileResult result);
}
