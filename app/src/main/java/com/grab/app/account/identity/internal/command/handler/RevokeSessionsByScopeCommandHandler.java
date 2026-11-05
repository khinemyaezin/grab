package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import com.identity.application.port.inbound.RevokeSessionsByScopeUseCase;
import org.springframework.stereotype.Component;

@Component
public class RevokeSessionsByScopeCommandHandler
        implements CommandHandler<RevokeSessionsByScopeCommand, Void> {

    private final RevokeSessionsByScopeUseCase useCase;

    public RevokeSessionsByScopeCommandHandler(RevokeSessionsByScopeUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityTransactional
    public Void handle(RevokeSessionsByScopeCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<RevokeSessionsByScopeCommand> getCommandType() {
        return RevokeSessionsByScopeCommand.class;
    }
}

