package com.grab.store.catalog.internal.config;

import com.catalog.adapter.persistence.config.CatalogPersistenceConfig;
import com.grab.storage.infrastructure.StorageInfraConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CatalogUseCaseConfig.class, CatalogPersistenceConfig.class, StorageInfraConfig.class})
public class CatalogConfig {
}