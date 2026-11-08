package com.grab.app.merchandising.inventory.internal.config;

import com.inventory.adapter.persistence.config.InventoryPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        InventoryUseCaseConfig.class,
        InventoryPersistenceConfig.class,
        InventoryModuleDataSourceConfig.class,
        InventoryModuleOutboxConfig.class
})
public class InventoryConfig {
}

