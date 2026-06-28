package com.identity.infrastructure.configuration;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.identity.domain.repository.UserRepository;
import com.identity.domain.repository.AccessAssignmentRepository;
import com.identity.domain.repository.AccessInvitationRepository;
import com.identity.domain.repository.AuthorityRepository;
import com.identity.domain.repository.PlatformRepository;
import com.identity.domain.repository.RoleDelegationRuleRepository;
import com.identity.domain.repository.SessionStore;
import com.identity.infrastructure.repository.adapter.JpaSessionStoreAdapter;
import com.identity.infrastructure.mapper.jpa.RoleJpaAssembler;
import com.identity.infrastructure.mapper.jpa.UserJpaAssembler;
import com.identity.infrastructure.mapper.jpa.AccessAssignmentJpaAssembler;
import com.identity.infrastructure.mapper.jpa.AccessInvitationJpaAssembler;
import com.identity.infrastructure.mapper.jpa.impl.AccessAssignmentJpaAssemblerImpl;
import com.identity.infrastructure.mapper.jpa.impl.AccessInvitationJpaAssemblerImpl;
import com.identity.infrastructure.outbox.IdentityOutboxEvent;
import com.identity.infrastructure.outbox.IdentityOutboxEventProcessor;
import com.identity.infrastructure.outbox.IdentityOutboxEventProducer;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import com.identity.infrastructure.repository.jpa.AccessAssignmentJpaRepository;
import com.identity.infrastructure.repository.jpa.AccessInvitationJpaRepository;
import com.identity.infrastructure.repository.jpa.AuthorityJpaRepository;
import com.identity.infrastructure.repository.jpa.PlatformJpaRepository;
import com.identity.infrastructure.repository.jpa.PlatformRoleJpaRepository;
import com.identity.infrastructure.repository.jpa.RefreshSessionJpaRepository;
import com.identity.infrastructure.repository.jpa.RoleDelegationRuleJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import com.identity.infrastructure.repository.jpa.impl.DefaultAuthorityRepository;
import com.identity.infrastructure.repository.jpa.impl.DefaultAccessAssignmentRepository;
import com.identity.infrastructure.repository.jpa.impl.DefaultAccessInvitationRepository;
import com.identity.infrastructure.repository.jpa.impl.DefaultPlatformRepository;
import com.identity.infrastructure.repository.jpa.impl.DefaultRoleRepository;
import com.identity.infrastructure.repository.jpa.impl.DefaultRoleDelegationRuleRepository;
import com.identity.infrastructure.repository.jpa.impl.DefaultUserRepository;
import com.identity.infrastructure.repository.jpa.impl.IdentityPersistenceExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
@Import(IdentityDomainConfig.class)
public class IdentityInfraConfig {

    @Bean("identityOutboxEventSerializer")
    public OutboxEventSerializer identityOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("identityOutboxEventDispatcher")
    public OutboxEventDispatcher identityOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("identityDomainEventProducer")
    public DomainEventProducer identityDomainEventProducer(
            @Qualifier("identityOutboxStore") OutboxStore<IdentityOutboxEvent, Long> outboxStore,
            @Qualifier("identityOutboxEventSerializer") OutboxEventSerializer serializer
    ) {
        return new IdentityOutboxEventProducer(outboxStore, serializer);
    }

    @Bean("identityOutboxStore")
    public OutboxStore<IdentityOutboxEvent, Long> identityOutboxStore(JpaContext context) {
        return new JpaOutboxStore<>(
                context.getEntityManagerByManagedType(IdentityOutboxEvent.class),
                IdentityOutboxEvent.class
        );
    }

    @Bean("identityPersistenceExecutor")
    public PersistenceExecutor identityPersistenceExecutor() {
        return new IdentityPersistenceExecutor();
    }

    @Bean
    public IdentityOutboxEventProcessor identityOutboxEventProcessor(
            @Qualifier("identityOutboxStore") OutboxStore<IdentityOutboxEvent, Long> outboxStore,
            @Qualifier("identityOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("identityOutboxEventDispatcher") OutboxEventDispatcher dispatcher,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${identity.outbox.batch-size:20}") int batchSize,
            @Value("${identity.outbox.retry-delay-ms:30000}") long retryDelayMs,
            @Value("${identity.outbox.claim-timeout-ms:120000}") long claimTimeoutMs,
            @Value("${identity.outbox.retention-ms:604800000}") long retentionMs
    ) {
        return new IdentityOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelayMs),
                Duration.ofMillis(claimTimeoutMs),
                Duration.ofMillis(retentionMs)
        );
    }

    @Bean
    public UserRepository userRepository(
            UserJpaRepository jpaRepository,
            UserJpaAssembler mapper,
            @Qualifier("identityDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultUserRepository(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public DefaultRoleRepository roleRepository(
            RoleJpaRepository jpaRepository,
            RoleJpaAssembler mapper,
            @Qualifier("identityDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor) {
        return new DefaultRoleRepository(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public AuthorityRepository authorityRepository(AuthorityJpaRepository jpaRepository) {
        return new DefaultAuthorityRepository(jpaRepository);
    }

    @Bean
    public RoleDelegationRuleRepository roleDelegationRuleRepository(
            RoleDelegationRuleJpaRepository rules,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultRoleDelegationRuleRepository(rules, executor);
    }

    @Bean
    public AccessAssignmentJpaAssembler accessAssignmentJpaAssembler(
            UserJpaRepository users,
            PlatformRoleJpaRepository platformRoles,
            IdMapper ids
    ) {
        return new AccessAssignmentJpaAssemblerImpl(users, platformRoles, ids);
    }

    @Bean
    public AccessInvitationJpaAssembler accessInvitationJpaAssembler(
            PlatformRoleJpaRepository platformRoles,
            IdMapper ids
    ) {
        return new AccessInvitationJpaAssemblerImpl(platformRoles, ids);
    }

    @Bean
    public PlatformRepository platformRepository(
            PlatformJpaRepository platforms,
            IdMapper ids,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultPlatformRepository(platforms, ids, executor);
    }

    @Bean
    public AccessAssignmentRepository accessAssignmentRepository(
            AccessAssignmentJpaRepository assignments,
            AccessAssignmentJpaAssembler assembler,
            @Qualifier("identityDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultAccessAssignmentRepository(assignments, assembler, domainEventProducer, executor);
    }

    @Bean
    public AccessInvitationRepository accessInvitationRepository(
            AccessInvitationJpaRepository invitations,
            AccessInvitationJpaAssembler assembler,
            @Qualifier("identityDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new DefaultAccessInvitationRepository(invitations, assembler, domainEventProducer, executor);
    }

    @Bean
    public SessionStore refreshSessionStore(RefreshSessionJpaRepository sessionRepository, UserJpaRepository userRepository) {
        return new JpaSessionStoreAdapter(sessionRepository, userRepository);
    }
}
