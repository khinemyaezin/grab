package com.product.infrastructure.configuration;

import com.grab.framework.id.IdGenerator;
import com.product.domain.service.*;
import com.product.domain.service.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public VariantCombination variantCombination() {
        return new VariantCombinationServiceImpl();
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
    public ProductVariantSynchronizer productVariantSynchronizer(VariantCombination variantCombination, IdGenerator idGenerator,
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
}
