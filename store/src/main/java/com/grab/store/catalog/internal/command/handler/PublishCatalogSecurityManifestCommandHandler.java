package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.PublishCatalogSecurityManifestCommand;
import com.catalog.application.security.CatalogSecurityManifest;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifest;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.shared.events.catalog.CatalogSecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.security.SecurityManifestPublicationCoordinator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PublishCatalogSecurityManifestCommandHandler
        implements CommandHandler<PublishCatalogSecurityManifestCommand, Void> {

    private final DomainEventProducer outbox;
    private final SecurityManifestPublicationCoordinator publication;

    public PublishCatalogSecurityManifestCommandHandler(
            @Qualifier("catalogDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("catalogSecurityManifestPublicationCoordinator") SecurityManifestPublicationCoordinator publication
    ) {
        this.outbox = outbox;
        this.publication = publication;
    }

    @Override
    @CatalogTransactional
    public Void handle(PublishCatalogSecurityManifestCommand command) {
        SecurityManifest manifest = CatalogSecurityManifest.CURRENT;
        boolean claimed = publication.claim(manifest);
        if (!claimed) {
            return null;
        }
        String eventId = UUID.randomUUID().toString();
        var declaration = new CatalogSecurityManifestDeclaredIntegrationEvent(manifest, eventId);
        outbox.produce("SecurityManifest", manifest.moduleKey(), List.of(declaration));
        return null;
    }

    @Override
    public Class<PublishCatalogSecurityManifestCommand> getCommandType() {
        return PublishCatalogSecurityManifestCommand.class;
    }
}
