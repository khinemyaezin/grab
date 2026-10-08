package com.grab.store.merchant.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationAdapter;
import com.grab.store.shared.events.merchant.MerchantSecurityManifestDeclaredIntegrationEvent;
import com.merchant.adapter.persistence.repository.jpa.MerchantSecurityManifestPublicationJpaRepository;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import com.merchant.application.service.PublishMerchantSecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationQueryAdapter;
import com.merchant.adapter.persistence.entity.MerchantSecurityManifestPublicationEntity;
import com.merchant.application.port.inbound.GetMerchantSecurityManifestPublicationStatusUseCase;
import com.merchant.application.service.GetMerchantSecurityManifestPublicationStatusService;
import org.springframework.data.jpa.repository.JpaContext;
import java.time.Clock;
import java.time.Duration;

@Configuration
@MerchantEnabled
public class MerchantSecurityManifestPublicationConfig {
    @Bean("merchantSecurityManifestPublicationPort")
    public SecurityManifestPublicationPort merchantPublicationPort(
            MerchantSecurityManifestPublicationJpaRepository repository,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        return new SecurityManifestPublicationAdapter(repository::lockByModuleKey, outbox,
                envelope -> {
                    var manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    var publishedAt = envelope.publishedAt();
                    return new MerchantSecurityManifestDeclaredIntegrationEvent(manifest, eventId, digest, publishedAt);
                }, clock, interval);
    }

    @Bean
    public PublishMerchantSecurityManifestUseCase publishMerchantSecurityManifestUseCase(
            @Qualifier("merchantSecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishMerchantSecurityManifestService(publication);
    }
    @Bean("merchantSecurityManifestPublicationQueryPort")
    public SecurityManifestPublicationQueryPort merchantPublicationQueryPort(JpaContext context) {
        var entityManager = context.getEntityManagerByManagedType(MerchantSecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, MerchantSecurityManifestPublicationEntity.class,
                "MerchantOutboxEvent");
    }

    @Bean
    public GetMerchantSecurityManifestPublicationStatusUseCase merchantPublicationStatusUseCase(
            @Qualifier("merchantSecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetMerchantSecurityManifestPublicationStatusService(publication);
    }
}
