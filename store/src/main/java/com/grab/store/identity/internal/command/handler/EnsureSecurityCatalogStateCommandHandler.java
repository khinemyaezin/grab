package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.EnsureSecurityCatalogStateCommand;
import com.identity.application.model.write.EnsureSecurityCatalogStateResult;
import com.identity.application.port.inbound.EnsureSecurityCatalogStateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class EnsureSecurityCatalogStateCommandHandler implements
        CommandHandler<EnsureSecurityCatalogStateCommand, EnsureSecurityCatalogStateResult> {
    private final EnsureSecurityCatalogStateUseCase useCase;

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
