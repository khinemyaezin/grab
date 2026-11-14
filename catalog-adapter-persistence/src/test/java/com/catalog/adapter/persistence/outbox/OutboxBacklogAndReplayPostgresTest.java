package com.catalog.adapter.persistence.outbox;

import com.catalog.adapter.persistence.repository.config.PostgreSqlTestContainer;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxStore;
import com.grab.outbox.infrastructure.core.port.in.OutboxBacklog;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = OutboxBacklogAndReplayPostgresTest.TestConfig.class)
class OutboxBacklogAndReplayPostgresTest extends PostgreSqlTestContainer {

    private static final Instant T0 = Instant.parse("2026-11-14T10:00:00Z");
    private static final List<OutboxStatus> PENDING =
            List.of(OutboxStatus.NEW, OutboxStatus.FAILED, OutboxStatus.PROCESSING);

    @PersistenceContext
    private EntityManager entityManager;
    private JpaOutboxStore<CatalogOutboxEvent, Long> store;

    @BeforeEach
    void setUp() {
        store = new JpaOutboxStore<>(entityManager, CatalogOutboxEvent.class);
        entityManager.createQuery("delete from CatalogOutboxEvent").executeUpdate();
    }

    @Test
    void backlog_countsUndeliveredRowsAndReportsOldest() {
        CatalogOutboxEvent oldest = event(T0);
        CatalogOutboxEvent processing = event(T0.plusSeconds(10));
        CatalogOutboxEvent published = event(T0.minusSeconds(60));
        CatalogOutboxEvent dead = event(T0.minusSeconds(120));
        store.saveAll(List.of(oldest, processing, published, dead));
        processing.markProcessing(T0.plusSeconds(11), "token");
        published.markPublished(T0);
        dead.markDead(T0, "boom");
        entityManager.flush();

        OutboxBacklog backlog = store.backlog(PENDING);

        assertThat(backlog.pending()).isEqualTo(2);
        assertThat(backlog.oldestOccurredAt()).isEqualTo(T0);
        assertThat(store.backlog(List.of(OutboxStatus.FAILED))).isEqualTo(new OutboxBacklog(0, null));
    }

    @Test
    void findDeadForReplay_onlyDeadRowsInRange_andRequeueResetsState() {
        CatalogOutboxEvent first = event(T0);
        CatalogOutboxEvent second = event(T0);
        CatalogOutboxEvent outOfRange = event(T0);
        CatalogOutboxEvent notDead = event(T0);
        store.saveAll(List.of(first, second, outOfRange, notDead));
        for (CatalogOutboxEvent event : List.of(first, second, outOfRange)) {
            event.markProcessing(T0, "token");
            event.markDead(T0, "boom");
        }
        entityManager.flush();
        entityManager.clear();

        List<CatalogOutboxEvent> replayable = store.findDeadForReplay(first.getId(), notDead.getId() - 2, 10);

        assertThat(replayable).extracting(CatalogOutboxEvent::getId).containsExactly(first.getId(), second.getId());
        Instant replayAt = T0.plusSeconds(300);
        replayable.forEach(event -> event.requeue(replayAt));
        entityManager.flush();
        entityManager.clear();

        CatalogOutboxEvent requeued = entityManager.find(CatalogOutboxEvent.class, first.getId());
        assertThat(requeued.getStatus()).isEqualTo(OutboxStatus.NEW);
        assertThat(requeued.getAttemptCount()).isZero();
        assertThat(requeued.getAvailableAt()).isEqualTo(replayAt);
        assertThat(requeued.getLastError()).isNull();
        assertThat(store.findDeadForReplay(first.getId(), notDead.getId(), 10))
                .extracting(CatalogOutboxEvent::getId).containsExactly(outOfRange.getId());
    }

    @Test
    void requeue_rejectsRowsThatAreNotDead() {
        CatalogOutboxEvent event = event(T0);

        assertThatThrownBy(() -> event.requeue(T0)).isInstanceOf(IllegalStateException.class);
    }

    private static CatalogOutboxEvent event(Instant occurredAt) {
        SerializedEvent serialized = new SerializedEvent("com.example.Happened", "{}", 1, "{}");
        return CatalogOutboxEvent.pending(UUID.randomUUID(), "Product", "p-1", serialized, occurredAt);
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = CatalogOutboxEvent.class)
    static class TestConfig {
    }
}
