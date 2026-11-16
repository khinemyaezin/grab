package com.catalog.adapter.persistence.outbox;

import com.catalog.adapter.persistence.repository.config.PostgreSqlTestContainer;
import com.grab.framework.outbox.OutboxDeliveryContext;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.grab.outbox.infrastructure.inbox.jpa.JpaProcessedEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import org.springframework.test.context.ContextConfiguration;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = ProcessedEventInboxPostgresTest.TestConfig.class)
class ProcessedEventInboxPostgresTest extends PostgreSqlTestContainer {

    private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");

    @PersistenceContext
    private EntityManager entityManager;
    private JpaProcessedEventRepository<CatalogProcessedEvent> inbox;

    @Autowired
    private PlatformTransactionManager manager;
    @Autowired
    private BusinessWriter writer;
    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        inbox = repositoryAt(NOW);
    }

    @Test
    void markProcessed_firstDeliveryTrue_redeliveryFalse() {
        UUID eventId = UUID.randomUUID();

        assertThat(inbox.markProcessed("catalog.workflow-step", eventId)).isTrue();
        entityManager.clear();
        assertThat(inbox.markProcessed("catalog.workflow-step", eventId)).isFalse();
    }

    @Test
    void markProcessed_isScopedPerConsumer() {
        UUID eventId = UUID.randomUUID();

        assertThat(inbox.markProcessed("catalog.workflow-step", eventId)).isTrue();
        assertThat(inbox.markProcessed("catalog.other-consumer", eventId)).isTrue();
    }

    @Test
    void deleteProcessedBefore_removesOnlyExpiredRowsInBoundedBatches() {
        JpaProcessedEventRepository<CatalogProcessedEvent> old = repositoryAt(NOW.minusSeconds(3600));
        old.markProcessed("c", UUID.randomUUID());
        old.markProcessed("c", UUID.randomUUID());
        old.markProcessed("c", UUID.randomUUID());
        UUID fresh = UUID.randomUUID();
        inbox.markProcessed("c", fresh);
        entityManager.clear();

        assertThat(inbox.deleteProcessedBefore(NOW.minusSeconds(60), 2)).isEqualTo(2);
        assertThat(inbox.deleteProcessedBefore(NOW.minusSeconds(60), 2)).isEqualTo(1);
        assertThat(inbox.deleteProcessedBefore(NOW.minusSeconds(60), 2)).isZero();
        entityManager.clear();
        assertThat(inbox.markProcessed("c", fresh)).isFalse();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void listenerReceiptAndTransactionalCommandCommitOrRollBackTogether() {
        JdbcTemplate database = createBusinessTable();
        UUID eventId = UUID.randomUUID();
        TestListener listener = new TestListener(manager, inbox);
        String consumer = "catalog.business-writer";

        assertThatThrownBy(() -> deliver(listener, eventId, consumer, () -> {
            writer.write(eventId);
            throw new IllegalStateException("listener failed after command");
        })).hasMessage("listener failed after command");

        assertThat(receiptCount(database, consumer, eventId)).isZero();
        assertThat(businessCount(database, eventId)).isZero();

        deliver(listener, eventId, consumer, () -> writer.write(eventId));
        deliver(listener, eventId, consumer, () -> {
            throw new AssertionError("duplicate action executed");
        });
        deliver(listener, eventId, "catalog.other-writer", () -> writer.write(eventId));

        assertThat(receiptCount(database, consumer, eventId)).isEqualTo(1);
        assertThat(businessCount(database, eventId)).isEqualTo(2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void listenerCommitsEvenWhenEnclosingTransactionRollsBack() {
        TestListener listener = new TestListener(manager, inbox);
        UUID eventId = UUID.randomUUID();
        JdbcTemplate database = createBusinessTable();
        String consumer = "catalog.independent-writer";

        TransactionTemplate enclosing = new TransactionTemplate(manager);
        enclosing.executeWithoutResult(status -> {
            deliver(listener, eventId, consumer, () -> writer.write(eventId));
            status.setRollbackOnly();
        });

        assertThat(receiptCount(database, consumer, eventId)).isEqualTo(1);
        assertThat(businessCount(database, eventId)).isEqualTo(1);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentDuplicateRollsBackAndIsSkippedOnRetry() throws Exception {
        JdbcTemplate database = createBusinessTable();
        UUID eventId = UUID.randomUUID();
        String consumer = "catalog.concurrent-writer";
        TestListener listener = new TestListener(manager, inbox);
        CountDownLatch firstWrite = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (ExecutorService workers = Executors.newFixedThreadPool(2)) {
            Future<?> first = workers.submit(() -> deliver(listener, eventId, consumer, () -> {
                writer.write(eventId);
                firstWrite.countDown();
                await(release);
            }));
            try {
                await(firstWrite);
                Future<?> second = workers.submit(() ->
                        deliver(listener, eventId, consumer, () -> writer.write(eventId)));
                assertThatThrownBy(() -> second.get(500, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
                release.countDown();
                first.get(10, TimeUnit.SECONDS);
                assertThatThrownBy(() -> second.get(10, TimeUnit.SECONDS))
                        .isInstanceOf(ExecutionException.class)
                        .rootCause()
                        .isInstanceOfSatisfying(SQLException.class, exception ->
                                assertThat(exception.getSQLState()).isEqualTo("23505"));
            } finally {
                release.countDown();
            }
        }

        deliver(listener, eventId, consumer, () -> {
            throw new AssertionError("duplicate action executed on retry");
        });
        assertThat(receiptCount(database, consumer, eventId)).isEqualTo(1);
        assertThat(businessCount(database, eventId)).isEqualTo(1);
    }

    private JdbcTemplate createBusinessTable() {
        JdbcTemplate database = new JdbcTemplate(dataSource);
        TransactionTemplate transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        transaction.executeWithoutResult(status ->
                database.execute("create table if not exists consumer_business_write (operation_id uuid not null)"));
        return database;
    }

    private int businessCount(JdbcTemplate database, UUID eventId) {
        return database.queryForObject("select count(*) from consumer_business_write where operation_id = ?",
                Integer.class, eventId);
    }

    private int receiptCount(JdbcTemplate database, String consumer, UUID eventId) {
        return database.queryForObject("select count(*) from processed_event where consumer = ? and event_id = ?",
                Integer.class, consumer, eventId);
    }

    private void deliver(TestListener listener, UUID eventId, String consumer, Runnable action) {
        OutboxDeliveryContext.run(eventId, () -> listener.receive(consumer, action));
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out awaiting concurrent delivery");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private static class TestListener extends AbstractIntegrationEventListener {
        TestListener(PlatformTransactionManager manager, JpaProcessedEventRepository<CatalogProcessedEvent> inbox) {
            super(manager, inbox);
        }

        void receive(String consumer, Runnable action) {
            processDelivery(consumer, action);
        }
    }

    static class BusinessWriter {
        private final JdbcTemplate database;

        BusinessWriter(DataSource dataSource) {
            database = new JdbcTemplate(dataSource);
        }

        @Transactional
        public void write(UUID operationId) {
            database.update("insert into consumer_business_write (operation_id) values (?)", operationId);
        }
    }

    private JpaProcessedEventRepository<CatalogProcessedEvent> repositoryAt(Instant instant) {
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        return new JpaProcessedEventRepository<>(
                entityManager, CatalogProcessedEvent.class, CatalogProcessedEvent::of, clock);
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = CatalogProcessedEvent.class)
    static class TestConfig {
        @Bean
        BusinessWriter businessWriter(DataSource dataSource) {
            return new BusinessWriter(dataSource);
        }
    }
}
