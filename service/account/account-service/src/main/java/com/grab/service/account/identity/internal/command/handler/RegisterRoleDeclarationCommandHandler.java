package com.grab.service.account.identity.internal.command.handler;

import com.grab.service.account.identity.internal.config.IdentityTransactional;
import com.grab.service.account.identity.internal.config.RequiresSecurityCatalog;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import com.identity.application.model.write.RegisterRoleDeclarationResult;
import com.identity.application.port.inbound.RegisterRoleDeclarationUseCase;
import org.springframework.stereotype.Component;

@Component
public class RegisterRoleDeclarationCommandHandler
        implements CommandHandler<RegisterRoleDeclarationCommand, RegisterRoleDeclarationResult> {

    private final RegisterRoleDeclarationUseCase useCase;

    public RegisterRoleDeclarationCommandHandler(RegisterRoleDeclarationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional
    public RegisterRoleDeclarationResult handle(RegisterRoleDeclarationCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<RegisterRoleDeclarationCommand> getCommandType() {
        return RegisterRoleDeclarationCommand.class;
    }
}
