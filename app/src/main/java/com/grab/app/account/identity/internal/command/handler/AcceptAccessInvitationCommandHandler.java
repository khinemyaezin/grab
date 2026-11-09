package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.app.account.identity.internal.config.RequiresSecurityCatalog;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.AcceptAccessInvitationCommand;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.port.inbound.AcceptAccessInvitationUseCase;
import org.springframework.stereotype.Component;

@Component
public class AcceptAccessInvitationCommandHandler
        implements CommandHandler<AcceptAccessInvitationCommand, AccessAssignmentResult> {

    private final AcceptAccessInvitationUseCase acceptAccessInvitationUseCase;

    public AcceptAccessInvitationCommandHandler(AcceptAccessInvitationUseCase acceptAccessInvitationUseCase) {
        this.acceptAccessInvitationUseCase = acceptAccessInvitationUseCase;
    }

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional
    public AccessAssignmentResult handle(AcceptAccessInvitationCommand command) {
        return acceptAccessInvitationUseCase.execute(command);
    }

    @Override
    public Class<AcceptAccessInvitationCommand> getCommandType() {
        return AcceptAccessInvitationCommand.class;
    }
}
