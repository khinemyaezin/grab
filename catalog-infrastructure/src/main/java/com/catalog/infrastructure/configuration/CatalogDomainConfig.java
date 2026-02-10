package com.catalog.infrastructure.configuration;

import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.service.VariantDeletionStrategy;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.service.VariationKeyGenerator;
import com.catalog.domain.service.impl.DefaultVariantCombinationService;
import com.catalog.domain.service.impl.DefaultVariationCombinationManager;
import com.catalog.domain.service.impl.DefaultVariationKeyGenerator;
import com.catalog.domain.service.impl.FullOptionHardDeleteStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogDomainConfig {

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
