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

import java.time.LocalDateTime;

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
        LocalDateTime oldest = LocalDateTime.of(2026, 10, 1, 0, 0);
        event("SecurityManifest", OutboxStatus.NEW, oldest);
        event("SecurityManifest", OutboxStatus.FAILED, oldest.plusMinutes(2));
        event("SecurityManifest", OutboxStatus.PUBLISHED, oldest.minusMinutes(5));
        event("Product", OutboxStatus.NEW, oldest.minusMinutes(10));
        entityManager.flush();
        entityManager.clear();

        PublicationStatus result = adapter.status("catalog");

        assertThat(result.pendingCount()).isEqualTo(2);
        assertThat(result.oldestPendingAt()).isEqualTo(oldest.toString());
    }

    private void event(String aggregateType, OutboxStatus status, LocalDateTime occurredAt) {
        CatalogOutboxEvent event = new CatalogOutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId("catalog");
        event.setEventType("TestEvent");
        event.setEventVersion(1);
        event.setHeaders("{}");
        event.setPayload("{}");
        event.setStatus(status);
        event.setOccurredAt(occurredAt);
        event.setAvailableAt(occurredAt);
        entityManager.persist(event);
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {CatalogOutboxEvent.class, CatalogSecurityManifestPublicationEntity.class})
    static class TestConfig {
    }
}
