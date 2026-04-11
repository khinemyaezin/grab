package com.catalog.domain.service;

import com.catalog.domain.service.dto.ProductVariantSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;

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
     * @param variants            the product being synchronized
     * @param desiredVariantTypes the original desired variant types
     * @return filtered variant types (may exclude certain options based on strategy)
     */
    List<VariantTypeSelection> filterVariantTypes(List<ProductVariantSelection> variants, List<VariantTypeSelection> desiredVariantTypes);
    /**
     * Removes obsolete variants from the product after synchronization.
     *
     * @param product    the product being synchronized
     * @param keysToKeep set of variant IDs that should be retained
     */
    void removeObsoleteVariants(Product product, Set<Id> keysToKeep);
    List<ProductVariant> removeObsoleteVariants(List<ProductVariant> variants, Set<Id> keysToKeep);
}
