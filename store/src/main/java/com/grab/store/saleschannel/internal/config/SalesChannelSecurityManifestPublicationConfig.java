package com.grab.store.saleschannel.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.grab.store.shared.events.saleschannel.SalesChannelSecurityManifestDeclaredIntegrationEvent;
import com.saleschannel.application.port.inbound.PublishSalesChannelSecurityManifestUseCase;
import com.saleschannel.application.service.PublishSalesChannelSecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import com.saleschannel.adapter.persistence.entity.SalesChannelSecurityManifestPublicationEntity;
import com.saleschannel.application.port.inbound.GetSalesChannelSecurityManifestPublicationStatusUseCase;
import com.saleschannel.application.service.GetSalesChannelSecurityManifestPublicationStatusService;
import com.grab.framework.security.SecurityManifest;
import org.springframework.data.jpa.repository.JpaContext;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.EntityManager;

@Configuration
public class SalesChannelSecurityManifestPublicationConfig {
    @Bean("saleschannelSecurityManifestPublicationPort")
    public SecurityManifestPublicationPort saleschannelPublicationPort(
            JpaContext context,
            @Qualifier("salesChannelDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        EntityManager entityManager = context.getEntityManagerByManagedType(SalesChannelSecurityManifestPublicationEntity.class);
        var stateProvider = SecurityManifestPublicationStateProvider.optimisticProvider(
                entityManager, SalesChannelSecurityManifestPublicationEntity.class, SalesChannelSecurityManifestPublicationEntity::new);
        return new SecurityManifestPublicationAdapter(stateProvider, outbox,
                envelope -> {
                    SecurityManifest manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    Instant publishedAt = envelope.publishedAt();
                    return new SalesChannelSecurityManifestDeclaredIntegrationEvent(manifest, eventId, digest, publishedAt);
                }, clock, interval);
    }

    @Bean
    public PublishSalesChannelSecurityManifestUseCase publishSalesChannelSecurityManifestUseCase(
            @Qualifier("saleschannelSecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishSalesChannelSecurityManifestService(publication);
    }
    @Bean("saleschannelSecurityManifestPublicationQueryPort")
    public SecurityManifestPublicationQueryPort saleschannelPublicationQueryPort(JpaContext context) {
        EntityManager entityManager = context.getEntityManagerByManagedType(SalesChannelSecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, SalesChannelSecurityManifestPublicationEntity.class,
                "SalesChannelOutboxEvent");
    }

    @Bean
    public GetSalesChannelSecurityManifestPublicationStatusUseCase saleschannelPublicationStatusUseCase(
            @Qualifier("saleschannelSecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetSalesChannelSecurityManifestPublicationStatusService(publication);
    }
}
