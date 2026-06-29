package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.api.rest.mapper.ChangeUserStatusRequestMapper;
import com.grab.store.identity.internal.command.ChangeUserStatusCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import com.identity.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final CommandBus commandBus;
    private final ChangeUserStatusRequestMapper changeStatusMapper;

    public UserProfileResponse changeStatus(String id, UserStatus status) {
        ChangeUserStatusCommand command = changeStatusMapper.toCommand(id, status);
        UserProfileResult result = commandBus.dispatch(command);
        return changeStatusMapper.toResponse(result);
    }
}
