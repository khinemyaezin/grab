package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.config.RequiresSecurityCatalog;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import com.identity.application.model.write.RegisterRoleDeclarationResult;
import com.identity.application.port.inbound.RegisterRoleDeclarationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterRoleDeclarationCommandHandler
        implements CommandHandler<RegisterRoleDeclarationCommand, RegisterRoleDeclarationResult> {

    private final RegisterRoleDeclarationUseCase registerRoleDeclarationUseCase;

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional
    public RegisterRoleDeclarationResult handle(RegisterRoleDeclarationCommand command) {
        return registerRoleDeclarationUseCase.execute(command);
    }

    @Override
    public Class<RegisterRoleDeclarationCommand> getCommandType() {
        return RegisterRoleDeclarationCommand.class;
    }
}
