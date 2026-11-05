package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.RoleResult;
import com.identity.application.port.inbound.CreateRoleUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreateRoleCommandHandler implements CommandHandler<CreateRoleCommand, RoleResult> {

    private final CreateRoleUseCase createRoleUseCase;

    public CreateRoleCommandHandler(CreateRoleUseCase createRoleUseCase) {
        this.createRoleUseCase = createRoleUseCase;
    }

    @Override
    @IdentityTransactional
    public RoleResult handle(CreateRoleCommand command) {
        return createRoleUseCase.execute(command);
    }

    @Override
    public Class<CreateRoleCommand> getCommandType() {
        return CreateRoleCommand.class;
    }
}

