package com.catalog.domain.service;

import com.catalog.domain.service.dto.ProductVariantSelection;
import com.catalog.domain.valueobject.VariantCombination;

import java.util.List;

public interface VariationCombinationManager {

    record VariantCombinationResult (
            VariantCombination variantCombination,
            ProductVariantSelection matchedVariant,
            MatchedType matchedType
    ){
        public enum MatchedType {
            NEW,
            EXTENDED,
            UNCHANGED
        }
    }

    List<VariantCombinationResult> syncCombinations(List<ProductVariantSelection> existingVariants, List<VariantCombination> combinations);
}
