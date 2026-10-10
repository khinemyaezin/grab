package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.config.RequiresSecurityCatalog;
import com.identity.application.model.write.RevalidateSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestResult;
import com.identity.application.port.inbound.RevalidateSecurityManifestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class RevalidateSecurityManifestCommandHandler
        implements CommandHandler<RevalidateSecurityManifestCommand, RegisterSecurityManifestResult> {
    private final RevalidateSecurityManifestUseCase useCase;

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional(propagation = Propagation.REQUIRES_NEW)
    public RegisterSecurityManifestResult handle(RevalidateSecurityManifestCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<RevalidateSecurityManifestCommand> getCommandType() {
        return RevalidateSecurityManifestCommand.class;
    }
}
