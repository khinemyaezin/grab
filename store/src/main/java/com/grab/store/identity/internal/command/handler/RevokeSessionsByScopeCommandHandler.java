package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import com.identity.application.port.inbound.RevokeSessionsByScopeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevokeSessionsByScopeCommandHandler implements CommandHandler<RevokeSessionsByScopeCommand, Void> {

    private final RevokeSessionsByScopeUseCase revokeSessionsByScopeUseCase;

    @Override
    @IdentityTransactional
    public Void handle(RevokeSessionsByScopeCommand command) {
        return revokeSessionsByScopeUseCase.execute(command);
    }

    @Override
    public Class<RevokeSessionsByScopeCommand> getCommandType() {
        return RevokeSessionsByScopeCommand.class;
    }
}
