package com.grab.app.merchandising.internal.config;

import com.grab.app.merchandising.catalog.internal.config.CatalogConfig;
import com.grab.app.merchandising.inventory.internal.config.InventoryConfig;
import com.grab.app.merchandising.pricing.internal.config.PricingConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        MerchandisingDataSourceConfig.class,
        MerchandisingBusConfig.class,
        CatalogConfig.class,
        InventoryConfig.class,
        PricingConfig.class
})
public class MerchandisingConfig {
}

