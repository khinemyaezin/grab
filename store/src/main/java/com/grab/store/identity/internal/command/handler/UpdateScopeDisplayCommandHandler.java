package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.UpdateScopeDisplayCommand;
import com.identity.application.port.inbound.UpdateScopeDisplayUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class UpdateScopeDisplayCommandHandler implements CommandHandler<UpdateScopeDisplayCommand, Void> {
    private final UpdateScopeDisplayUseCase useCase;

    @Override
    @IdentityTransactional(propagation = Propagation.REQUIRES_NEW)
    public Void handle(UpdateScopeDisplayCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<UpdateScopeDisplayCommand> getCommandType() {
        return UpdateScopeDisplayCommand.class;
    }
}
