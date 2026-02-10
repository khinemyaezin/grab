package com.product.infrastructure.configuration;

import com.product.domain.service.*;
import com.product.domain.service.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public VariantCombinationService variantCombination() {
        return new DefaultVariantCombinationService();
    }

    @Bean
    public VariantDeletionStrategy variantDeletionStrategy() {
        return new FullOptionHardDeleteStrategy();
    }

    @Bean
    public VariationKeyGenerator variationKeyGenerator() {
        return new DefaultVariationKeyGenerator();
    }

    @Bean
    public VariationCombinationManager variantCombinationManager(VariationKeyGenerator variationKeyGenerator){
        return new DefaultVariationCombinationManager(variationKeyGenerator);
    }
}
