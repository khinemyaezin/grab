package com.identity.application.service;

import com.identity.application.port.inbound.RefreshTokenUseCase;

import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.RefreshTokenCommand;
import com.identity.domain.service.TokenLifeCycle;
import com.identity.domain.service.TokenPair;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenLifeCycle tokenLifeCycle;
    public AuthResult execute(RefreshTokenCommand command) {
        TokenPair tokenPair = tokenLifeCycle.refresh(command.refreshToken());
        return new AuthResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.expiresInMs(),
                null,
                null,
                Set.of(),
                "ACTIVE",
                !tokenPair.contextSelected()
        );
    }
}
