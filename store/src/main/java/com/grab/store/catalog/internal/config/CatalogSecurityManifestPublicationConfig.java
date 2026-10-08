package com.grab.store.catalog.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationAdapter;
import com.grab.store.shared.events.catalog.CatalogSecurityManifestDeclaredIntegrationEvent;
import com.catalog.adapter.persistence.repository.jpa.CatalogSecurityManifestPublicationJpaRepository;
import com.catalog.application.port.inbound.PublishCatalogSecurityManifestUseCase;
import com.catalog.application.service.PublishCatalogSecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationQueryAdapter;
import com.catalog.adapter.persistence.entity.CatalogSecurityManifestPublicationEntity;
import com.catalog.application.port.inbound.GetCatalogSecurityManifestPublicationStatusUseCase;
import com.catalog.application.service.GetCatalogSecurityManifestPublicationStatusService;
import org.springframework.data.jpa.repository.JpaContext;
import java.time.Clock;
import java.time.Duration;

@Configuration
public class CatalogSecurityManifestPublicationConfig {
    @Bean("catalogSecurityManifestPublicationPort")
    public SecurityManifestPublicationPort catalogPublicationPort(
            CatalogSecurityManifestPublicationJpaRepository repository,
            @Qualifier("catalogDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        return new SecurityManifestPublicationAdapter(repository::lockByModuleKey, outbox,
                envelope -> {
                    var manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    var publishedAt = envelope.publishedAt();
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
        var entityManager = context.getEntityManagerByManagedType(CatalogSecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, CatalogSecurityManifestPublicationEntity.class,
                "CatalogOutboxEvent");
    }

    @Bean
    public GetCatalogSecurityManifestPublicationStatusUseCase catalogPublicationStatusUseCase(
            @Qualifier("catalogSecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetCatalogSecurityManifestPublicationStatusService(publication);
    }
}
