package com.grab.app.merchandising.catalog.internal.config;

import com.catalog.application.port.inbound.GetCatalogSecurityManifestPublicationStatusUseCase;
import com.catalog.application.port.inbound.PublishCatalogSecurityManifestUseCase;
import com.catalog.application.service.GetCatalogSecurityManifestPublicationStatusService;
import com.catalog.application.service.PublishCatalogSecurityManifestService;
import com.grab.app.merchandising.catalog.port.event.CatalogSecurityManifestDeclaredIntegrationEvent;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class CatalogSecurityManifestPublicationConfig {

    @Bean("catalogSecurityManifestEventFactory")
    public SecurityManifestEventFactory catalogSecurityManifestEventFactory() {
        return envelope -> new CatalogSecurityManifestDeclaredIntegrationEvent(
                envelope.manifest(),
                envelope.eventId(),
                envelope.suppliedContentDigest(),
                envelope.publishedAt()
        );
    }

    @Bean
    public PublishCatalogSecurityManifestUseCase publishCatalogSecurityManifestUseCase(
            @Qualifier("catalogSecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishCatalogSecurityManifestService(publication);
    }

    @Bean
    public GetCatalogSecurityManifestPublicationStatusUseCase catalogPublicationStatusUseCase(
            @Qualifier("catalogSecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetCatalogSecurityManifestPublicationStatusService(publication);
    }
}
