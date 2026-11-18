package com.grab.service.checkout.cart.internal.config;

import com.cart.adapter.persistence.outbox.CartOutboxEvent;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.outbox.infrastructure.core.port.in.DeliverOutboxEventsUseCase;
import com.grab.outbox.infrastructure.module.OutboxModule;
import com.grab.scheduling.infrastructure.ModuleJobScheduler;
import com.grab.scheduling.infrastructure.OutboxScheduledJobs;
import com.grab.scheduling.infrastructure.ScheduledJob;
import com.grab.scheduling.infrastructure.ShedLockSingletonJobExecutor;
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
public class CartModuleOutboxConfig {

    @Bean("cartSingletonJobExecutor")
    public SingletonJobExecutor cartSingletonJobExecutor(
            @Qualifier("orderDataSource") DataSource dataSource,
            @Value("${cart.flyway.default-schema:cart}") String schema,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        MeterRegistry meterRegistry = meters.getIfAvailable();
        return ShedLockSingletonJobExecutor.fromProperties(
                dataSource, schema, environment, "cart.scheduling", meterRegistry);
    }

    @Bean("cartOutboxModule")
    public OutboxModule<CartOutboxEvent> cartOutboxModule(
            JpaContext context,
            @Qualifier("cartTransactionManager") PlatformTransactionManager transactionManager,
            ApplicationEventPublisher publisher,
            Environment environment,
            ObjectProvider<MeterRegistry> meters
    ) {
        EntityManager entityManager = context.getEntityManagerByManagedType(CartOutboxEvent.class);
        return OutboxModule.builder("cart", CartOutboxEvent.class, CartOutboxEvent::pending)
                .entityManager(entityManager)
                .transactionManager(transactionManager)
                .eventPublisher(publisher)
                .properties(environment)
                .meterRegistry(meters.getIfAvailable())
                .build();
    }

    @Bean("cartDomainEventProducer")
    public DomainEventProducer cartDomainEventProducer(
            @Qualifier("cartOutboxModule") OutboxModule<CartOutboxEvent> outbox
    ) {
        return outbox.producer();
    }

    @Bean("cartJobScheduler")
    public ModuleJobScheduler cartJobScheduler(
            @Qualifier("cartOutboxModule") OutboxModule<CartOutboxEvent> outbox,
            @Qualifier("cartSingletonJobExecutor") SingletonJobExecutor executor,
            Environment environment
    ) {
        DeliverOutboxEventsUseCase<Long> delivery = outbox.delivery();
        List<ScheduledJob> jobs = OutboxScheduledJobs.of("cart", delivery::pollOnce, outbox.cleanupJob(),
                delivery::cleanupPublished, executor, environment);
        return ModuleJobScheduler.fromProperties("cart", jobs, environment);
    }
}
