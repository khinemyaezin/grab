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
    private final CommandBus bus;
    private final RegisterUserRequestMapper registerMapper;
    private final LoginRequestMapper loginMapper;
    private final RefreshTokenRequestMapper refreshMapper;

    public AuthResponse register(RegisterRequest r) {
        return registerMapper.toResponse(bus.dispatch(registerMapper.toCommand(r)));
    }

    public AuthResponse login(LoginRequest r) {
        return loginMapper.toResponse(bus.dispatch(loginMapper.toCommand(r)));
    }

    public AuthResponse refresh(RefreshTokenRequest r) {
        return refreshMapper.toResponse(bus.dispatch(refreshMapper.toCommand(r)));
    }

    public void logout(RefreshTokenRequest r) {
        bus.dispatch(new LogoutCommand(r.refreshToken()));
    }
}
