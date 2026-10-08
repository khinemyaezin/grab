package com.grab.store.identity.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationAdapter;
import com.grab.store.shared.events.identity.IdentitySecurityManifestDeclaredIntegrationEvent;
import com.identity.adapter.persistence.repository.jpa.IdentitySecurityManifestPublicationJpaRepository;
import com.identity.application.port.inbound.PublishIdentitySecurityManifestUseCase;
import com.identity.application.service.PublishIdentitySecurityManifestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.grab.framework.security.SecurityManifestPublicationQueryPort;
import com.grab.outbox.infrastructure.security.SecurityManifestPublicationQueryAdapter;
import com.identity.adapter.persistence.entity.IdentitySecurityManifestPublicationEntity;
import com.identity.application.port.inbound.GetIdentitySecurityManifestPublicationStatusUseCase;
import com.identity.application.service.GetIdentitySecurityManifestPublicationStatusService;
import org.springframework.data.jpa.repository.JpaContext;
import java.time.Clock;
import java.time.Duration;

@Configuration
public class IdentitySecurityManifestPublicationConfig {
    @Bean("identitySecurityManifestPublicationPort")
    public SecurityManifestPublicationPort identityPublicationPort(
            IdentitySecurityManifestPublicationJpaRepository repository,
            @Qualifier("identityDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        return new SecurityManifestPublicationAdapter(repository::lockByModuleKey, outbox,
                envelope -> {
                    var manifest = envelope.manifest();
                    String eventId = envelope.eventId();
                    String digest = envelope.suppliedContentDigest();
                    var publishedAt = envelope.publishedAt();
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
        var entityManager = context.getEntityManagerByManagedType(IdentitySecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, IdentitySecurityManifestPublicationEntity.class,
                "IdentityOutboxEvent");
    }

    @Bean
    public GetIdentitySecurityManifestPublicationStatusUseCase identityPublicationStatusUseCase(
            @Qualifier("identitySecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetIdentitySecurityManifestPublicationStatusService(publication);
    }
}
