package com.grab.service.account.identity.internal.command.handler;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.grab.service.account.identity.internal.config.IdentityTransactional;
import com.grab.service.account.shared.security.SecurityManifestPublicationRetryable;
import com.grab.framework.cqrs.command.CommandHandler;
import com.identity.application.model.write.PublishIdentitySecurityManifestCommand;
import com.identity.application.port.inbound.PublishIdentitySecurityManifestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class PublishIdentitySecurityManifestCommandHandler
        implements CommandHandler<PublishIdentitySecurityManifestCommand, PublicationResult> {

    private final PublishIdentitySecurityManifestUseCase useCase;

    public PublishIdentitySecurityManifestCommandHandler(PublishIdentitySecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SecurityManifestPublicationRetryable
    @IdentityTransactional(propagation = Propagation.REQUIRES_NEW)
    public PublicationResult handle(PublishIdentitySecurityManifestCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<PublishIdentitySecurityManifestCommand> getCommandType() {
        return PublishIdentitySecurityManifestCommand.class;
    }
}

