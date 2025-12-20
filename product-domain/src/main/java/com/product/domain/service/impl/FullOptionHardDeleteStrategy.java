package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.*;
import com.product.domain.service.VariantDeletionStrategy;

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
    public List<VariantType> filterVariantTypes(Product product, List<VariantType> desiredVariantTypes) {
        if (product.getVariants().isEmpty() || desiredVariantTypes == null || desiredVariantTypes.isEmpty()) {
            return desiredVariantTypes;
        }

        Set<String> fullyDeletedOptions = findFullyDeletedOptions(product);

        if (fullyDeletedOptions.isEmpty()) {
            return desiredVariantTypes;
        }

        return filterOutDeletedOptions(desiredVariantTypes, fullyDeletedOptions);
    }

    @Override
    public void removeObsoleteVariants(Product product, Set<Id> keysToKeep) {
        product.getVariants().stream()
                .map(ProductVariant::getId)
                .filter(id -> !keysToKeep.contains(id))
                .toList()
                .forEach(product::removeVariant);
    }

    /**
     * Finds option values where ALL variants containing that option are DELETED.
     *
     * @param product the product to analyze
     * @return set of "TypeName:OptionName" keys for fully deleted options
     */
    private Set<String> findFullyDeletedOptions(Product product) {
        // Group variants by each option value they contain
        // Key: "Size:Large", Value: list of variants containing Large
        Map<String, List<ProductVariant>> variantsByOption = new HashMap<>();

        for (ProductVariant variant : product.getVariants()) {
            for (ProductVariation variation : variant.getVariations()) {
                String key = buildOptionKey(variation.getTypeName(), variation.getOptionName());
                variantsByOption.computeIfAbsent(key, k -> new ArrayList<>()).add(variant);
            }
        }

        // Find options where ALL variants are DELETED
        return variantsByOption.entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(ProductVariant::isDeleted))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Filters out option values that are fully deleted from the desired variant types.
     */
    private List<VariantType> filterOutDeletedOptions(
            List<VariantType> desiredVariantTypes,
            Set<String> fullyDeletedOptions) {

        List<VariantType> filtered = new ArrayList<>();

        for (VariantType type : desiredVariantTypes) {
            List<VariantOption> remainingOptions = type.getOptions().stream()
                    .filter(opt -> !fullyDeletedOptions.contains(buildOptionKey(type.getName(), opt.getName())))
                    .toList();

            if (!remainingOptions.isEmpty()) {
                VariantType filteredType = new VariantType(type.getId(), type.getName());
                remainingOptions.forEach(filteredType::addOption);
                filtered.add(filteredType);
            }
            // If all options are deleted, the entire type is excluded
        }

        return filtered;
    }

    private String buildOptionKey(String typeName, String optionName) {
        return typeName + ":" + optionName;
    }

    private record OptionKey(Id typeId, Id optionId){

    }
}
