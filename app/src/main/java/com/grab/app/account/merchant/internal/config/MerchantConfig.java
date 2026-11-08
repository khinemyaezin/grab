package com.grab.app.account.merchant.internal.config;

import com.merchant.adapter.persistence.config.MerchantPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        MerchantUseCaseConfig.class,
        MerchantPersistenceConfig.class,
        MerchantModuleDataSourceConfig.class,
        MerchantModuleOutboxConfig.class
})
public class MerchantConfig {
}

