package com.grab.app.merchandising.catalog.internal.config;

import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.outbox.infrastructure.inbox.ProcessedEventRetention;
import com.grab.outbox.infrastructure.module.OutboxModule;
import com.grab.scheduling.infrastructure.ClusterSingletonTask;
import com.grab.scheduling.infrastructure.CompetingWorkerTask;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.scheduling.infrastructure.ScheduledJob;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Duration;
import java.util.List;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogOutboxModuleTest {

    @Test
    void composesCatalogWithExistingQualifiersAndTypedJobs() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityManagerFactory factory = mock(EntityManagerFactory.class);
        Metamodel metamodel = mock(Metamodel.class);
        @SuppressWarnings("unchecked")
        EntityType<CatalogOutboxEvent> entity = mock(EntityType.class);
        when(entityManager.getMetamodel()).thenReturn(metamodel);
        when(entityManager.getEntityManagerFactory()).thenReturn(factory);
        when(metamodel.entity(CatalogOutboxEvent.class)).thenReturn(entity);
        when(entity.getName()).thenReturn("CatalogOutboxEvent");
        @SuppressWarnings("unchecked")
        TypedQuery<CatalogOutboxEvent> claim = mock(TypedQuery.class, RETURNS_SELF);
        when(claim.getResultList()).thenReturn(List.of());
        when(entityManager.createQuery(anyString(), eq(CatalogOutboxEvent.class))).thenReturn(claim);
        Query cleanup = mock(Query.class, RETURNS_SELF);
        when(cleanup.getResultList()).thenReturn(List.of());
        when(entityManager.createQuery(anyString())).thenReturn(cleanup);
        JpaContext jpa = mock(JpaContext.class);
        when(jpa.getEntityManagerByManagedType(CatalogOutboxEvent.class)).thenReturn(entityManager);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());

        SimpleMeterRegistry meters = new SimpleMeterRegistry();
        new ApplicationContextRunner()
                .withUserConfiguration(CatalogModuleOutboxConfig.class)
                .withPropertyValues("catalog.scheduling.lock-provider=local", "catalog.outbox.hot-queue.enabled=false",
                        "catalog.inbox.cleanup.fixed-delay-ms=1234", "inbox.cleanup.fixed-delay-ms=4321")
                .withBean("catalogDataSource", DataSource.class, () -> mock(DataSource.class))
                .withBean("catalogTransactionManager", PlatformTransactionManager.class, () -> transactions)
                .withBean("catalogProcessedEventRepository", ProcessedEventRepository.class,
                        () -> mock(ProcessedEventRepository.class))
                .withBean(JpaContext.class, () -> jpa)
                .withBean(SimpleMeterRegistry.class, () -> meters)
                .run(context -> {
                    assertThat(context).hasBean("catalogDomainEventProducer");
                    assertThat(context).hasBean("catalogSingletonJobExecutor");
                    assertThat(context).hasBean("catalogProcessedEventRetention");
                    OutboxModule<?> module = context.getBean("catalogOutboxModule", OutboxModule.class);
                    assertThat(context.getBean("catalogDomainEventProducer", DomainEventProducer.class))
                            .isSameAs(module.producer());
                    assertThat(context.getBean("catalogProcessedEventRetention"))
                            .isInstanceOf(ProcessedEventRetention.class);
                    SingletonJobExecutor executor = context.getBean("catalogSingletonJobExecutor",
                            SingletonJobExecutor.class);
                    ModuleJobScheduler scheduler = context.getBean("catalogJobScheduler", ModuleJobScheduler.class);
                    assertThat(scheduler.getPhase()).isGreaterThan(module.getPhase());
                    List<ScheduledJob> jobs = scheduler.jobs();
                    assertThat(jobs).extracting(ScheduledJob::name).containsExactly(
                            "catalog.outbox.poll", "catalog.outbox.cleanup", "catalog.inbox.cleanup");
                    assertThat(jobs.get(0)).isInstanceOf(CompetingWorkerTask.class);
                    assertThat(jobs.subList(1, 3)).allSatisfy(job ->
                            assertThat(((ClusterSingletonTask) job).executor()).isSameAs(executor));
                    assertThat(jobs.get(2).fixedDelay()).isEqualTo(Duration.ofMillis(1234));
                    assertThat(meters.find("outbox.pending").tag("module", "catalog").gauge()).isNotNull();
                    assertThat(meters.find("outbox.oldest_pending_age_seconds").tag("module", "catalog").gauge())
                            .isNotNull();
                    assertThat(meters.find("outbox.hot_queue.dropped").tag("module", "catalog").functionCounter())
                            .isNotNull();
                });
    }
}
