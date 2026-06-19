package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.command.ManageAuthorityCommand;
import com.grab.store.identity.internal.command.RoleResult;
import org.mapstruct.Mapper;
import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ManageAuthorityRequestMapper {
    public abstract ManageAuthorityCommand toCommand(String roleCode, String authorityCode, boolean assign);
    public abstract RoleResponse toResponse(RoleResult result);
}
