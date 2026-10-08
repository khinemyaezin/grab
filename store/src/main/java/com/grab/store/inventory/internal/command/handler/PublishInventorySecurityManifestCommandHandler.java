package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifest;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.shared.events.inventory.InventorySecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.security.SecurityManifestPublicationCoordinator;
import com.inventory.application.model.write.PublishInventorySecurityManifestCommand;
import com.inventory.application.security.InventorySecurityManifest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PublishInventorySecurityManifestCommandHandler
        implements CommandHandler<PublishInventorySecurityManifestCommand, Void> {

    private final DomainEventProducer outbox;
    private final SecurityManifestPublicationCoordinator publication;

    public PublishInventorySecurityManifestCommandHandler(
            @Qualifier("inventoryDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("inventorySecurityManifestPublicationCoordinator") SecurityManifestPublicationCoordinator publication
    ) {
        this.outbox = outbox;
        this.publication = publication;
    }

    @Override
    @InventoryTransactional
    public Void handle(PublishInventorySecurityManifestCommand command) {
        SecurityManifest manifest = InventorySecurityManifest.CURRENT;
        boolean claimed = publication.claim(manifest);
        if (!claimed) {
            return null;
        }
        String eventId = UUID.randomUUID().toString();
        var declaration = new InventorySecurityManifestDeclaredIntegrationEvent(manifest, eventId);
        outbox.produce("SecurityManifest", manifest.moduleKey(), List.of(declaration));
        return null;
    }

    @Override
    public Class<PublishInventorySecurityManifestCommand> getCommandType() {
        return PublishInventorySecurityManifestCommand.class;
    }
}
