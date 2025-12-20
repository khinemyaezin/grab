package com.product.domain.service;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.VariantType;

import java.util.List;
import java.util.Set;

/**
 * Strategy interface for handling variant deletion during product synchronization.
 * Follows Open/Closed Principle - extend behavior without modifying ProductFactoryImpl.
 */
public interface VariantDeletionStrategy {

    /**
     * Filters the desired variant types based on deletion rules.
     * Called before generating combinations to exclude option values that should be hard deleted.
     *
     * @param product            the product being synchronized
     * @param desiredVariantTypes the original desired variant types
     * @return filtered variant types (may exclude certain options based on strategy)
     */
    List<VariantType> filterVariantTypes(Product product, List<VariantType> desiredVariantTypes);

    /**
     * Removes obsolete variants from the product after synchronization.
     *
     * @param product    the product being synchronized
     * @param keysToKeep set of variant IDs that should be retained
     */
    void removeObsoleteVariants(Product product, Set<Id> keysToKeep);
}
