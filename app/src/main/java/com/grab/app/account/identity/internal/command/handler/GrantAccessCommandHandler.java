package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.app.account.identity.internal.config.RequiresSecurityCatalog;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.GrantAccessCommand;
import com.identity.application.port.inbound.GrantAccessUseCase;
import org.springframework.stereotype.Component;

@Component
public class GrantAccessCommandHandler implements CommandHandler<GrantAccessCommand, AccessAssignmentResult> {

    private final GrantAccessUseCase grantAccessUseCase;

    public GrantAccessCommandHandler(GrantAccessUseCase grantAccessUseCase) {
        this.grantAccessUseCase = grantAccessUseCase;
    }

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional
    public AccessAssignmentResult handle(GrantAccessCommand command) {
        return grantAccessUseCase.execute(command);
    }

    @Override
    public Class<GrantAccessCommand> getCommandType() {
        return GrantAccessCommand.class;
    }
}

