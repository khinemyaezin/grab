package com.grab.app.shared.outbox;

import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxObserver;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class OutboxObservabilityConfig {

    @Bean
    public OutboxObserver outboxObserver(MeterRegistry meterRegistry) {
        return new MicrometerOutboxObserver(meterRegistry);
    }

    @Bean
    public OutboxBacklogMetrics outboxBacklogMetrics(
            List<AbstractOutboxProcessor<?, ?>> processors,
            @Value("${outbox.metrics.backlog-cache-ttl-ms:15000}") long cacheTtlMs
    ) {
        Duration cacheTtl = Duration.ofMillis(cacheTtlMs);
        return new OutboxBacklogMetrics(processors, cacheTtl, Clock.systemUTC());
    }

    @Bean
    public OutboxReplayer outboxReplayer(List<AbstractOutboxProcessor<?, ?>> processors) {
        return new OutboxReplayer(processors);
    }

    @Bean
    public OutboxClaimStrategyVerifier outboxClaimStrategyVerifier(
            ListableBeanFactory beanFactory,
            @Value("${outbox.claim-strategy-check:fail}") String mode
    ) {
        OutboxClaimStrategyVerifier.Mode parsedMode = OutboxClaimStrategyVerifier.Mode.parse(mode);
        return new OutboxClaimStrategyVerifier(beanFactory, parsedMode);
    }

    @Bean
    static ObservabilityBeanPostProcessor observabilityBeanPostProcessor(ObjectProvider<MeterRegistry> meterRegistry) {
        return new ObservabilityBeanPostProcessor(meterRegistry);
    }
}
