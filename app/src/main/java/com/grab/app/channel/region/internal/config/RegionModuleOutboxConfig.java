package com.grab.app.channel.region.internal.config;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;
import com.grab.outbox.infrastructure.module.OutboxModule;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.scheduling.infrastructure.OutboxScheduledJobs;
import com.grab.scheduling.infrastructure.ScheduledJob;
import com.grab.scheduling.infrastructure.ShedLockSingletonJobExecutor;
import com.region.adapter.persistence.outbox.RegionOutboxEvent;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class RegionModuleOutboxConfig {

    @Bean("regionSingletonJobExecutor")
    public SingletonJobExecutor regionSingletonJobExecutor(
            @Qualifier("channelDataSource") DataSource dataSource,
            @Value("${region.flyway.default-schema:region}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "region.scheduling", meterRegistry);
    }

    @Bean("regionOutboxModule")
    public OutboxModule<RegionOutboxEvent> regionOutboxModule(
            JpaContext context,
            @Qualifier("regionTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(RegionOutboxEvent.class);
        return OutboxModule.builder("region", RegionOutboxEvent.class, RegionOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("regionDomainEventProducer")
    public DomainEventProducer regionDomainEventProducer(
            @Qualifier("regionOutboxModule") OutboxModule<RegionOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("regionJobScheduler")
    public ModuleJobScheduler regionJobScheduler(
            @Qualifier("regionOutboxModule") OutboxModule<RegionOutboxEvent> outbox,
            @Qualifier("regionSingletonJobExecutor") SingletonJobExecutor executor,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> jobs = OutboxScheduledJobs.of("region", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        return ModuleJobScheduler.fromProperties("region", jobs, environment);
    }
}
