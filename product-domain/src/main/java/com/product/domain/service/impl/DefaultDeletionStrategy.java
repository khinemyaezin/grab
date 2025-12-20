package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.VariantType;
import com.product.domain.service.VariantDeletionStrategy;

import java.util.List;
import java.util.Set;

/**
 * Default deletion strategy that preserves soft-deleted variants.
 * Variants marked as DELETED remain in the product for history preservation.
 * Only removes variants that are no longer part of the desired combinations.
 */
public class DefaultDeletionStrategy implements VariantDeletionStrategy {

    @Override
    public List<VariantType> filterVariantTypes(Product product, List<VariantType> desiredVariantTypes) {
        // Default behavior: no filtering, keep all desired types as-is
        return desiredVariantTypes;
    }

    @Override
    public void removeObsoleteVariants(Product product, Set<Id> keysToKeep) {
        product.getVariants().stream()
                .map(ProductVariant::getId)
                .filter(id -> !keysToKeep.contains(id))
                .toList()
                .forEach(product::removeVariant);
    }
}
