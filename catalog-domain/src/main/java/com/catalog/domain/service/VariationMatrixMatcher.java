package com.catalog.domain.service;

import com.catalog.domain.valueobject.ProductVariation;

import java.util.List;

public interface VariationMatrixMatcher {

    <T> MatchingResult<T> match(
            List<VariantInput<T>> existingVariants,
            List<List<ProductVariation>> newCombinations
    );

    record VariantInput<T>(
            List<ProductVariation> variations,
            T payload
    ) {}

    record MatchingResult<T>(
            List<VariantMatch<T>> matches,
            List<T> collapsed
    ) {}

    record VariantMatch<T>(
            List<ProductVariation> combination,
            MatchType type,
            T matchedPayload
    ) {}

    enum MatchType {
        UNCHANGED,
        EXTENDED,
        NEW
    }
}
