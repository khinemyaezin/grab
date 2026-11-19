package com.grab.service.account.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.service.account.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.ProvisionExternalUserCommand;
import com.identity.application.port.inbound.ProvisionExternalUserUseCase;
import org.springframework.stereotype.Component;

@Component
public class ProvisionExternalUserCommandHandler
        implements CommandHandler<ProvisionExternalUserCommand, ExternalUserProvisionResult> {
    private final ProvisionExternalUserUseCase useCase;

    public ProvisionExternalUserCommandHandler(ProvisionExternalUserUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityTransactional
    public ExternalUserProvisionResult handle(ProvisionExternalUserCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<ProvisionExternalUserCommand> getCommandType() {
        return ProvisionExternalUserCommand.class;
    }
}
