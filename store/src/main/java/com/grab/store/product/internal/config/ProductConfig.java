package com.grab.store.product.internal.config;

import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.util.ProductSKUGenerator;
import com.grab.store.product.internal.util.UuidGenerator;
import com.product.domain.service.SkuGenerator;
import com.product.infrastructure.configuration.ProductInfraConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ProductInfraConfig.class)
public class ProductConfig {
    @Bean
    public IdGenerator idGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public SkuGenerator skuGenerator() {
        return new ProductSKUGenerator();
    }
}