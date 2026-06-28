package com.grab.store.identity.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.identity.internal.api.rest.dto.request.*;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.api.rest.mapper.*;
import com.grab.store.identity.internal.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCommandService {
    private final CommandBus commandBus;
    private final LoginRequestMapper loginMapper;
    private final RefreshTokenRequestMapper refreshMapper;
    private final LogoutRequestMapper logoutMapper;

    public AuthResponse login(LoginRequest r) {
        LoginCommand command = loginMapper.toCommand(r);
        AuthResult result =  commandBus.dispatch(command);
        return loginMapper.toResponse(result);
    }

    public AuthResponse refresh(RefreshTokenRequest r) {
        RefreshTokenCommand command = refreshMapper.toCommand(r);
        AuthResult result = commandBus.dispatch(command);
        return refreshMapper.toResponse(result);
    }

    public void logout(LogoutRequest r) {
        commandBus.dispatch(logoutMapper.toCommand(r));
    }
}
