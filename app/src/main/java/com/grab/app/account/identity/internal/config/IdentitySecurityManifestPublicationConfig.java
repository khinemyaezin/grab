package com.grab.app.account.identity.internal.config;

import com.grab.app.account.identity.port.event.IdentitySecurityManifestDeclaredIntegrationEvent;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.identity.application.port.inbound.GetIdentitySecurityManifestPublicationStatusUseCase;
import com.identity.application.port.inbound.PublishIdentitySecurityManifestUseCase;
import com.identity.application.service.GetIdentitySecurityManifestPublicationStatusService;
import com.identity.application.service.PublishIdentitySecurityManifestService;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class IdentitySecurityManifestPublicationConfig {

    @Bean("identitySecurityManifestEventFactory")
    public SecurityManifestEventFactory identitySecurityManifestEventFactory() {
        return envelope -> new IdentitySecurityManifestDeclaredIntegrationEvent(
                envelope.manifest(),
                envelope.eventId(),
                envelope.suppliedContentDigest(),
                envelope.publishedAt()
        );
    }

    @Bean
    public PublishIdentitySecurityManifestUseCase publishIdentitySecurityManifestUseCase(
            @Qualifier("identitySecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishIdentitySecurityManifestService(publication);
    }

    @Bean
    public GetIdentitySecurityManifestPublicationStatusUseCase identityPublicationStatusUseCase(
            @Qualifier("identitySecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetIdentitySecurityManifestPublicationStatusService(publication);
    }
}
