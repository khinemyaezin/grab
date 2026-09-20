package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.request.CreateRoleRequest;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.RoleResult;
import org.mapstruct.Mapper;
import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateRoleRequestMapper {
    public abstract CreateRoleCommand toCommand(CreateRoleRequest request);
    public abstract RoleResponse toResponse(RoleResult result);
}
