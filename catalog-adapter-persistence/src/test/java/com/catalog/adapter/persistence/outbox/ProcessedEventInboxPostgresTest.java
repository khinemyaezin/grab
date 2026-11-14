package com.catalog.adapter.persistence.outbox;

import com.catalog.adapter.persistence.repository.config.PostgreSqlTestContainer;
import com.grab.outbox.infrastructure.inbox.jpa.JpaProcessedEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = ProcessedEventInboxPostgresTest.TestConfig.class)
class ProcessedEventInboxPostgresTest extends PostgreSqlTestContainer {

    private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");

    @PersistenceContext
    private EntityManager entityManager;
    private JpaProcessedEventRepository<CatalogProcessedEvent> inbox;

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

    private JpaProcessedEventRepository<CatalogProcessedEvent> repositoryAt(Instant instant) {
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        return new JpaProcessedEventRepository<>(
                entityManager, CatalogProcessedEvent.class, CatalogProcessedEvent::of, clock);
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = CatalogProcessedEvent.class)
    static class TestConfig {
    }
}
