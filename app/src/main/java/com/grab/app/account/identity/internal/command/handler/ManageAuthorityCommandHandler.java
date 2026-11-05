package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.ManageAuthorityCommand;
import com.identity.application.model.write.RoleResult;
import com.identity.application.port.inbound.ManageAuthorityUseCase;
import org.springframework.stereotype.Component;

@Component
public class ManageAuthorityCommandHandler implements CommandHandler<ManageAuthorityCommand, RoleResult> {

    private final ManageAuthorityUseCase manageAuthorityUseCase;

    public ManageAuthorityCommandHandler(ManageAuthorityUseCase manageAuthorityUseCase) {
        this.manageAuthorityUseCase = manageAuthorityUseCase;
    }

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

