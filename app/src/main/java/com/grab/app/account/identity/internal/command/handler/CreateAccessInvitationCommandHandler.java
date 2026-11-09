package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.app.account.identity.internal.config.RequiresSecurityCatalog;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.AccessInvitationResult;
import com.identity.application.model.write.CreateAccessInvitationCommand;
import com.identity.application.port.inbound.CreateAccessInvitationUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreateAccessInvitationCommandHandler
        implements CommandHandler<CreateAccessInvitationCommand, AccessInvitationResult> {

    private final CreateAccessInvitationUseCase createAccessInvitationUseCase;

    public CreateAccessInvitationCommandHandler(CreateAccessInvitationUseCase createAccessInvitationUseCase) {
        this.createAccessInvitationUseCase = createAccessInvitationUseCase;
    }

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional
    public AccessInvitationResult handle(CreateAccessInvitationCommand command) {
        return createAccessInvitationUseCase.execute(command);
    }

    @Override
    public Class<CreateAccessInvitationCommand> getCommandType() {
        return CreateAccessInvitationCommand.class;
    }
}

