package com.grab.app.merchandising.catalog.internal.config;

import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.outbox.JsonOutboxEventSerializer;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.module.OutboxModule;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.manifest.adapter.persistence.SecurityManifestEventFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class CatalogOutboxExtractionPostgresTest {

    @Test
    void catalogAloneProducesDeliversCleansAndReplays() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")) {
            postgres.start();
            DataSource dataSource = new DriverManagerDataSource(
                    postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
            IdGenerator ids = new UuidGenerator();
            ReceivedEvents received = new ReceivedEvents();
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            new ApplicationContextRunner()
                    .withUserConfiguration(CatalogModuleDataSourceConfig.class)
                    .withBean("catalogDataSource", DataSource.class, () -> dataSource)
                    .withBean(IdGenerator.class, () -> ids)
                    .withBean(IdMapper.class, () -> new IdMapper(ids))
                    .withBean("catalogSecurityManifestEventFactory", SecurityManifestEventFactory.class,
                            () -> envelope -> new CatalogEvent("manifest"))
                    .withBean(ReceivedEvents.class, () -> received)
                    .withPropertyValues("catalog.jpa.hibernate.hbm2ddl.auto=validate",
                            "catalog.jpa.hibernate.show_sql=false",
                            "catalog.outbox.fixed-delay-ms=50",
                            "catalog.outbox.hot-queue.workers=1",
                            "catalog.outbox.retention-ms=60000",
                            "catalog.outbox.cleanup-fixed-delay-ms=50",
                            "catalog.outbox.cleanup-lock-at-least-for-ms=0")
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        assertThat(context.getBeansOfType(OutboxModule.class)).containsOnlyKeys("catalogOutboxModule");
                        assertThat(context.getBeansOfType(ModuleJobScheduler.class)).containsOnlyKeys("catalogJobScheduler");
                        JpaContext jpaContext = context.getBean(JpaContext.class);
                        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(CatalogOutboxEvent.class);
                        PlatformTransactionManager manager = context.getBean("catalogTransactionManager",
                                PlatformTransactionManager.class);
                        TransactionTemplate tx = new TransactionTemplate(manager);
                        DomainEventProducer producer = context.getBean("catalogDomainEventProducer", DomainEventProducer.class);
                        OutboxModule<?> outbox = context.getBean("catalogOutboxModule", OutboxModule.class);

                        tx.executeWithoutResult(status -> {
                            producer.produce("Catalog", "rolled-back", List.of(new CatalogEvent("rolled-back")));
                            status.setRollbackOnly();
                        });
                        CatalogEvent event = new CatalogEvent("created");
                        tx.executeWithoutResult(status -> producer.produce("Catalog", "created", List.of(event)));
                        assertThat(received.events.poll(5, TimeUnit.SECONDS)).isEqualTo(event);
                        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                            List<String> aggregates = tx.execute(status -> entityManager.createQuery(
                                    "select e.aggregateId from CatalogOutboxEvent e where e.status = :status", String.class)
                                    .setParameter("status", OutboxStatus.PUBLISHED).getResultList());
                            assertThat(aggregates).containsExactly("created");
                        });

                        Instant now = Instant.now();
                        CatalogOutboxEvent expired = row("expired", now.minusSeconds(180));
                        expired.markPublished(now.minusSeconds(120));
                        CatalogOutboxEvent dead = row("replay", now);
                        dead.markDead(now, "manual replay fixture");
                        tx.executeWithoutResult(status -> {
                            entityManager.persist(expired);
                            entityManager.persist(dead);
                        });
                        assertThat(outbox.administration().replayDead(dead.getId(), dead.getId(), 1)).isOne();
                        assertThat(received.events.poll(5, TimeUnit.SECONDS)).isEqualTo(new CatalogEvent("replay"));
                        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                            CatalogOutboxEvent deleted = tx.execute(status ->
                                    entityManager.find(CatalogOutboxEvent.class, expired.getId()));
                            CatalogOutboxEvent published = tx.execute(status ->
                                    entityManager.find(CatalogOutboxEvent.class, dead.getId()));
                            assertThat(deleted).isNull();
                            assertThat(published.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
                        });

                        CatalogOutboxEvent blocking = row("block-until-shutdown", Instant.now());
                        CatalogOutboxEvent queued = row("release-on-shutdown", Instant.now());
                        tx.executeWithoutResult(status -> {
                            entityManager.persist(blocking);
                            entityManager.persist(queued);
                        });
                        assertThat(received.blocked.await(5, TimeUnit.SECONDS)).isTrue();
                        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                                assertThat(status(jdbc, "release-on-shutdown")).isEqualTo("PROCESSING"));
                    });
            assertThat(status(jdbc, "release-on-shutdown")).isEqualTo("FAILED");
            assertThat(received.events).isEmpty();
        }
    }

    private static String status(JdbcTemplate jdbc, String aggregate) {
        return jdbc.queryForObject("select status from catalog.outbox_record where aggregate_id = ?", String.class,
                aggregate);
    }

    private static CatalogOutboxEvent row(String value, Instant occurred) {
        JsonOutboxEventSerializer serializer = new JsonOutboxEventSerializer();
        SerializedEvent serialized = serializer.serialize(new CatalogEvent(value));
        return CatalogOutboxEvent.pending(UUID.randomUUID(), "Catalog", value, serialized, occurred);
    }

    public record CatalogEvent(String value) implements Event {
    }

    static final class ReceivedEvents {
        private final BlockingQueue<CatalogEvent> events = new LinkedBlockingQueue<>();
        private final CountDownLatch blocked = new CountDownLatch(1);

        @EventListener
        public void onEvent(CatalogEvent event) throws InterruptedException {
            if ("block-until-shutdown".equals(event.value())) {
                blocked.countDown();
                new CountDownLatch(1).await(15, TimeUnit.SECONDS);
                return;
            }
            events.add(event);
        }
    }
}
