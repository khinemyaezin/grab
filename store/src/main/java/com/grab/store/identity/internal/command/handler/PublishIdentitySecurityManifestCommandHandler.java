package com.grab.store.identity.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifest;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.shared.events.identity.IdentitySecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.security.SecurityManifestPublicationCoordinator;
import com.identity.application.model.write.PublishIdentitySecurityManifestCommand;
import com.identity.application.security.IdentitySecurityManifest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PublishIdentitySecurityManifestCommandHandler
        implements CommandHandler<PublishIdentitySecurityManifestCommand, Void> {

    private final DomainEventProducer outbox;
    private final SecurityManifestPublicationCoordinator publication;

    public PublishIdentitySecurityManifestCommandHandler(
            @Qualifier("identityDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("identitySecurityManifestPublicationCoordinator") SecurityManifestPublicationCoordinator publication
    ) {
        this.outbox = outbox;
        this.publication = publication;
    }

    @Override
    @IdentityTransactional
    public Void handle(PublishIdentitySecurityManifestCommand command) {
        SecurityManifest manifest = IdentitySecurityManifest.CURRENT;
        boolean claimed = publication.claim(manifest);
        if (!claimed) {
            return null;
        }
        String eventId = UUID.randomUUID().toString();
        var declaration = new IdentitySecurityManifestDeclaredIntegrationEvent(manifest, eventId);
        outbox.produce("SecurityManifest", manifest.moduleKey(), List.of(declaration));
        return null;
    }

    @Override
    public Class<PublishIdentitySecurityManifestCommand> getCommandType() {
        return PublishIdentitySecurityManifestCommand.class;
    }
}
