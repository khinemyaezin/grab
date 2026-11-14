package com.grab.app.shared.security.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class PublicationObservabilityConfig {

    @Bean
    static ObservabilityBeanPostProcessor publicationObservabilityBeanPostProcessor(
            ObjectProvider<MeterRegistry> meterRegistry
    ) {
        return new ObservabilityBeanPostProcessor(meterRegistry);
    }
}
