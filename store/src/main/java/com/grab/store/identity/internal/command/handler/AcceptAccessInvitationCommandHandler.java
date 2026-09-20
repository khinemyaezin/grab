package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.AcceptAccessInvitationUseCase;
import com.identity.application.model.write.AcceptAccessInvitationCommand;
import com.identity.application.model.write.AccessAssignmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AcceptAccessInvitationCommandHandler implements CommandHandler<AcceptAccessInvitationCommand, AccessAssignmentResult> {

    private final AcceptAccessInvitationUseCase acceptAccessInvitationUseCase;

    @Override
    @IdentityTransactional
    public AccessAssignmentResult handle(AcceptAccessInvitationCommand command) {
        return acceptAccessInvitationUseCase.execute(command);
    }

    @Override
    public Class<AcceptAccessInvitationCommand> getCommandType() {
        return AcceptAccessInvitationCommand.class;
    }
}
