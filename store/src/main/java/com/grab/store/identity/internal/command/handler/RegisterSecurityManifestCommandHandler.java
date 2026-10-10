package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.config.RequiresSecurityCatalog;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestResult;
import org.springframework.transaction.annotation.Propagation;
import com.identity.application.port.inbound.RegisterSecurityManifestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterSecurityManifestCommandHandler
        implements CommandHandler<RegisterSecurityManifestCommand, RegisterSecurityManifestResult> {
    private final RegisterSecurityManifestUseCase useCase;

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional(propagation = Propagation.REQUIRES_NEW)
    public RegisterSecurityManifestResult handle(RegisterSecurityManifestCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<RegisterSecurityManifestCommand> getCommandType() {
        return RegisterSecurityManifestCommand.class;
    }
}
