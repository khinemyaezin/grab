package com.product.infrastructure.configuration;

import com.grab.framework.mapper.CommonMapper;
import com.product.domain.aggregate.product.ProductFactory;
import com.product.domain.aggregate.product.ProductFactoryImpl;
import com.product.domain.aggregate.product.ProductVariantFactory;
import com.product.domain.aggregate.product.ProductVariantFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {
    @Bean
    public ProductFactory getProductFactory() {
        return new ProductFactoryImpl();
    }

    @Bean
    public ProductVariantFactory getProductVariantFactory() {
        return new ProductVariantFactoryImpl();
    }

    @Bean
    public CommonMapper getCommonMapper() {
        return new CommonMapper();
    }
}
