package com.grab.app.account.identity.internal.command.handler;

import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.grab.app.account.identity.internal.config.RequiresSecurityCatalog;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.RegisterSecurityManifestResult;
import com.identity.application.model.write.RevalidateSecurityManifestCommand;
import com.identity.application.port.inbound.RevalidateSecurityManifestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class RevalidateSecurityManifestCommandHandler
        implements CommandHandler<RevalidateSecurityManifestCommand, RegisterSecurityManifestResult> {

    private final RevalidateSecurityManifestUseCase useCase;

    public RevalidateSecurityManifestCommandHandler(RevalidateSecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

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
