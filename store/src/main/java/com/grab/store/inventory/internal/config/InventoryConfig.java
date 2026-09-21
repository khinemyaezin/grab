package com.grab.store.inventory.internal.config;

import com.inventory.adapter.persistence.config.InventoryPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({InventoryUseCaseConfig.class, InventoryPersistenceConfig.class, InventoryApplicationConfig.class})
public class InventoryConfig {
}
