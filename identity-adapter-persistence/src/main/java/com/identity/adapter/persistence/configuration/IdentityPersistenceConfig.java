package com.identity.adapter.persistence.configuration;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.support.PersistenceExecutor;
import com.grab.outbox.infrastructure.OutboxRelays;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.identity.domain.port.outbound.UserRepository;
import com.identity.domain.port.outbound.AccessAssignmentRepository;
import com.identity.domain.port.outbound.AccessInvitationRepository;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.PlatformRepository;
import com.identity.domain.port.outbound.RoleDelegationRuleRepository;
import com.identity.domain.port.outbound.SessionStore;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.application.port.outbound.IdentityLookupQueryPort;
import com.identity.application.port.outbound.AccessAssignmentQueryPort;
import com.identity.application.port.outbound.RoleQueryPort;
import com.identity.application.port.outbound.UserQueryPort;
import com.identity.adapter.persistence.adapter.*;
import com.identity.adapter.persistence.mapper.jpa.RoleJpaAssembler;
import com.identity.adapter.persistence.mapper.jpa.UserJpaAssembler;
import com.identity.adapter.persistence.mapper.jpa.AccessAssignmentJpaAssembler;
import com.identity.adapter.persistence.mapper.jpa.AccessInvitationJpaAssembler;
import com.identity.adapter.persistence.mapper.jpa.impl.AccessAssignmentJpaAssemblerImpl;
import com.identity.adapter.persistence.mapper.jpa.impl.AccessInvitationJpaAssemblerImpl;
import com.identity.adapter.persistence.mapper.jpa.impl.RoleJpaAssemblerImpl;
import com.identity.adapter.persistence.mapper.jpa.impl.UserJpaAssemblerImpl;
import com.identity.adapter.persistence.mapper.jpa.RoleEntityMapper;
import com.identity.adapter.persistence.mapper.jpa.RoleMapper;
import com.identity.adapter.persistence.mapper.jpa.UserEntityMapper;
import com.identity.adapter.persistence.mapper.jpa.UserMapper;
import com.identity.adapter.persistence.repository.jpa.*;
import com.identity.adapter.persistence.security.BcryptPasswordHasher;
import com.identity.domain.service.PasswordHasher;
import com.identity.adapter.persistence.outbox.IdentityOutboxEvent;
import com.identity.adapter.persistence.outbox.IdentityOutboxEventProcessor;
import com.identity.adapter.persistence.outbox.IdentityOutboxEventProducer;
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
public class IdentityPersistenceConfig {

    @Bean("identityOutboxEventSerializer")
    public OutboxEventSerializer identityOutboxEventSerializer() {
        return new JsonOutboxEventSerializer();
    }

    @Bean("identityOutboxEventDispatcher")
    public OutboxEventDispatcher identityOutboxEventDispatcher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean("identityOutboxRelay")
    public OutboxRelay<Long> identityOutboxRelay(
            @Value("${identity.outbox.hot-queue.enabled:true}") boolean enabled,
            @Value("${identity.outbox.hot-queue.workers:2}") int workers,
            @Value("${identity.outbox.hot-queue.capacity:1000}") int capacity,
            @Value("${identity.outbox.hot-queue.drain-timeout-ms:5000}") long drainTimeoutMs
    ) {
        return OutboxRelays.moduleRelay("identity", enabled, workers, capacity, drainTimeoutMs);
    }

