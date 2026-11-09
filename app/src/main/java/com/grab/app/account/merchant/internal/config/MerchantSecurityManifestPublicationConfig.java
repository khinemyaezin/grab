package com.grab.app.account.merchant.internal.config;

import com.grab.app.account.merchant.port.event.MerchantSecurityManifestDeclaredIntegrationEvent;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.merchant.application.port.inbound.GetMerchantSecurityManifestPublicationStatusUseCase;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import com.merchant.application.service.GetMerchantSecurityManifestPublicationStatusService;
import com.merchant.application.service.PublishMerchantSecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class MerchantSecurityManifestPublicationConfig {

    @Bean("merchantSecurityManifestEventFactory")
    public SecurityManifestEventFactory merchantSecurityManifestEventFactory() {
        return envelope -> new MerchantSecurityManifestDeclaredIntegrationEvent(
                envelope.manifest(),
                envelope.eventId(),
                envelope.suppliedContentDigest(),
                envelope.publishedAt()
        );
    }

    @Bean
    public PublishMerchantSecurityManifestUseCase publishMerchantSecurityManifestUseCase(
            @Qualifier("merchantSecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishMerchantSecurityManifestService(publication);
    }

    @Bean
    public GetMerchantSecurityManifestPublicationStatusUseCase merchantPublicationStatusUseCase(
            @Qualifier("merchantSecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetMerchantSecurityManifestPublicationStatusService(publication);
    }
}
