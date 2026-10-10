package com.grab.store.catalog.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.grab.store.shared.events.catalog.CatalogSecurityManifestDeclaredIntegrationEvent;
import com.catalog.application.port.inbound.PublishCatalogSecurityManifestUseCase;
import com.catalog.application.service.PublishCatalogSecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import com.catalog.adapter.persistence.entity.CatalogSecurityManifestPublicationEntity;
import com.catalog.application.port.inbound.GetCatalogSecurityManifestPublicationStatusUseCase;
import com.catalog.application.service.GetCatalogSecurityManifestPublicationStatusService;
import com.grab.framework.security.SecurityManifest;
import org.springframework.data.jpa.repository.JpaContext;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import jakarta.persistence.EntityManager;

@Configuration
public class CatalogSecurityManifestPublicationConfig {
    @Bean("catalogSecurityManifestPublicationPort")
    public SecurityManifestPublicationPort catalogPublicationPort(
            JpaContext context,
            @Qualifier("catalogDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        EntityManager entityManager = context.getEntityManagerByManagedType(CatalogSecurityManifestPublicationEntity.class);
        Function<String, CatalogSecurityManifestPublicationEntity> stateProvider = SecurityManifestPublicationStateProvider
                .optimisticProvider(
                        entityManager,
                        CatalogSecurityManifestPublicationEntity.class,
                        CatalogSecurityManifestPublicationEntity::new);

        return new SecurityManifestPublicationAdapter(stateProvider, outbox,
                envelope -> {
                    SecurityManifest manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    Instant publishedAt = envelope.publishedAt();
                    return new CatalogSecurityManifestDeclaredIntegrationEvent(manifest, eventId, digest, publishedAt);
                }, clock, interval);
    }

    @Bean
    public PublishCatalogSecurityManifestUseCase publishCatalogSecurityManifestUseCase(
            @Qualifier("catalogSecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishCatalogSecurityManifestService(publication);
    }

    @Bean("catalogSecurityManifestPublicationQueryPort")
    public SecurityManifestPublicationQueryPort catalogPublicationQueryPort(JpaContext context) {
        EntityManager entityManager = context.getEntityManagerByManagedType(CatalogSecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, CatalogSecurityManifestPublicationEntity.class,
                "CatalogOutboxEvent");
    }

    @Bean
    public GetCatalogSecurityManifestPublicationStatusUseCase catalogPublicationStatusUseCase(
            @Qualifier("catalogSecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetCatalogSecurityManifestPublicationStatusService(publication);
    }
}
