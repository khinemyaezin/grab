package com.grab.store.merchant.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.domain.Event;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.SecurityManifestEnvelope;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.ScopeDeclaration;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationAdapter;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationStateProvider;
import com.merchant.adapter.persistence.entity.MerchantSecurityManifestPublicationEntity;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEvent;
import com.merchant.adapter.persistence.outbox.MerchantOutboxEventProducer;
import com.merchant.application.model.write.PublishMerchantSecurityManifestCommand;
import com.merchant.application.port.inbound.PublishMerchantSecurityManifestUseCase;
import com.grab.store.merchant.internal.command.handler.PublishMerchantSecurityManifestCommandHandler;
import com.grab.store.shared.events.merchant.MerchantSecurityManifestDeclaredIntegrationEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(SecurityManifestPublicationConcurrencyIntegrationTest.Config.class)
@Testcontainers(disabledWithoutDocker = true)
class SecurityManifestPublicationConcurrencyIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> DATABASE = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private PublicationTestControl control;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    @Qualifier("merchantTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Autowired
    @Qualifier("merchantDataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("merchantEntityManagerFactory")
    private EntityManagerFactory entityManagerFactory;

    private JdbcTemplate jdbc;

    @BeforeEach
    void reset() {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("TRUNCATE merchant_outbox_events, security_manifest_publication RESTART IDENTITY");
        control.reset();
    }

    @Test
    void publish_concurrentFirstUse_createsOneStateAndOneOutboxEvent() throws Exception {
        SecurityManifest manifest = manifest(2, "current");
        control.creationBarrier = new CyclicBarrier(2);

        List<DispatchResult> results = dispatchConcurrently(manifest, manifest);

        assertThat(results).extracting(DispatchResult::result)
                .containsExactlyInAnyOrder(SecurityManifestPublicationPort.PublicationResult.ENQUEUED,
                        SecurityManifestPublicationPort.PublicationResult.NOT_DUE);
        Long stateCount = jdbc.queryForObject("SELECT count(*) FROM security_manifest_publication", Long.class);
        Long eventCount = jdbc.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        Long version = jdbc.queryForObject("SELECT version FROM security_manifest_publication", Long.class);
        assertThat(stateCount).isEqualTo(1L);
        assertThat(eventCount).isEqualTo(1L);
        assertThat(version).isEqualTo(1L);
        assertThat(results).filteredOn(result -> result.result() == SecurityManifestPublicationPort.PublicationResult.NOT_DUE)
                .singleElement().satisfies(this::assertRetriedWithFreshEntityManager);
    }

    @Test
    void publish_concurrentUpdate_retriesWithFreshEntityManagerAndEnqueuesOnce() throws Exception {
        SecurityManifest baseline = manifest(1, "baseline");
        seedExpiredState(baseline);
        SecurityManifest candidate = manifest(2, "current");
        control.eventFactoryBarrier = new CyclicBarrier(2);

        List<DispatchResult> results = dispatchConcurrently(candidate, candidate);

        assertThat(results).extracting(DispatchResult::result)
                .containsExactlyInAnyOrder(SecurityManifestPublicationPort.PublicationResult.ENQUEUED,
                        SecurityManifestPublicationPort.PublicationResult.NOT_DUE);
        Integer revision = jdbc.queryForObject("SELECT security_revision FROM security_manifest_publication", Integer.class);
        Long version = jdbc.queryForObject("SELECT version FROM security_manifest_publication", Long.class);
        Long eventCount = jdbc.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        assertThat(revision).isEqualTo(2);
        assertThat(version).isEqualTo(2L);
        assertThat(eventCount).isEqualTo(1L);
        assertThat(results).filteredOn(result -> result.result() == SecurityManifestPublicationPort.PublicationResult.NOT_DUE)
                .singleElement().satisfies(this::assertRetriedWithFreshEntityManager);
    }

    @Test
    void publish_olderConcurrentCandidate_retriesToSuperseded() throws Exception {
        SecurityManifest baseline = manifest(1, "baseline");
        seedExpiredState(baseline);
        SecurityManifest older = manifest(2, "older-loser");
        SecurityManifest newer = manifest(3, "newer-winner");

        List<DispatchResult> results = dispatchOrderedByDigest(older, newer);

        assertThat(results).extracting(DispatchResult::result)
                .containsExactlyInAnyOrder(SecurityManifestPublicationPort.PublicationResult.SUPERSEDED,
                        SecurityManifestPublicationPort.PublicationResult.ENQUEUED);
        Integer revision = jdbc.queryForObject("SELECT security_revision FROM security_manifest_publication", Integer.class);
        Long eventCount = jdbc.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        assertThat(revision).isEqualTo(3);
        assertThat(eventCount).isEqualTo(1L);
        assertThat(results).filteredOn(result -> result.result() == SecurityManifestPublicationPort.PublicationResult.SUPERSEDED)
                .singleElement().satisfies(this::assertRetriedWithFreshEntityManager);
    }

    @Test
    void publish_conflictingConcurrentCandidate_retriesToConflict() throws Exception {
        SecurityManifest baseline = manifest(1, "baseline");
        seedExpiredState(baseline);
        SecurityManifest accepted = manifest(2, "accepted");
        SecurityManifest conflicting = manifest(2, "conflicting");

        List<DispatchResult> results = dispatchOrderedByDigest(conflicting, accepted);

        assertThat(results).extracting(DispatchResult::result)
                .containsExactlyInAnyOrder(SecurityManifestPublicationPort.PublicationResult.CONFLICT,
                        SecurityManifestPublicationPort.PublicationResult.ENQUEUED);
        Long eventCount = jdbc.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        assertThat(eventCount).isEqualTo(1L);
        assertThat(results).filteredOn(result -> result.result() == SecurityManifestPublicationPort.PublicationResult.CONFLICT)
                .singleElement().satisfies(this::assertRetriedWithFreshEntityManager);
    }

    @Test
    void publish_failureAfterOutboxFlush_rollsBackFirstStateAndEvent() {
        control.failAfterEnqueue.set(true);
        SecurityManifest manifest = manifest(2, "current");

        assertThatThrownBy(() -> dispatch(manifest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("injected publication failure");

        Long stateCount = jdbc.queryForObject("SELECT count(*) FROM security_manifest_publication", Long.class);
        Long eventCount = jdbc.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        assertThat(stateCount).isZero();
        assertThat(eventCount).isZero();
    }

    @Test
    void publish_integrityFailureExhaustsThreeAttemptsAndPropagates() {
        control.failStateInitialization.set(true);

        SecurityManifest manifest = manifest(2, "current");
        assertThatThrownBy(() -> dispatch(manifest))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("injected state initialization failure");

        int attempts = control.stateFactoryAttempts.get();
        Long stateCount = jdbc.queryForObject("SELECT count(*) FROM security_manifest_publication", Long.class);
        Long eventCount = jdbc.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        assertThat(attempts).isEqualTo(3);
        assertThat(stateCount).isZero();
        assertThat(eventCount).isZero();
    }

    @Test
    void publish_insideAmbientTransaction_usesIndependentHandlerTransaction() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        SecurityManifest manifest = manifest(2, "current");

        transaction.executeWithoutResult(status -> {
            DispatchResult result = dispatch(manifest);
            assertThat(result.result()).isEqualTo(SecurityManifestPublicationPort.PublicationResult.ENQUEUED);
            status.setRollbackOnly();
        });

        Long stateCount = jdbc.queryForObject("SELECT count(*) FROM security_manifest_publication", Long.class);
        Long eventCount = jdbc.queryForObject("SELECT count(*) FROM merchant_outbox_events", Long.class);
        assertThat(stateCount).isEqualTo(1L);
        assertThat(eventCount).isEqualTo(1L);
    }

    private void assertRetriedWithFreshEntityManager(DispatchResult result) {
        List<EntityManager> entityManagers = result.entityManagers();
        assertThat(entityManagers).hasSize(2);
        EntityManager firstAttemptEntityManager = entityManagers.get(0);
        EntityManager retryEntityManager = entityManagers.get(1);
        assertThat(firstAttemptEntityManager).isNotSameAs(retryEntityManager);
    }

    private List<DispatchResult> dispatchConcurrently(SecurityManifest first, SecurityManifest second) throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Callable<DispatchResult> firstTask = worker(ready, first);
            Callable<DispatchResult> secondTask = worker(ready, second);
            Future<DispatchResult> firstWorker = pool.submit(firstTask);
            Future<DispatchResult> secondWorker = pool.submit(secondTask);
            ready.countDown();
            DispatchResult firstResult = firstWorker.get(20, TimeUnit.SECONDS);
            DispatchResult secondResult = secondWorker.get(20, TimeUnit.SECONDS);
            return List.of(firstResult, secondResult);
        }
    }

    private List<DispatchResult> dispatchOrderedByDigest(SecurityManifest blocked, SecurityManifest released) throws Exception {
        String blockedDigest = blocked.contentDigest();
        String releasedDigest = released.contentDigest();
        control.blockDigestUntilCommit(blockedDigest, releasedDigest);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<DispatchResult> blockedWorker = pool.submit(() -> dispatch(blocked));
            boolean blockedBeforeEventFactory = control.blockedEventFactory.await(10, TimeUnit.SECONDS);
            assertThat(blockedBeforeEventFactory).isTrue();
            Future<DispatchResult> releasedWorker = pool.submit(() -> dispatch(released));
            DispatchResult blockedResult = blockedWorker.get(20, TimeUnit.SECONDS);
            DispatchResult releasedResult = releasedWorker.get(20, TimeUnit.SECONDS);
            return List.of(blockedResult, releasedResult);
        } finally {
            control.releaseBlockedEventFactory();
        }
    }

    private Callable<DispatchResult> worker(CountDownLatch ready, SecurityManifest manifest) {
        return () -> {
            ready.await();
            return dispatch(manifest);
        };
    }

    private DispatchResult dispatch(SecurityManifest manifest) {
        control.begin(manifest);
        try {
            commandBus.dispatch(new PublishMerchantSecurityManifestCommand());
            return control.result();
        } finally {
            control.releaseAfterCommit(manifest);
            control.end();
        }
    }

    private void seedExpiredState(SecurityManifest manifest) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            MerchantSecurityManifestPublicationEntity state = new MerchantSecurityManifestPublicationEntity("merchant");
            entityManager.persist(state);
            entityManager.flush();
            Instant publicationTime = Instant.now();
            Instant expired = publicationTime.minusSeconds(600);
            state.recordEnqueued(manifest.securityRevision(), manifest.contentDigest(), expired, expired);
            entityManager.flush();
        });
    }

    private SecurityManifest manifest(int revision, String name) {
        AuthorityDefinition authority = new AuthorityDefinition("MERCHANT_READ", "Read " + name, name);
        List<AuthorityDefinition> authorities = List.of(authority);
        List<ScopeDeclaration> scopes = List.of();
        return new SecurityManifest("merchant", revision, scopes, authorities);
    }

    private record DispatchResult(SecurityManifestPublicationPort.PublicationResult result, List<EntityManager> entityManagers) {
    }

    static class PublicationTestControl {
        private final EntityManagerFactory entityManagerFactory;
        private final ThreadLocal<SecurityManifest> manifest = new ThreadLocal<>();
        private final ThreadLocal<SecurityManifestPublicationPort.PublicationResult> result = new ThreadLocal<>();
        private final ThreadLocal<List<EntityManager>> entityManagers = new ThreadLocal<>();
        private final AtomicInteger stateFactoryAttempts = new AtomicInteger();
        private final AtomicBoolean failAfterEnqueue = new AtomicBoolean();
        private final AtomicBoolean failStateInitialization = new AtomicBoolean();
        private volatile CyclicBarrier creationBarrier;
        private volatile CyclicBarrier eventFactoryBarrier;
        private volatile String blockedDigest;
        private volatile String releasedDigest;
        private volatile CountDownLatch blockedEventFactory;
        private volatile CountDownLatch releaseEventFactory;

        PublicationTestControl(EntityManagerFactory entityManagerFactory) {
            this.entityManagerFactory = entityManagerFactory;
        }

        void reset() {
            creationBarrier = null;
            eventFactoryBarrier = null;
            blockedDigest = null;
            releasedDigest = null;
            blockedEventFactory = null;
            releaseEventFactory = null;
            stateFactoryAttempts.set(0);
            failAfterEnqueue.set(false);
            failStateInitialization.set(false);
        }

        void begin(SecurityManifest publicationManifest) {
            manifest.set(publicationManifest);
            entityManagers.set(new ArrayList<>());
        }

        void end() {
            manifest.remove();
            result.remove();
            entityManagers.remove();
        }

        SecurityManifest manifest() {
            return manifest.get();
        }

        void setResult(SecurityManifestPublicationPort.PublicationResult publicationResult) {
            result.set(publicationResult);
        }

        DispatchResult result() {
            List<EntityManager> currentEntityManagers = entityManagers.get();
            List<EntityManager> observedEntityManagers = List.copyOf(currentEntityManagers);
            SecurityManifestPublicationPort.PublicationResult currentResult = result.get();
            return new DispatchResult(currentResult, observedEntityManagers);
        }

        void observeEntityManager() {
            Object resource = TransactionSynchronizationManager.getResource(entityManagerFactory);
            EntityManager transactionEntityManager = ((EntityManagerHolder) resource).getEntityManager();
            entityManagers.get().add(transactionEntityManager);
        }

        MerchantSecurityManifestPublicationEntity createState(String moduleKey) {
            stateFactoryAttempts.incrementAndGet();
            await(creationBarrier);
            if (failStateInitialization.get()) {
                throw new DataIntegrityViolationException("injected state initialization failure");
            }
            return new MerchantSecurityManifestPublicationEntity(moduleKey);
        }

        void blockDigestUntilCommit(String digestToBlock, String digestToRelease) {
            blockedDigest = digestToBlock;
            releasedDigest = digestToRelease;
            blockedEventFactory = new CountDownLatch(1);
            releaseEventFactory = new CountDownLatch(1);
        }

        void beforeEventFactory(SecurityManifestEnvelope envelope) {
            await(eventFactoryBarrier);
            String suppliedDigest = envelope.suppliedContentDigest();
            if (!suppliedDigest.equals(blockedDigest)) {
                return;
            }
            blockedEventFactory.countDown();
            await(releaseEventFactory);
        }

        void releaseAfterCommit(SecurityManifest publicationManifest) {
            String committedDigest = publicationManifest.contentDigest();
            if (committedDigest.equals(releasedDigest) && releaseEventFactory != null) {
                releaseEventFactory.countDown();
            }
        }

        void releaseBlockedEventFactory() {
            if (releaseEventFactory != null) {
                releaseEventFactory.countDown();
            }
        }

        private void await(CyclicBarrier barrier) {
            if (barrier == null) {
                return;
            }
            try {
                barrier.await(10, TimeUnit.SECONDS);
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }

        private void await(CountDownLatch latch) {
            if (latch == null) {
                return;
            }
            try {
                if (!latch.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("publication concurrency gate timed out");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
        }
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @EnableRetry(proxyTargetClass = true, order = Ordered.HIGHEST_PRECEDENCE)
    static class Config {

        @Bean("merchantDataSource")
        DataSource merchantDataSource() {
            return new DriverManagerDataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
        }

        @Bean("merchantEntityManagerFactory")
        LocalContainerEntityManagerFactoryBean merchantEntityManagerFactory(
                @Qualifier("merchantDataSource") DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPersistenceUnitName("merchant-publication-test");
            factory.setPackagesToScan("com.merchant.adapter.persistence.entity", "com.merchant.adapter.persistence.outbox");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }

        @Bean("merchantTransactionManager")
        PlatformTransactionManager merchantTransactionManager(
                @Qualifier("merchantEntityManagerFactory") EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }

        @Bean
        EntityManager merchantEntityManager(@Qualifier("merchantEntityManagerFactory") EntityManagerFactory factory) {
            return SharedEntityManagerCreator.createSharedEntityManager(factory);
        }

        @Bean
        OutboxStore<MerchantOutboxEvent, Long> merchantOutboxStore(EntityManager entityManager) {
            Class<MerchantOutboxEvent> eventType = MerchantOutboxEvent.class;
            return new JpaOutboxStore<>(entityManager, eventType);
        }

        @Bean
        PublicationTestControl publicationTestControl(
                @Qualifier("merchantEntityManagerFactory") EntityManagerFactory factory) {
            return new PublicationTestControl(factory);
        }

        @Bean("merchantPublication")
        SecurityManifestPublicationPort merchantPublication(
                EntityManager entityManager,
                OutboxStore<MerchantOutboxEvent, Long> outboxStore,
                PublicationTestControl control) {
            JsonOutboxEventSerializer serializer = new JsonOutboxEventSerializer();
            MerchantOutboxEventProducer outbox = new MerchantOutboxEventProducer(outboxStore, serializer);
            var baseProvider = SecurityManifestPublicationStateProvider.optimisticProvider(
                    entityManager, MerchantSecurityManifestPublicationEntity.class, control::createState);
            Function<String, MerchantSecurityManifestPublicationEntity> stateProvider = moduleKey -> {
                control.observeEntityManager();
                return baseProvider.apply(moduleKey);
            };
            Function<SecurityManifestEnvelope, Event> eventFactory = envelope -> {
                control.beforeEventFactory(envelope);
                return new MerchantSecurityManifestDeclaredIntegrationEvent(envelope.manifest(), envelope.eventId(),
                        envelope.suppliedContentDigest(), envelope.publishedAt());
            };
            Clock clock = Clock.systemUTC();
            Duration publicationInterval = Duration.ofMinutes(5);
            return new SecurityManifestPublicationAdapter(stateProvider, outbox, eventFactory, clock, publicationInterval);
        }

        @Bean
        PublishMerchantSecurityManifestUseCase publishMerchantSecurityManifestUseCase(
                @Qualifier("merchantPublication") SecurityManifestPublicationPort publication,
                PublicationTestControl control) {
            return command -> {
                SecurityManifest publicationManifest = control.manifest();
                SecurityManifestPublicationPort.PublicationResult publicationResult = publication.enqueue(publicationManifest);
                control.setResult(publicationResult);
                if (control.failAfterEnqueue.getAndSet(false)) {
                    throw new IllegalStateException("injected publication failure");
                }
            };
        }

        @Bean
        PublishMerchantSecurityManifestCommandHandler publishMerchantSecurityManifestCommandHandler(
                PublishMerchantSecurityManifestUseCase useCase) {
            return new PublishMerchantSecurityManifestCommandHandler(useCase);
        }

        @Bean
        CommandBus commandBus(PublishMerchantSecurityManifestCommandHandler handler) {
            List<CommandHandler<?, ?>> handlers = List.of(handler);
            return new DefaultCommandBus(handlers);
        }
    }
}
