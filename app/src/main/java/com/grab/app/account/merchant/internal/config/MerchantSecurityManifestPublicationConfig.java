package com.grab.app.account.merchant.internal.config;

import com.grab.app.shared.events.merchant.MerchantRoleDeclarationDeclaredIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantSecurityManifestDeclaredIntegrationEvent;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;
import com.manifest.adapter.persistence.RoleDeclarationEventFactory;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import com.merchant.application.port.inbound.GetMerchantSecurityManifestPublicationStatusUseCase;
import com.merchant.application.port.inbound.PublishMerchantRoleDeclarationUseCase;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import com.merchant.application.service.GetMerchantSecurityManifestPublicationStatusService;
import com.merchant.application.service.PublishMerchantRoleDeclarationService;
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

    @Bean("merchantRoleDeclarationEventFactory")
    public RoleDeclarationEventFactory merchantRoleDeclarationEventFactory() {
        return envelope -> new MerchantRoleDeclarationDeclaredIntegrationEvent(
                envelope.declaration(),
                envelope.eventId(),
                envelope.suppliedContentDigest(),
                envelope.publishedAt()
        );
    }

    @Bean
    public PublishMerchantRoleDeclarationUseCase publishMerchantRoleDeclarationUseCase(
            @Qualifier("merchantRoleDeclarationPublicationPort") RoleDeclarationPublicationPort publication) {
        return new PublishMerchantRoleDeclarationService(publication);
    }
}
