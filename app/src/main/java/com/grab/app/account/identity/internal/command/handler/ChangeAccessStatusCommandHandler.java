package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.app.account.identity.internal.config.RequiresSecurityCatalog;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.ChangeAccessStatusCommand;
import com.identity.application.port.inbound.ChangeAccessStatusUseCase;
import org.springframework.stereotype.Component;

@Component
public class ChangeAccessStatusCommandHandler implements CommandHandler<ChangeAccessStatusCommand, AccessAssignmentResult> {

    private final ChangeAccessStatusUseCase changeAccessStatusUseCase;

    public ChangeAccessStatusCommandHandler(ChangeAccessStatusUseCase changeAccessStatusUseCase) {
        this.changeAccessStatusUseCase = changeAccessStatusUseCase;
    }

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional
    public AccessAssignmentResult handle(ChangeAccessStatusCommand command) {
        return changeAccessStatusUseCase.execute(command);
    }

    @Override
    public Class<ChangeAccessStatusCommand> getCommandType() {
        return ChangeAccessStatusCommand.class;
    }
}

