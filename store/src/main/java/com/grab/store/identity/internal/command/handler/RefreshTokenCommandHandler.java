package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.RefreshTokenUseCase;
import com.identity.application.model.write.RefreshTokenCommand;
import com.identity.application.model.write.AuthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCommandHandler implements CommandHandler<RefreshTokenCommand, AuthResult> {

    private final RefreshTokenUseCase refreshTokenUseCase;

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
