package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.shared.security.SecurityManifestPublicationRetryable;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;
import com.inventory.application.port.inbound.PublishInventorySecurityManifestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class PublishInventorySecurityManifestCommandHandler
        implements CommandHandler<PublishInventorySecurityManifestCommand, Void> {
    private final PublishInventorySecurityManifestUseCase useCase;

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
