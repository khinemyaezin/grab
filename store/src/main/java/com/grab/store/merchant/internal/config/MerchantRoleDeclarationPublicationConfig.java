package com.grab.store.merchant.internal.config;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.role.RoleDeclarationPublicationEnvelope;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;
import com.grab.store.shared.events.merchant.MerchantRoleDeclarationDeclaredIntegrationEvent;
import com.manifest.adapter.persistence.adapter.RoleDeclarationPublicationAdapter;
import com.merchant.adapter.persistence.entity.MerchantRoleDeclarationPublicationEntity;
import com.merchant.application.port.inbound.PublishMerchantRoleDeclarationUseCase;
import com.merchant.application.service.PublishMerchantRoleDeclarationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;

import java.time.Clock;
import java.time.Duration;
import java.util.function.Function;

@Configuration
@MerchantEnabled
public class MerchantRoleDeclarationPublicationConfig {
    @Bean("merchantRoleDeclarationPublicationPort")
    public RoleDeclarationPublicationPort merchantRoleDeclarationPublicationPort(
            JpaContext context,
            @Qualifier("merchantDomainEventProducer") DomainEventProducer outbox,
            @Value("${security.manifest.republish.fixed-delay-ms:300000}") long intervalMs
    ) {
        Clock clock = Clock.systemUTC();
        Duration interval = Duration.ofMillis(intervalMs);
        EntityManager entityManager =
                context.getEntityManagerByManagedType(MerchantRoleDeclarationPublicationEntity.class);
        Function<String, MerchantRoleDeclarationPublicationEntity> stateProvider = publicationKey -> {
            MerchantRoleDeclarationPublicationEntity existing =
                    entityManager.find(MerchantRoleDeclarationPublicationEntity.class, publicationKey,
                            LockModeType.PESSIMISTIC_WRITE);
            if (existing != null) {
                return existing;
            }
            MerchantRoleDeclarationPublicationEntity fresh =
                    new MerchantRoleDeclarationPublicationEntity(publicationKey);
            entityManager.persist(fresh);
            entityManager.flush();
            return fresh;
        };
        Function<RoleDeclarationPublicationEnvelope, Event> eventFactory =
                envelope -> new MerchantRoleDeclarationDeclaredIntegrationEvent(
                        envelope.declaration(),
                        envelope.eventId(),
                        envelope.suppliedContentDigest(),
                        envelope.publishedAt()
                );
        return new RoleDeclarationPublicationAdapter(stateProvider, outbox, eventFactory, clock, interval);
    }

    @Bean
    public PublishMerchantRoleDeclarationUseCase publishMerchantRoleDeclarationUseCase(
            @Qualifier("merchantRoleDeclarationPublicationPort") RoleDeclarationPublicationPort publication
    ) {
        return new PublishMerchantRoleDeclarationService(publication);
    }
}
