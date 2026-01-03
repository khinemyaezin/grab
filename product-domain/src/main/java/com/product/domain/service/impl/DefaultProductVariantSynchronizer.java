package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.domain.service.*;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DefaultProductVariantSynchronizer implements ProductVariantSynchronizer{
    private final IdGenerator idGenerator;
    private final SkuGenerator skuGenerator;
    private final VariantCombination variantCombination;
    private final VariantDeletionStrategy deletionStrategy;
    private final VariantInputsFactory  variantInputsFactory;
    private final VariantSorter variantSorter;
    private final VariantKeyGenerator variantKeyGenerator;

    public void synchronize(Product product, List<VariantType> desiredTypes) {
        List<VariantType> filteredTypes = deletionStrategy.filterVariantTypes(product, desiredTypes);
        VariantInputsFactory.VariantInputs inputs = variantInputsFactory.prepare(product, filteredTypes);
        List<List<VariantOption>> combinations = variantCombination.generateCombinations(filteredTypes);
        Set<Id> effectedIds = synchronizeVariants(product, inputs, combinations);

        Map<String, Integer> combinationOrder = variantSorter.buildCombinationOrderMap(combinations, inputs.variantTypeOrder());
        variantSorter.sortVariantsByCombinationOrder(product, inputs.variantTypeOrder(), combinationOrder);
        deletionStrategy.removeObsoleteVariants(product, effectedIds);
    }

    private Set<Id> synchronizeVariants(Product product, VariantInputsFactory.VariantInputs inputs,
                                         List<List<VariantOption>> combinations) {
        Map<String, Integer> baseKeyUseCount = new HashMap<>();
        Set<Id> effectedIds = new HashSet<>();

        for (List<VariantOption> combination : combinations) {
            List<ProductVariation> variations = combination.stream()
                    .map(this::toVariation)
                    .toList();
            String key = variantKeyGenerator.generateVariationKey(inputs.variantTypeOrder(),
                    variations, inputs.newTypes());

            ProductVariant match = inputs.existingVariantByKey().get(key);
            int useCount = baseKeyUseCount.getOrDefault(key, 0);

            if (match != null && useCount == 0) {
                ProductVariant updatedVariant = updateExistingVariant(product, match, variations);
                effectedIds.add(updatedVariant.getId());
            } else {
                ProductVariant newVariant = createNewVariant(product, variations, match, useCount);
                effectedIds.add(newVariant.getId());
            }

            baseKeyUseCount.put(key, useCount + 1);
        }

        return effectedIds;
    }

    protected ProductVariation toVariation(VariantOption option) {
        return new ProductVariation(option.getName(),
                option.getId(),
                option.getVariantType().getName(),
                option.getVariantType().getId());
    }

    private ProductVariant createNewVariant(Product product, List<ProductVariation> variations,
                                  ProductVariant match, int useCount) {
        String baseSku = match != null ? match.getSku() : null;
        SkuGenerator.Context ctx = new SkuGenerator.Context(
                product.getId(),
                product.getName(),
                variations,
                baseSku,
                useCount,
                Map.of()
        );
        String sku = skuGenerator.generate(ctx);
        Id newId = idGenerator.generateId();
        ProductVariant newVariant = new ProductVariant(newId, product.getId(), sku, variations);
        product.addVariant(newVariant);
        return newVariant;
    }

    private ProductVariant updateExistingVariant(Product product, ProductVariant match,
                                           List<ProductVariation> variations) {
        ProductVariant updated = new ProductVariant(
                match.getId(), product.getId(), match.getSku(),
                match.getStatus(), variations);
        product.updateVariant(updated);
        return updated;
    }
}
