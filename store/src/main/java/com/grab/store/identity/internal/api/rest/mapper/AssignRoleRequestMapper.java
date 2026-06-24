package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.command.AssignRoleCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import org.mapstruct.Mapper;
import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AssignRoleRequestMapper {
    public abstract AssignRoleCommand toCommand(String userId, String roleCode, boolean assign);
    public abstract UserProfileResponse toResponse(UserProfileResult result);
}
