package com.product.domain.service;

import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.valueobject.VariantCombination;

import java.util.List;

public interface VariationCombinationManager {
    record VariantCombinationResult (
            VariantCombination variantCombination,
            ProductVariant matchedVariant,
            MatchedType matchedType
    ){
        public enum MatchedType {
            NEW,
            EXTENDED,
            UNCHANGED
        }
    }

    List<VariantCombinationResult> syncCombinations(List<ProductVariant> existingVariants, List<VariantCombination> combinations);
}
