package com.grab.store.merchant.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.grab.store.shared.events.merchant.MerchantSecurityManifestDeclaredIntegrationEvent;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import com.merchant.application.service.PublishMerchantSecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import com.merchant.adapter.persistence.entity.MerchantSecurityManifestPublicationEntity;
import com.merchant.application.port.inbound.GetMerchantSecurityManifestPublicationStatusUseCase;
import com.merchant.application.service.GetMerchantSecurityManifestPublicationStatusService;
import com.grab.framework.security.SecurityManifest;
import org.springframework.data.jpa.repository.JpaContext;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.EntityManager;

@Configuration
@MerchantEnabled
public class MerchantSecurityManifestPublicationConfig {
    @Bean("merchantSecurityManifestPublicationPort")
    public SecurityManifestPublicationPort merchantPublicationPort(
            JpaContext context,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        EntityManager entityManager = context.getEntityManagerByManagedType(MerchantSecurityManifestPublicationEntity.class);
        var stateProvider = SecurityManifestPublicationStateProvider.optimisticProvider(
                entityManager, MerchantSecurityManifestPublicationEntity.class, MerchantSecurityManifestPublicationEntity::new);
        return new SecurityManifestPublicationAdapter(stateProvider, outbox,
                envelope -> {
                    SecurityManifest manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    Instant publishedAt = envelope.publishedAt();
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
        EntityManager entityManager = context.getEntityManagerByManagedType(MerchantSecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, MerchantSecurityManifestPublicationEntity.class,
                "MerchantOutboxEvent");
    }

    @Bean
    public GetMerchantSecurityManifestPublicationStatusUseCase merchantPublicationStatusUseCase(
            @Qualifier("merchantSecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetMerchantSecurityManifestPublicationStatusService(publication);
    }
}
