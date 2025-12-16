package com.product.infrastructure.configuration;

import com.product.domain.service.VariantCombination;
import com.product.domain.service.impl.VariantCombinationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public VariantCombination variantCombination() {
        return new VariantCombinationServiceImpl();
    }
}
