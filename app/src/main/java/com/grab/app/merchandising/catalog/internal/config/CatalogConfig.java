package com.grab.app.merchandising.catalog.internal.config;

import com.catalog.adapter.persistence.config.CatalogPersistenceConfig;
import com.grab.storage.adapter.s3.StorageInfraConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        ProductUseCaseConfig.class,
        CatalogPersistenceConfig.class,
        StorageInfraConfig.class,
        CatalogModuleDataSourceConfig.class,
        CatalogModuleOutboxConfig.class
})
public class CatalogConfig {
}

