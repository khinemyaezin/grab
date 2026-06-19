package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.request.CreateRoleRequest;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.api.rest.mapper.RoleMapper;
import com.grab.store.identity.internal.api.rest.mapper.UserProfileMapper;
import com.identity.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityAdminCommandService {
    private final CommandBus bus;
    private final UserProfileMapper userProfileMapper;
    private final RoleMapper roleMapper;

    public UserProfileResponse status(String id, UserStatus status) {
        return userProfileMapper.toResponse(bus.dispatch(userProfileMapper.toCommand(id, status)));
    }

    public UserProfileResponse assignRole(String id, String code, boolean assign) {
        return userProfileMapper.toResponse(bus.dispatch(userProfileMapper.toCommand(id, code, assign)));
    }

    public RoleResponse createRole(CreateRoleRequest request) {
        return roleMapper.toResponse(bus.dispatch(roleMapper.toCommand(request)));
    }

    public RoleResponse authority(String roleCode, String authorityCode, boolean assign) {
        return roleMapper.toResponse(bus.dispatch(roleMapper.toCommand(roleCode, authorityCode, assign)));
    }
}
