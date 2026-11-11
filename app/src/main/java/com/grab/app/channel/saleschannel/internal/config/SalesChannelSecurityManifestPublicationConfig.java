package com.grab.app.channel.saleschannel.internal.config;

import com.grab.app.shared.events.saleschannel.SalesChannelSecurityManifestDeclaredIntegrationEvent;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.saleschannel.application.port.inbound.GetSalesChannelSecurityManifestPublicationStatusUseCase;
import com.saleschannel.application.port.inbound.PublishSalesChannelSecurityManifestUseCase;
import com.saleschannel.application.service.GetSalesChannelSecurityManifestPublicationStatusService;
import com.saleschannel.application.service.PublishSalesChannelSecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class SalesChannelSecurityManifestPublicationConfig {

    @Bean("saleschannelSecurityManifestEventFactory")
    public SecurityManifestEventFactory saleschannelSecurityManifestEventFactory() {
        return envelope -> new SalesChannelSecurityManifestDeclaredIntegrationEvent(
                envelope.manifest(),
                envelope.eventId(),
                envelope.suppliedContentDigest(),
                envelope.publishedAt()
        );
    }

    @Bean
    public PublishSalesChannelSecurityManifestUseCase publishSalesChannelSecurityManifestUseCase(
            @Qualifier("saleschannelSecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishSalesChannelSecurityManifestService(publication);
    }

    @Bean
    public GetSalesChannelSecurityManifestPublicationStatusUseCase saleschannelPublicationStatusUseCase(
            @Qualifier("saleschannelSecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetSalesChannelSecurityManifestPublicationStatusService(publication);
    }
}
