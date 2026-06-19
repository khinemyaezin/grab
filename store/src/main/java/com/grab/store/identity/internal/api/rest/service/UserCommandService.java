package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.response.UserMutationResponse;
import com.grab.store.identity.internal.api.rest.mapper.AssignRoleRequestMapper;
import com.grab.store.identity.internal.api.rest.mapper.ChangeUserStatusRequestMapper;
import com.identity.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final CommandBus commandBus;
    private final ChangeUserStatusRequestMapper changeStatusMapper;
    private final AssignRoleRequestMapper assignRoleMapper;

    public UserMutationResponse changeStatus(String id, UserStatus status) {
        return changeStatusMapper.toResponse(commandBus.dispatch(changeStatusMapper.toCommand(id, status)));
    }

    public UserMutationResponse assignRole(String id, String code, boolean assign) {
        return assignRoleMapper.toResponse(commandBus.dispatch(assignRoleMapper.toCommand(id, code, assign)));
    }
}
