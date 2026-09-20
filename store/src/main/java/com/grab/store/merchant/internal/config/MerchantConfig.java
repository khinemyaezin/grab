package com.grab.store.merchant.internal.config;

import com.merchant.adapter.persistence.config.MerchantPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@MerchantEnabled
@Import({MerchantUseCaseConfig.class, MerchantPersistenceConfig.class})
public class MerchantConfig {
}
