package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.command.AuthResult;
import com.grab.store.identity.internal.command.RefreshTokenCommand;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.domain.service.TokenIssuer;
import com.identity.domain.service.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RefreshTokenCommandHandler implements CommandHandler<RefreshTokenCommand, AuthResult> {

    private final TokenIssuer tokenIssuer;

    @Override
    @IdentityTransactional
    public AuthResult handle(RefreshTokenCommand command) {
        TokenPair tokenPair = tokenIssuer.refresh(command.refreshToken());
        return new AuthResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.expiresInMs(),
                null,
                null,
                Set.of(),
                "ACTIVE"
        );
    }

    @Override
    public Class<RefreshTokenCommand> getCommandType() {
        return RefreshTokenCommand.class;
    }
}
