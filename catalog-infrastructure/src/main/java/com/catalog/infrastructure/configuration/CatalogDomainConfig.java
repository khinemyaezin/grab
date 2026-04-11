package com.catalog.infrastructure.configuration;

import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.VariantDeletionStrategy;
import com.catalog.domain.service.MatrixCombinationSynchronizer;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.impl.*;
import com.catalog.domain.valueobject.ProductVariation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;

@Configuration
public class CatalogDomainConfig {

    @Bean
    public MatrixCombinationService variantCombination() {
        return new DefaultMatrixCombinationService();
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
    public MatrixKeyGenerator variationKeyGenerator(Comparator<ProductVariation> getVariationComparator) {
        return new DefaultMatrixKeyGenerator(getVariationComparator);
    }

    @Bean
    public MatrixCombinationSynchronizer variantCombinationManager(MatrixKeyGenerator matrixKeyGenerator){
        return new DefaultMatrixCombinationSynchronizer(matrixKeyGenerator);
    }
}
