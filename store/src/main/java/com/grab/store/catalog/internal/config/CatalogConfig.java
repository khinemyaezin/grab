package com.grab.store.catalog.internal.config;

import com.grab.store.catalog.internal.util.ProductSKUGenerator;
import com.catalog.domain.service.SkuGenerator;
import com.catalog.infrastructure.configuration.CatalogInfraConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CatalogInfraConfig.class)
public class CatalogConfig {

    @Bean
    public SkuGenerator skuGenerator() {
        return new ProductSKUGenerator();
    }
}