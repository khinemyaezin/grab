package com.grab.service.account.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.service.account.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.UpdateExternalUserStatusCommand;
import com.identity.application.port.inbound.UpdateExternalUserUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdateExternalUserStatusCommandHandler
        implements CommandHandler<UpdateExternalUserStatusCommand, ExternalUserProvisionResult> {
    private final UpdateExternalUserUseCase useCase;

    public UpdateExternalUserStatusCommandHandler(UpdateExternalUserUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityTransactional
    public ExternalUserProvisionResult handle(UpdateExternalUserStatusCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<UpdateExternalUserStatusCommand> getCommandType() {
        return UpdateExternalUserStatusCommand.class;
    }
}
