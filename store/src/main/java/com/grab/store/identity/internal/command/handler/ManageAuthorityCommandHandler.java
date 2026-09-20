package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.port.inbound.ManageAuthorityUseCase;
import com.identity.application.model.write.ManageAuthorityCommand;
import com.identity.application.model.write.RoleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManageAuthorityCommandHandler implements CommandHandler<ManageAuthorityCommand, RoleResult> {

    private final ManageAuthorityUseCase manageAuthorityUseCase;

    @Override
    @IdentityTransactional
    public RoleResult handle(ManageAuthorityCommand command) {
        return manageAuthorityUseCase.execute(command);
    }

    @Override
    public Class<ManageAuthorityCommand> getCommandType() {
        return ManageAuthorityCommand.class;
    }
}
