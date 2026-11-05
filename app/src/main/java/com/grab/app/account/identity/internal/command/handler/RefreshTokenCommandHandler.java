package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.RefreshTokenCommand;
import com.identity.application.port.inbound.RefreshTokenUseCase;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCommandHandler implements CommandHandler<RefreshTokenCommand, AuthResult> {

    private final RefreshTokenUseCase refreshTokenUseCase;

    public RefreshTokenCommandHandler(RefreshTokenUseCase refreshTokenUseCase) {
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @Override
    @IdentityTransactional
    public AuthResult handle(RefreshTokenCommand command) {
        return refreshTokenUseCase.execute(command);
    }

    @Override
    public Class<RefreshTokenCommand> getCommandType() {
        return RefreshTokenCommand.class;
    }
}

