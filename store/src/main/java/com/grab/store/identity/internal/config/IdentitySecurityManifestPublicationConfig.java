package com.grab.store.identity.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.grab.store.shared.events.identity.IdentitySecurityManifestDeclaredIntegrationEvent;
import com.identity.adapter.persistence.repository.jpa.IdentitySecurityManifestPublicationJpaRepository;
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

import com.grab.store.shared.security.SecurityManifestPublicationStateProvider;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.function.Function;

@Configuration
public class IdentitySecurityManifestPublicationConfig {
    @Bean("identitySecurityManifestPublicationPort")
    public SecurityManifestPublicationPort identityPublicationPort(
            IdentitySecurityManifestPublicationJpaRepository repository,
            @Qualifier("identityDomainEventProducer") DomainEventProducer outbox,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        Function<String, IdentitySecurityManifestPublicationEntity> stateProvider =
                SecurityManifestPublicationStateProvider.lockingProvider(
                        repository,
                        repository::lockByModuleKey,
                        IdentitySecurityManifestPublicationEntity::new,
                        transactionManager);
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
        jakarta.persistence.EntityManager entityManager = context.getEntityManagerByManagedType(IdentitySecurityManifestPublicationEntity.class);
        return new SecurityManifestPublicationQueryAdapter(entityManager, IdentitySecurityManifestPublicationEntity.class,
                "IdentityOutboxEvent");
    }

    @Bean
    public GetIdentitySecurityManifestPublicationStatusUseCase identityPublicationStatusUseCase(
            @Qualifier("identitySecurityManifestPublicationQueryPort") SecurityManifestPublicationQueryPort publication) {
        return new GetIdentitySecurityManifestPublicationStatusService(publication);
    }
}
