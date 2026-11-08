package com.grab.app.account.customer.internal.config;

import com.customer.adapter.persistence.config.CustomerPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        CustomerUseCaseConfig.class,
        CustomerPersistenceConfig.class,
        CustomerModuleDataSourceConfig.class,
        CustomerModuleOutboxConfig.class
})
public class CustomerConfig {
}

