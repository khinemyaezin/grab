package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifest;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.grab.store.shared.events.merchant.MerchantSecurityManifestDeclaredIntegrationEvent;
import com.grab.store.shared.security.SecurityManifestPublicationCoordinator;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import com.merchant.application.security.MerchantSecurityManifest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PublishMerchantSecurityManifestCommandHandler
        implements CommandHandler<PublishMerchantSecurityManifestCommand, Void> {

    private final DomainEventProducer outbox;
    private final SecurityManifestPublicationCoordinator publication;

    public PublishMerchantSecurityManifestCommandHandler(
            @Qualifier("merchantDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("merchantSecurityManifestPublicationCoordinator") SecurityManifestPublicationCoordinator publication
    ) {
        this.outbox = outbox;
        this.publication = publication;
    }

    @Override
    @MerchantTransactional
    public Void handle(PublishMerchantSecurityManifestCommand command) {
        SecurityManifest manifest = MerchantSecurityManifest.CURRENT;
        boolean claimed = publication.claim(manifest);
        if (!claimed) {
            return null;
        }
        String eventId = UUID.randomUUID().toString();
        var declaration = new MerchantSecurityManifestDeclaredIntegrationEvent(manifest, eventId);
        outbox.produce("SecurityManifest", manifest.moduleKey(), List.of(declaration));
        return null;
    }

    @Override
    public Class<PublishMerchantSecurityManifestCommand> getCommandType() {
        return PublishMerchantSecurityManifestCommand.class;
    }
}
