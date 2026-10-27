package com.grab.app.account.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.shared.security.SecurityManifestPublicationRetryable;
import com.grab.app.account.identity.internal.config.IdentityTransactional;
import com.identity.application.model.write.PublishIdentitySecurityManifestCommand;
import com.identity.application.port.inbound.PublishIdentitySecurityManifestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class PublishIdentitySecurityManifestCommandHandler
        implements CommandHandler<PublishIdentitySecurityManifestCommand, Void> {
    private final PublishIdentitySecurityManifestUseCase useCase;

    @Override
    @SecurityManifestPublicationRetryable
    @IdentityTransactional(propagation = Propagation.REQUIRES_NEW)
    public Void handle(PublishIdentitySecurityManifestCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<PublishIdentitySecurityManifestCommand> getCommandType() {
        return PublishIdentitySecurityManifestCommand.class;
    }
}