    @Bean("identityDomainEventProducer")
    public DomainEventProducer identityDomainEventProducer(
            @Qualifier("identityOutboxStore") OutboxStore<IdentityOutboxEvent, Long> outboxStore,
            @Qualifier("identityOutboxEventSerializer") OutboxEventSerializer serializer,
            @Qualifier("identityOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new IdentityOutboxEventProducer(outboxStore, serializer, relay);
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
            @Value("${identity.outbox.retention-ms:604800000}") long retentionMs,
            @Qualifier("identityOutboxRelay") OutboxRelay<Long> relay
    ) {
        return new IdentityOutboxEventProcessor(
                outboxStore,
                serializer,
                dispatcher,
                transactionManager,
                batchSize,
                Duration.ofMillis(retryDelayMs),
                Duration.ofMillis(claimTimeoutMs),
                Duration.ofMillis(retentionMs),
                relay
        );
    }

    @Bean
    public UserRepository userRepository(
            UserJpaRepository jpaRepository,
            UserJpaAssembler mapper,
            @Qualifier("identityDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor) {
        return new UserRepositoryAdapter(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public RoleRepository roleRepository(
            RoleJpaRepository jpaRepository,
            RoleJpaAssembler mapper,
            @Qualifier("identityDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor) {
        return new RoleRepositoryAdapter(jpaRepository, mapper, domainEventProducer, executor);
    }

    @Bean
    public UserQueryPort userQueryPort(
            UserJpaRepository userJpaRepository,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new UserQueryAdapter(userJpaRepository, executor);
    }

    @Bean
    public RoleQueryPort roleQueryPort(
            RoleJpaRepository jpaRepository,
            PlatformJpaRepository platformJpaRepository,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new RoleQueryAdapter(jpaRepository, platformJpaRepository, executor);
    }

    @Bean
    public AccessAssignmentQueryPort accessAssignmentQueryPort(
            AccessAssignmentJpaRepository assignments,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new AccessAssignmentQueryAdapter(assignments, executor);
    }

    @Bean
    public AuthorityRepository authorityRepository(AuthorityJpaRepository jpaRepository) {
        return new AuthorityRepositoryAdapter(jpaRepository);
    }

    @Bean
    public RoleDelegationRuleRepository roleDelegationRuleRepository(
            RoleDelegationRuleJpaRepository rules,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new RoleDelegationRuleRepositoryAdapter(rules, executor);
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
            RoleJpaRepository roles,
            AuthorityJpaRepository authorities,
            IdMapper ids,
            IdGenerator idGenerator,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new PlatformRepositoryAdapter(platforms, roles, authorities, ids, idGenerator, executor);
    }

    @Bean
    public AccessAssignmentRepository accessAssignmentRepository(
            AccessAssignmentJpaRepository assignments,
            AccessAssignmentJpaAssembler assembler,
            @Qualifier("identityDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new AccessAssignmentRepositoryAdapter(assignments, assembler, domainEventProducer, executor);
    }

    @Bean
    public AccessInvitationRepository accessInvitationRepository(
            AccessInvitationJpaRepository invitations,
            AccessInvitationJpaAssembler assembler,
            @Qualifier("identityDomainEventProducer") DomainEventProducer domainEventProducer,
            @Qualifier("identityPersistenceExecutor") PersistenceExecutor executor
    ) {
        return new AccessInvitationRepositoryAdapter(invitations, assembler, domainEventProducer, executor);
    }

    @Bean
    public SessionStore refreshSessionStore(RefreshSessionJpaRepository sessionRepository, UserJpaRepository userRepository) {
        return new SessionStoreAdapter(sessionRepository, userRepository);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new BcryptPasswordHasher();
    }

    @Bean
    public UserJpaAssembler userJpaAssembler(UserEntityMapper entityMapper, UserMapper domainMapper) {
        return new UserJpaAssemblerImpl(entityMapper, domainMapper);
    }

    @Bean
    public RoleJpaAssembler roleJpaAssembler(
            RoleEntityMapper entityMapper,
            RoleMapper domainMapper,
            AuthorityJpaRepository authorityRepository) {
        return new RoleJpaAssemblerImpl(entityMapper, domainMapper, authorityRepository);
    }

    @Bean
    public RoleMapper roleMapper(IdMapper ids) {
        return new RoleMapper(ids);
    }

    @Bean
    public IdentityLookupQueryPort identityLookupQueryPort(
            UserJpaRepository users,
            ExternalIdentityJpaRepository externalIdentities,
            ExternalEntitlementMappingJpaRepository entitlementMappings,
            AccessAssignmentJpaRepository accessAssignments) {
        return new IdentityLookupQueryAdapter(
                users, externalIdentities, entitlementMappings, accessAssignments
        );
    }
}
