package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.entity.CatalogSecurityManifestPublicationEntity;
import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
import com.catalog.adapter.persistence.repository.config.PostgreSqlTestContainer;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.manifest.adapter.persistence.adapter.SecurityManifestPublicationQueryAdapter;
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

import com.grab.framework.outbox.SerializedEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = SecurityManifestBacklogPostgresTest.TestConfig.class)
class SecurityManifestBacklogPostgresTest extends PostgreSqlTestContainer {
    @PersistenceContext
    private EntityManager entityManager;
    private SecurityManifestPublicationQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SecurityManifestPublicationQueryAdapter(
                entityManager, CatalogSecurityManifestPublicationEntity.class, "CatalogOutboxEvent"
        );
    }

    @Test
    void status_emptyOutbox_returnsZeroCountAndAbsentOldestTimestamp() {
        PublicationStatus result = adapter.status("catalog");

        assertThat(result.pendingCount()).isZero();
        assertThat(result.oldestPendingAt()).isNull();
        assertThat(result.enqueuedRevision()).isZero();
    }

    @Test
    void status_mixedOutboxEvents_countsOnlyUnpublishedManifestEvents() {
        Instant oldest = Instant.parse("2026-10-01T00:00:00Z");
        event("SecurityManifest", OutboxStatus.NEW, oldest);
        event("SecurityManifest", OutboxStatus.FAILED, oldest.plus(2, ChronoUnit.MINUTES));
        event("SecurityManifest", OutboxStatus.PUBLISHED, oldest.minus(5, ChronoUnit.MINUTES));
        event("Product", OutboxStatus.NEW, oldest.minus(10, ChronoUnit.MINUTES));
        entityManager.flush();
        entityManager.clear();

        PublicationStatus result = adapter.status("catalog");

        assertThat(result.pendingCount()).isEqualTo(2);
        assertThat(result.oldestPendingAt()).isEqualTo(oldest.toString());
    }

    private void event(String aggregateType, OutboxStatus status, Instant occurredAt) {
        CatalogOutboxEvent event = CatalogOutboxEvent.pending(
                UUID.randomUUID(),
                aggregateType,
                "catalog",
                new SerializedEvent("TestEvent", "{}", 1, "{}"),
                occurredAt
        );
        if (status != OutboxStatus.NEW) {
            event.markProcessing(occurredAt, "claim");
        }
        if (status == OutboxStatus.FAILED) {
            event.markFailed("boom", occurredAt);
        }
        if (status == OutboxStatus.PUBLISHED) {
            event.markPublished(occurredAt);
        }
        entityManager.persist(event);
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {CatalogOutboxEvent.class, CatalogSecurityManifestPublicationEntity.class})
    static class TestConfig {
    }
}
