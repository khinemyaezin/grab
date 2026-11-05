package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.UpdateScopeDisplayCommand;
import com.identity.application.port.inbound.UpdateScopeDisplayUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdateScopeDisplayCommandHandler implements CommandHandler<UpdateScopeDisplayCommand, Void> {

    private final UpdateScopeDisplayUseCase useCase;

    public UpdateScopeDisplayCommandHandler(UpdateScopeDisplayUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityTransactional
    public Void handle(UpdateScopeDisplayCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<UpdateScopeDisplayCommand> getCommandType() {
        return UpdateScopeDisplayCommand.class;
    }
}

