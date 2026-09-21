package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.CreateAccessInvitationUseCase;
import com.identity.application.model.write.CreateAccessInvitationCommand;
import com.identity.application.model.write.AccessInvitationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateAccessInvitationCommandHandler implements CommandHandler<CreateAccessInvitationCommand, AccessInvitationResult> {

    private final CreateAccessInvitationUseCase createAccessInvitationUseCase;

    @Override
    @IdentityTransactional
    public AccessInvitationResult handle(CreateAccessInvitationCommand command) {
        return createAccessInvitationUseCase.execute(command);
    }

    @Override
    public Class<CreateAccessInvitationCommand> getCommandType() {
        return CreateAccessInvitationCommand.class;
    }
}
