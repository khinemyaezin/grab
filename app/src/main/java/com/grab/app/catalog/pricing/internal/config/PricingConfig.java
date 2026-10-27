package com.grab.app.catalog.pricing.internal.config;

import com.pricing.adapter.persistence.config.PricingPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PricingUseCaseConfig.class, PricingPersistenceConfig.class})
public class PricingConfig {
}
