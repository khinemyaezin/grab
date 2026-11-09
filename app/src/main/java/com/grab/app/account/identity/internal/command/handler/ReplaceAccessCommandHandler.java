package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.app.account.identity.internal.config.RequiresSecurityCatalog;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.port.inbound.ReplaceAccessUseCase;
import org.springframework.stereotype.Component;

@Component
public class ReplaceAccessCommandHandler implements CommandHandler<ReplaceAccessCommand, AccessAssignmentResult> {

    private final ReplaceAccessUseCase replaceAccessUseCase;

    public ReplaceAccessCommandHandler(ReplaceAccessUseCase replaceAccessUseCase) {
        this.replaceAccessUseCase = replaceAccessUseCase;
    }

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional
    public AccessAssignmentResult handle(ReplaceAccessCommand command) {
        return replaceAccessUseCase.execute(command);
    }

    @Override
    public Class<ReplaceAccessCommand> getCommandType() {
        return ReplaceAccessCommand.class;
    }
}

