package com.catalog.domain.service;

import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.VariantCombination;

import java.util.List;

public interface MatrixCombinationSynchronizer {

    record VariantCombinationResult (
            VariantCombination variantCombination,

            // multiple existing variants could match a single combination,
            // e.g. when a combination is reduced to a simpler one, it could match multiple existing variants with more variations
            List<ProductVariant> productVariants,
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
