package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.request.*;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.api.rest.mapper.*;
import com.grab.store.identity.internal.command.LogoutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCommandService {
    private final CommandBus commandBus;
    private final RegisterUserRequestMapper registerMapper;
    private final LoginRequestMapper loginMapper;
    private final RefreshTokenRequestMapper refreshMapper;
    private final LogoutRequestMapper logoutMapper;

    public AuthResponse register(RegisterUserRequest r) {
        return registerMapper.toResponse(commandBus.dispatch(registerMapper.toCommand(r)));
    }

    public AuthResponse login(LoginRequest r) {
        return loginMapper.toResponse(commandBus.dispatch(loginMapper.toCommand(r)));
    }

    public AuthResponse refresh(RefreshTokenRequest r) {
        return refreshMapper.toResponse(commandBus.dispatch(refreshMapper.toCommand(r)));
    }

    public void logout(LogoutRequest r) {
        commandBus.dispatch(logoutMapper.toCommand(r));
    }
}
