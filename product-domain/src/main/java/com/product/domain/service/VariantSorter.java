package com.product.domain.service;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.VariantOption;

import java.util.List;
import java.util.Map;

/**
 * Sorts product variants based on combination order.
 * Follows Single Responsibility Principle - only handles sorting logic.
 */
public interface VariantSorter {

    /**
     * Builds an order map from combinations for sorting.
     *
     * @param combinations list of variant option combinations
     * @param typeOrder    ordered list of variant type IDs
     * @return map of combination key to order index
     */
    Map<String, Integer> buildCombinationOrderMap(List<List<VariantOption>> combinations, List<Id> typeOrder);

    /**
     * Sorts product variants by the combination order.
     *
     * @param product          the product whose variants to sort
     * @param typeOrder        ordered list of variant type IDs
     * @param combinationOrder map of combination key to order index
     */
    void sortVariantsByCombinationOrder(Product product, List<Id> typeOrder, Map<String, Integer> combinationOrder);
}