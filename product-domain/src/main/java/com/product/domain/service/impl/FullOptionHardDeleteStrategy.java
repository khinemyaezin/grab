package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.*;
import com.product.domain.service.VariantDeletionStrategy;
import com.product.domain.valueobject.ProductVariation;

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
    public List<VariantType> filterVariantTypes(List<ProductVariant> targetVariants, List<VariantType> desiredVariantTypes) {
        if (targetVariants.isEmpty() || desiredVariantTypes == null || desiredVariantTypes.isEmpty()) {
            return desiredVariantTypes;
        }

        Set<String> fullyDeletedOptions = findFullyDeletedOptions(targetVariants);

        if (fullyDeletedOptions.isEmpty()) {
            return desiredVariantTypes;
        }

        return filterOutDeletedOptions(desiredVariantTypes, fullyDeletedOptions);
    }

    private Set<String> findFullyDeletedOptions(List<ProductVariant> targetVariants) {
        // Group variants by each option value they contain
        // Key: "Size:Large", Value: list of variants containing Large
        Map<String, List<ProductVariant>> variantsByOption = new HashMap<>();

        for (ProductVariant variant : targetVariants) {
            for (ProductVariation variation : variant.getVariations()) {
                String key = buildOptionKey(variation.getTypeId(), variation.getOptionId());
                variantsByOption.computeIfAbsent(key, k -> new ArrayList<>()).add(variant);
            }
        }

        return variantsByOption.entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(ProductVariant::isDeleted))
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
                String key = buildOptionKey(variation.getTypeId(), variation.getOptionId());
                variantsByOption.computeIfAbsent(key, k -> new ArrayList<>()).add(variant);
            }
        }

        return variantsByOption.entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(ProductVariant::isDeleted))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private List<VariantType> filterOutDeletedOptions(
            List<VariantType> desiredVariantTypes,
            Set<String> fullyDeletedOptions) {

        List<VariantType> filtered = new ArrayList<>();

        for (VariantType type : desiredVariantTypes) {
            List<VariantOption> remainingOptions = type.getOptions().stream()
                    .filter(opt -> !fullyDeletedOptions.contains(buildOptionKey(type.getId(), opt.getId())))
                    .toList();

            if (!remainingOptions.isEmpty()) {
                VariantType filteredType = new VariantType(type.getId(), type.getName());
                remainingOptions.forEach(filteredType::addOption);
                filtered.add(filteredType);
            }
        }

        return filtered;
    }

    private String buildOptionKey(Id typeId, Id optionId) {
        return typeId.getValue() + ":" + optionId.getValue();
    }
}
