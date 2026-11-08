package com.grab.app.merchandising.pricing.internal.config;

import com.pricing.adapter.persistence.config.PricingPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        PricingUseCaseConfig.class,
        PricingPersistenceConfig.class,
        PricingModuleDataSourceConfig.class,
        PricingModuleOutboxConfig.class
})
public class PricingConfig {
}

