package com.grab.store.identity.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.grab.store.shared.events.identity.IdentitySecurityManifestDeclaredIntegrationEvent;
import com.identity.application.port.inbound.PublishIdentitySecurityManifestUseCase;
import com.identity.application.service.PublishIdentitySecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
import com.identity.adapter.persistence.entity.IdentitySecurityManifestPublicationEntity;
import com.identity.application.port.inbound.GetIdentitySecurityManifestPublicationStatusUseCase;
import com.identity.application.service.GetIdentitySecurityManifestPublicationStatusService;
import com.grab.framework.security.SecurityManifest;
import org.springframework.data.jpa.repository.JpaContext;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.EntityManager;

@Configuration
public class IdentitySecurityManifestPublicationConfig {
    @Bean("identitySecurityManifestPublicationPort")
    public SecurityManifestPublicationPort identityPublicationPort(
            JpaContext context,
            @Qualifier("identityDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        EntityManager entityManager = context.getEntityManagerByManagedType(IdentitySecurityManifestPublicationEntity.class);
        var stateProvider = SecurityManifestPublicationStateProvider.optimisticProvider(
                entityManager, IdentitySecurityManifestPublicationEntity.class, IdentitySecurityManifestPublicationEntity::new);
        return new SecurityManifestPublicationAdapter(stateProvider, outbox,
                envelope -> {
                    SecurityManifest manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    Instant publishedAt = envelope.publishedAt();
                    return new IdentitySecurityManifestDeclaredIntegrationEvent(manifest, eventId, digest, publishedAt);
                }, clock, interval);
    }

    @Bean
    public PublishIdentitySecurityManifestUseCase publishIdentitySecurityManifestUseCase(
            @Qualifier("identitySecurityManifestPublicationPort") SecurityManifestPublicationPort publication) {
        return new PublishIdentitySecurityManifestService(publication);
    }
    @Bean("identitySecurityManifestPublicationQueryPort")
    public SecurityManifestPublicationQueryPort identityPublicationQueryPort(JpaContext context) {
        EntityManager entityManager = context.getEntityManagerByManagedType(IdentitySecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, IdentitySecurityManifestPublicationEntity.class,
                "IdentityOutboxEvent");
    }

    @Bean
    public GetIdentitySecurityManifestPublicationStatusUseCase identityPublicationStatusUseCase(
            @Qualifier("identitySecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetIdentitySecurityManifestPublicationStatusService(publication);
    }
}
