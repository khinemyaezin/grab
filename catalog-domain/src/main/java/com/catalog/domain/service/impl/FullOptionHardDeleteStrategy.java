package com.catalog.domain.service.impl;

import com.catalog.domain.service.dto.ProductVariantSelection;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.grab.framework.id.Id;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.service.VariantDeletionStrategy;
import com.catalog.domain.valueobject.ProductVariation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Deletion strategy that hard deletes variants when ALL variants
 * of a specific option value are marked as DELETED.
 *
 * <p>Example: If a product has Size options [Large, Small] and ALL variants
 * with "Large" are deleted, those variants will be completely removed
 * rather than kept as soft-deleted.</p>
 */
public class FullOptionHardDeleteStrategy implements VariantDeletionStrategy {

    @Override
    public List<VariantTypeSelection> filterVariantTypes(List<ProductVariantSelection> targetVariants, List<VariantTypeSelection> desiredVariantTypes) {
        if (targetVariants.isEmpty() || desiredVariantTypes == null || desiredVariantTypes.isEmpty()) {
            return desiredVariantTypes;
        }

        Set<String> fullyDeletedOptions = findFullyDeletedOptions(targetVariants);

        if (fullyDeletedOptions.isEmpty()) {
            return desiredVariantTypes;
        }

        return filterOutDeletedOptions(desiredVariantTypes, fullyDeletedOptions);
    }

    private Set<String> findFullyDeletedOptions(List<ProductVariantSelection> targetVariants) {
        // Group variants by each option value they contain
        // Key: "Size:Large", Value: list of variants containing Large
        Map<String, List<ProductVariantSelection>> variantsByOption = new HashMap<>();

        for (ProductVariantSelection variant : targetVariants) {
            for (ProductVariation variation : variant.variations()) {
                String key = buildOptionKey(variation.getTypeId(), variation.getOptionId());
                variantsByOption.computeIfAbsent(key, k -> new ArrayList<>()).add(variant);
            }
        }

        return variantsByOption.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .allMatch(v -> Objects.equals(v.status(),
                                ProductVariantStatus.DELETED)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public void removeObsoleteVariants(Product product, Set<Id> keysToKeep) {
        product.getVariants().stream()
                .map(ProductVariant::getId)
                .filter(id -> !keysToKeep.contains(id))
                .toList()
                .forEach(product::removeVariant);
    }

    @Override
    public List<ProductVariant> removeObsoleteVariants(List<ProductVariant> variants, Set<Id> keysToKeep) {
        return variants.stream()
                .filter(variant -> !keysToKeep.contains(variant.getId()))
                .toList();
    }

    private List<VariantTypeSelection> filterOutDeletedOptions(
            List<VariantTypeSelection> desiredVariantTypes,
            Set<String> fullyDeletedOptions) {

        List<VariantTypeSelection> filtered = new ArrayList<>();

        for (VariantTypeSelection type : desiredVariantTypes) {
            List<VariantOptionSelection> remainingOptions = type.options().stream()
                    .filter(opt -> !fullyDeletedOptions.contains(buildOptionKey(type.typeId(), opt.valueId())))
                    .toList();

            if (!remainingOptions.isEmpty()) {
                VariantTypeSelection filteredType = new VariantTypeSelection(type.typeId(), remainingOptions);
                filtered.add(filteredType);
            }
        }

        return filtered;
    }

    private String buildOptionKey(Id typeId, Id optionId) {
        return typeId.getValue() + ":" + optionId.getValue();
    }
}
