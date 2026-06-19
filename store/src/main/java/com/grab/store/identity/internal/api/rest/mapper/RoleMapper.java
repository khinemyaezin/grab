package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.request.CreateRoleRequest;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.command.CreateRoleCommand;
import com.grab.store.identity.internal.command.ManageAuthorityCommand;
import com.grab.store.identity.internal.command.RoleResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class RoleMapper {
    public abstract CreateRoleCommand toCommand(CreateRoleRequest request);
    public abstract ManageAuthorityCommand toCommand(String roleCode, String authorityCode, boolean assign);
    public abstract RoleResponse toResponse(RoleResult result);
}
