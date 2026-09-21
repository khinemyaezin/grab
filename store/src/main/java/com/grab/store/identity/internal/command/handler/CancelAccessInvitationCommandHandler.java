package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.CancelAccessInvitationUseCase;
import com.identity.application.model.write.CancelAccessInvitationCommand;
import com.identity.application.model.write.AccessInvitationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelAccessInvitationCommandHandler implements CommandHandler<CancelAccessInvitationCommand, AccessInvitationResult> {

    private final CancelAccessInvitationUseCase cancelAccessInvitationUseCase;

    @Override
    @IdentityTransactional
    public AccessInvitationResult handle(CancelAccessInvitationCommand command) {
        return cancelAccessInvitationUseCase.execute(command);
    }

    @Override
    public Class<CancelAccessInvitationCommand> getCommandType() {
        return CancelAccessInvitationCommand.class;
    }
}
