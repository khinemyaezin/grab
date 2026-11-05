package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.EnsureSecurityCatalogStateCommand;
import com.identity.application.model.write.EnsureSecurityCatalogStateResult;
import com.identity.application.port.inbound.EnsureSecurityCatalogStateUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class EnsureSecurityCatalogStateCommandHandler implements
        CommandHandler<EnsureSecurityCatalogStateCommand, EnsureSecurityCatalogStateResult> {

    private final EnsureSecurityCatalogStateUseCase useCase;

    public EnsureSecurityCatalogStateCommandHandler(EnsureSecurityCatalogStateUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @IdentityTransactional(propagation = Propagation.REQUIRES_NEW)
    public EnsureSecurityCatalogStateResult handle(EnsureSecurityCatalogStateCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<EnsureSecurityCatalogStateCommand> getCommandType() {
        return EnsureSecurityCatalogStateCommand.class;
    }
}

