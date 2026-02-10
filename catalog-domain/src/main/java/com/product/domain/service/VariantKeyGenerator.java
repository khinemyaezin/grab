package com.product.domain.service;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.valueobject.ProductVariation;
import com.product.domain.aggregate.product.VariantOption;

import java.util.List;
import java.util.Set;

/**
 * Generates keys for variant matching during synchronization.
 * Follows Single Responsibility Principle - only handles key generation logic.
 */
public interface VariantKeyGenerator {

    /**
     * Generates a key for a combination of variant options.
     *
     * @param combination list of variant options
     * @param typeOrder   ordered list of variant type IDs
     * @return the generated key
     */
    String generateCombinationKey(List<VariantOption> combination, List<Id> typeOrder);

    /**
     * Generates a key for an existing product variant.
     *
     * @param variant   the product variant
     * @param typeOrder ordered list of variant type IDs
     * @return the generated key
     */
    String generateVariantKey(ProductVariant variant, List<Id> typeOrder);

    /**
     * Generates a key for variations, optionally ignoring certain types.
     *
     * @param typeOrder    ordered list of variant type IDs
     * @param variations   the product variations
     * @param ignoredTypes set of type IDs to ignore when generating the key
     * @return the generated key
     */
    String generateVariationKey(List<Id> typeOrder, Iterable<ProductVariation> variations, Set<Id> ignoredTypes);
}