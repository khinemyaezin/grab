package com.grab.service.merchandising.inventory.internal.command.handler;

import com.grab.framework.security.SecurityManifestPublicationPort.PublicationResult;
import com.grab.service.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.service.merchandising.shared.security.SecurityManifestPublicationRetryable;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;
import com.inventory.application.port.inbound.PublishInventorySecurityManifestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class PublishInventorySecurityManifestCommandHandler
        implements CommandHandler<PublishInventorySecurityManifestCommand, PublicationResult> {

    private final PublishInventorySecurityManifestUseCase useCase;

    public PublishInventorySecurityManifestCommandHandler(PublishInventorySecurityManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @SecurityManifestPublicationRetryable
    @InventoryTransactional(propagation = Propagation.REQUIRES_NEW)
    public PublicationResult handle(PublishInventorySecurityManifestCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<PublishInventorySecurityManifestCommand> getCommandType() {
        return PublishInventorySecurityManifestCommand.class;
    }
}

