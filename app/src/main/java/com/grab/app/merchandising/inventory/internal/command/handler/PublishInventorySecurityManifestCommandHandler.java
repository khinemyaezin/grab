package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.app.shared.security.SecurityManifestPublicationRetryable;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;
import com.inventory.application.port.inbound.PublishInventorySecurityManifestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class PublishInventorySecurityManifestCommandHandler
        implements CommandHandler<PublishInventorySecurityManifestCommand, Void> {

    private final PublishInventorySecurityManifestUseCase useCase;

    public PublishInventorySecurityManifestCommandHandler(PublishInventorySecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SecurityManifestPublicationRetryable
    @InventoryTransactional(propagation = Propagation.REQUIRES_NEW)
    public Void handle(PublishInventorySecurityManifestCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<PublishInventorySecurityManifestCommand> getCommandType() {
        return PublishInventorySecurityManifestCommand.class;
    }
}

