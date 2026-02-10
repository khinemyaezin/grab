package com.grab.store.catalog.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.util.ProductSKUGenerator;
import com.grab.store.catalog.internal.util.UuidGenerator;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.infrastructure.configuration.CatalogInfraConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CatalogInfraConfig.class)
public class CatalogConfig {
    @Bean
    public IdGenerator idGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public SkuGenerator skuGenerator() {
        return new ProductSKUGenerator();
    }
}