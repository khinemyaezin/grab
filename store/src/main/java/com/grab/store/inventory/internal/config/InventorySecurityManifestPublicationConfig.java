package com.grab.store.inventory.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.grab.store.shared.events.inventory.InventorySecurityManifestDeclaredIntegrationEvent;
import com.inventory.application.port.inbound.PublishInventorySecurityManifestUseCase;
import com.inventory.application.service.PublishInventorySecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import com.inventory.adapter.persistence.entity.InventorySecurityManifestPublicationEntity;
import com.inventory.application.port.inbound.GetInventorySecurityManifestPublicationStatusUseCase;
import com.inventory.application.service.GetInventorySecurityManifestPublicationStatusService;
import com.grab.framework.security.SecurityManifest;
import org.springframework.data.jpa.repository.JpaContext;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.EntityManager;

@Configuration
public class InventorySecurityManifestPublicationConfig {
    @Bean("inventorySecurityManifestPublicationPort")
    public SecurityManifestPublicationPort inventoryPublicationPort(
            JpaContext context,
            @Qualifier("inventoryDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        EntityManager entityManager = context.getEntityManagerByManagedType(InventorySecurityManifestPublicationEntity.class);
        var stateProvider = SecurityManifestPublicationStateProvider.optimisticProvider(
                entityManager, InventorySecurityManifestPublicationEntity.class, InventorySecurityManifestPublicationEntity::new);
        return new SecurityManifestPublicationAdapter(stateProvider, outbox,
                envelope -> {
                    SecurityManifest manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    Instant publishedAt = envelope.publishedAt();
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
        EntityManager entityManager = context.getEntityManagerByManagedType(InventorySecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, InventorySecurityManifestPublicationEntity.class,
                "InventoryOutboxEvent");
    }

    @Bean
    public GetInventorySecurityManifestPublicationStatusUseCase inventoryPublicationStatusUseCase(
            @Qualifier("inventorySecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetInventorySecurityManifestPublicationStatusService(publication);
    }
}
