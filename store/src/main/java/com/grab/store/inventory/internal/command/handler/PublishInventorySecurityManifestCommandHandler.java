package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;
import com.inventory.application.port.inbound.PublishInventorySecurityManifestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishInventorySecurityManifestCommandHandler
        implements CommandHandler<PublishInventorySecurityManifestCommand, Void> {
    private final PublishInventorySecurityManifestUseCase useCase;

    @Override
    @InventoryTransactional
    public Void handle(PublishInventorySecurityManifestCommand command) {
        useCase.execute(command);
        return null;
    }

    @Override
    public Class<PublishInventorySecurityManifestCommand> getCommandType() {
        return PublishInventorySecurityManifestCommand.class;
    }
}
