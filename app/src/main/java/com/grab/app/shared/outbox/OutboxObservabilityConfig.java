package com.grab.app.shared.outbox;

import com.grab.outbox.infrastructure.OutboxObserver;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OutboxObservabilityConfig {

    @Bean
    public OutboxObserver outboxObserver(MeterRegistry meterRegistry) {
        return new MicrometerOutboxObserver(meterRegistry);
    }
}
