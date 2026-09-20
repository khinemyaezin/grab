package com.identity.application.service;

import com.identity.application.port.inbound.LogoutUseCase;

import com.identity.application.model.write.LogoutCommand;
import com.identity.domain.service.TokenLifeCycle;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenLifeCycle tokenLifeCycle;
    public Void execute(LogoutCommand command) {
        tokenLifeCycle.revoke(command.refreshToken());
        return null;
    }
}
