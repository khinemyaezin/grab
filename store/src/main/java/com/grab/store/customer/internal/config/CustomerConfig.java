package com.grab.store.customer.internal.config;

import com.customer.adapter.persistence.config.CustomerPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@CustomerEnabled
@Import({CustomerModuleDataSourceConfig.class, CustomerUseCaseConfig.class, CustomerPersistenceConfig.class})
public class CustomerConfig {
}
