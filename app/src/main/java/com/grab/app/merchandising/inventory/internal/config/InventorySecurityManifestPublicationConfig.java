package com.grab.app.merchandising.inventory.internal.config;

import com.grab.app.merchandising.inventory.port.event.InventorySecurityManifestDeclaredIntegrationEvent;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.inventory.adapter.persistence.adapter.InventorySecurityManifestEventFactory;
import com.inventory.application.port.inbound.GetInventorySecurityManifestPublicationStatusUseCase;
import com.inventory.application.port.inbound.PublishInventorySecurityManifestUseCase;
import com.inventory.application.service.GetInventorySecurityManifestPublicationStatusService;
import com.inventory.application.service.PublishInventorySecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class InventorySecurityManifestPublicationConfig {

    @Bean
    public InventorySecurityManifestEventFactory inventorySecurityManifestEventFactory() {
        return envelope -> new InventorySecurityManifestDeclaredIntegrationEvent(
                envelope.manifest(),
                envelope.eventId(),
                envelope.suppliedContentDigest(),
                envelope.publishedAt()
        );
    }

    @Bean
    public PublishInventorySecurityManifestUseCase publishInventorySecurityManifestUseCase(
            @Qualifier("inventorySecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishInventorySecurityManifestService(publication);
    }

    @Bean
    public GetInventorySecurityManifestPublicationStatusUseCase inventoryPublicationStatusUseCase(
            @Qualifier("inventorySecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetInventorySecurityManifestPublicationStatusService(publication);
    }
}
