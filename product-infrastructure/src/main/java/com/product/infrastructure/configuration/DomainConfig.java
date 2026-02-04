package com.product.infrastructure.configuration;

import com.grab.framework.id.IdGenerator;
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
    public VariantKeyGenerator variantKeyGenerator() {
        return new DefaultVariantKeyGenerator();
    }
    @Bean
    public VariantInputsFactory variantInputsFactory(VariantKeyGenerator variantKeyGenerator) {
        return new DefaultVariantInputsFactory(variantKeyGenerator);
    }
    @Bean
    public VariantSorter variantSorter(VariantKeyGenerator variantKeyGenerator) {
        return new DefaultVariantSorter(variantKeyGenerator);
    }
    @Bean
    public ProductVariantSynchronizer productVariantSynchronizer(VariantCombinationService variantCombination, IdGenerator idGenerator,
                                                                 SkuGenerator skuGenerator, VariantDeletionStrategy deletionStrategy,
                                                                 VariantInputsFactory variantInputsFactory, VariantSorter variantSorter,
                                                                 VariantKeyGenerator variantKeyGenerator) {
        return new DefaultProductVariantSynchronizer(
                idGenerator,
                skuGenerator,
                variantCombination,
                deletionStrategy,
                variantInputsFactory,
                variantSorter,
                variantKeyGenerator
        );
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
