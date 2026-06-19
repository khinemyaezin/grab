package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.request.CreateRoleRequest;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.api.rest.mapper.CreateRoleRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.ManageAuthorityRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleCommandService {

    private final CommandBus commandBus;
    private final CreateRoleRequestMapper createRoleMapper;
    private final ManageAuthorityRequestMapper manageAuthorityMapper;

    public RoleResponse createRole(CreateRoleRequest request) {
        return createRoleMapper.toResponse(commandBus.dispatch(createRoleMapper.toCommand(request)));
    }

    public RoleResponse manageAuthority(String roleCode, String authorityCode, boolean assign) {
        return manageAuthorityMapper.toResponse(commandBus.dispatch(
                manageAuthorityMapper.toCommand(roleCode, authorityCode, assign)
        ));
    }
}
