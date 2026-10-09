package com.grab.store.inventory.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.grab.store.shared.events.inventory.InventorySecurityManifestDeclaredIntegrationEvent;
import com.inventory.adapter.persistence.repository.jpa.InventorySecurityManifestPublicationJpaRepository;
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

import com.grab.store.shared.security.SecurityManifestPublicationStateProvider;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.function.Function;

@Configuration
public class InventorySecurityManifestPublicationConfig {
    @Bean("inventorySecurityManifestPublicationPort")
    public SecurityManifestPublicationPort inventoryPublicationPort(
            InventorySecurityManifestPublicationJpaRepository repository,
            @Qualifier("inventoryDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        Function<String, InventorySecurityManifestPublicationEntity> stateProvider =
                SecurityManifestPublicationStateProvider.lockingProvider(
                        repository,
                        repository::lockByModuleKey,
                        InventorySecurityManifestPublicationEntity::new,
                        transactionManager);
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
        jakarta.persistence.EntityManager entityManager = context.getEntityManagerByManagedType(InventorySecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, InventorySecurityManifestPublicationEntity.class,
                "InventoryOutboxEvent");
    }

    @Bean
    public GetInventorySecurityManifestPublicationStatusUseCase inventoryPublicationStatusUseCase(
            @Qualifier("inventorySecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetInventorySecurityManifestPublicationStatusService(publication);
    }
}
