package com.catalog.domain.service;

import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.VariantCombination;

import java.util.List;

public interface MatrixCombinationSynchronizer {

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

    List<VariantCombinationResult> syncMatrixCombination(List<ProductVariant> existingVariants, List<VariantCombination> combinations);
}
