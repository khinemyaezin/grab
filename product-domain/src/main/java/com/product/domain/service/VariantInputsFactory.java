package com.product.domain.service;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.VariantType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory for preparing variant synchronization inputs.
 * Follows Single Responsibility Principle - only handles input preparation logic.
 */
public interface VariantInputsFactory {

    /**
     * Prepares the inputs required for variant synchronization.
     *
     * @param product             the product being synchronized
     * @param desiredVariantTypes the desired variant types
     * @return prepared inputs for synchronization
     */
    VariantInputs prepare(Product product, List<VariantType> desiredVariantTypes);

    /**
     * Immutable container for synchronization inputs.
     */
    record VariantInputs(
            List<Id> variantTypeOrder,
            Set<Id> newTypes,
            Map<String, ProductVariant> existingVariantByKey
    ) {}
}