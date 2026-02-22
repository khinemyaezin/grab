package com.catalog.infrastructure.configuration;

import com.catalog.domain.service.VariantCombinationService;
import com.catalog.domain.service.VariantDeletionStrategy;
import com.catalog.domain.service.VariationCombinationManager;
import com.catalog.domain.service.VariationKeyGenerator;
import com.catalog.domain.service.impl.*;
import com.catalog.domain.valueobject.ProductVariation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;

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
    public Comparator<ProductVariation> getVariationComparator() {
        return new ProductVariationComparator();
    }

    @Bean
    public VariationKeyGenerator variationKeyGenerator(Comparator<ProductVariation> getVariationComparator) {
        return new DefaultVariationKeyGenerator(getVariationComparator);
    }

    @Bean
    public VariationCombinationManager variantCombinationManager(VariationKeyGenerator variationKeyGenerator){
        return new DefaultVariationCombinationManager(variationKeyGenerator);
    }
}
