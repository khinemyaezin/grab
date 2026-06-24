package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.request.CreateRoleRequest;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.api.rest.mapper.CreateRoleRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.ManageAuthorityRequestMapper;
import com.grab.store.identity.internal.command.CreateRoleCommand;
import com.grab.store.identity.internal.command.ManageAuthorityCommand;
import com.grab.store.identity.internal.command.RoleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleCommandService {

    private final CommandBus commandBus;
    private final CreateRoleRequestMapper createRoleMapper;
    private final ManageAuthorityRequestMapper manageAuthorityMapper;

    public RoleResponse createRole(CreateRoleRequest request) {
        CreateRoleCommand command = createRoleMapper.toCommand(request);
        RoleResult result = commandBus.dispatch(command);
        return createRoleMapper.toResponse(result);
    }

    public RoleResponse manageAuthority(String roleCode, String authorityCode, boolean assign) {
        ManageAuthorityCommand command = manageAuthorityMapper.toCommand(roleCode, authorityCode, assign);
        RoleResult result = commandBus.dispatch(command);
        return manageAuthorityMapper.toResponse(result);
    }
}
