package com.grab.store.saleschannel.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifest;
import com.grab.store.saleschannel.internal.config.SalesChannelTransactional;
import com.grab.store.shared.events.saleschannel.SalesChannelSecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.security.SecurityManifestPublicationCoordinator;
import com.saleschannel.application.model.write.PublishSalesChannelSecurityManifestCommand;
import com.saleschannel.application.security.SalesChannelSecurityManifest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PublishSalesChannelSecurityManifestCommandHandler
        implements CommandHandler<PublishSalesChannelSecurityManifestCommand, Void> {

    private final DomainEventProducer outbox;
    private final SecurityManifestPublicationCoordinator publication;

    public PublishSalesChannelSecurityManifestCommandHandler(
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("salesChannelSecurityManifestPublicationCoordinator") SecurityManifestPublicationCoordinator publication
    ) {
        this.outbox = outbox;
        this.publication = publication;
    }

    @Override
    @SalesChannelTransactional
    public Void handle(PublishSalesChannelSecurityManifestCommand command) {
        SecurityManifest manifest = SalesChannelSecurityManifest.CURRENT;
        boolean claimed = publication.claim(manifest);
        if (!claimed) {
            return null;
        }
        String eventId = UUID.randomUUID().toString();
        var declaration = new SalesChannelSecurityManifestDeclaredIntegrationEvent(manifest, eventId);
        outbox.produce("SecurityManifest", manifest.moduleKey(), List.of(declaration));
        return null;
    }

    @Override
    public Class<PublishSalesChannelSecurityManifestCommand> getCommandType() {
        return PublishSalesChannelSecurityManifestCommand.class;
    }
}
