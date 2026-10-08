package com.grab.store.inventory.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationAdapter;
import com.grab.store.shared.events.inventory.InventorySecurityManifestDeclaredIntegrationEvent;
import com.inventory.adapter.persistence.repository.jpa.InventorySecurityManifestPublicationJpaRepository;
import com.inventory.application.port.inbound.PublishInventorySecurityManifestUseCase;
import com.inventory.application.service.PublishInventorySecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationQueryAdapter;
import com.inventory.adapter.persistence.entity.InventorySecurityManifestPublicationEntity;
import com.inventory.application.port.inbound.GetInventorySecurityManifestPublicationStatusUseCase;
import com.inventory.application.service.GetInventorySecurityManifestPublicationStatusService;
import org.springframework.data.jpa.repository.JpaContext;
import java.time.Clock;
import java.time.Duration;

@Configuration
public class InventorySecurityManifestPublicationConfig {
    @Bean("inventorySecurityManifestPublicationPort")
    public SecurityManifestPublicationPort inventoryPublicationPort(
            InventorySecurityManifestPublicationJpaRepository repository,
            @Qualifier("inventoryDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        return new SecurityManifestPublicationAdapter(repository::lockByModuleKey, outbox,
                envelope -> {
                    var manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    var publishedAt = envelope.publishedAt();
                    return new InventorySecurityManifestDeclaredIntegrationEvent(manifest, eventId, digest, publishedAt);
                }, clock, interval);
    }

    @Bean
    public PublishInventorySecurityManifestUseCase publishInventorySecurityManifestUseCase(
            @Qualifier("inventorySecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishInventorySecurityManifestService(publication);
    }
    @Bean("inventorySecurityManifestPublicationQueryPort")
    public SecurityManifestPublicationQueryPort inventoryPublicationQueryPort(JpaContext context) {
        var entityManager = context.getEntityManagerByManagedType(InventorySecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, InventorySecurityManifestPublicationEntity.class,
                "InventoryOutboxEvent");
    }

    @Bean
    public GetInventorySecurityManifestPublicationStatusUseCase inventoryPublicationStatusUseCase(
            @Qualifier("inventorySecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetInventorySecurityManifestPublicationStatusService(publication);
    }
}
