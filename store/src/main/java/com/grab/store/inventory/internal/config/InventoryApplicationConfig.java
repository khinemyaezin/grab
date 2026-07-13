package com.grab.store.inventory.internal.config;

import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import com.grab.store.inventory.internal.policy.impl.DefaultInventoryLocationAccessPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryApplicationConfig {

    @Bean
    public InventoryLocationAccessPolicy inventoryLocationAccessPolicy() {
        return new DefaultInventoryLocationAccessPolicy();
    }
}
