package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import com.identity.application.model.write.RegisterRoleDeclarationResult;
import com.identity.application.port.inbound.RegisterRoleDeclarationUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class RegisterRoleDeclarationCommandHandler
        implements CommandHandler<RegisterRoleDeclarationCommand, RegisterRoleDeclarationResult> {

    private final RegisterRoleDeclarationUseCase useCase;

    public RegisterRoleDeclarationCommandHandler(RegisterRoleDeclarationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityTransactional(propagation = Propagation.REQUIRES_NEW)
    public RegisterRoleDeclarationResult handle(RegisterRoleDeclarationCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<RegisterRoleDeclarationCommand> getCommandType() {
        return RegisterRoleDeclarationCommand.class;
    }
}
