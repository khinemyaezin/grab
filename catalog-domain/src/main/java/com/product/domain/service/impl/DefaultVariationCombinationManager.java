package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.service.VariantKeyGenerator;
import com.product.domain.service.VariationKeyGenerator;
import com.product.domain.valueobject.ProductVariation;
import com.product.domain.valueobject.VariantCombination;
import com.product.domain.service.VariationCombinationManager;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultVariationCombinationManager implements VariationCombinationManager {
    private static final Comparator<ProductVariation> VARIATION_COMPARATOR =
            Comparator.comparing(v -> v.getTypeId().getValue(), Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));

    private final VariationKeyGenerator keyGenerator;

    @Override
    public List<VariantCombinationResult> syncCombinations(List<ProductVariant> existingVariants, List<VariantCombination> combinations) {
        if (combinations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Id> commonTypeIds = extractCommonTypeIds(existingVariants, combinations);

        if (commonTypeIds.isEmpty()) {
            List<VariantCombinationResult> results = new ArrayList<>(combinations.size());
            for (VariantCombination c : combinations) {
                results.add(new VariantCombinationResult(c, null, VariantCombinationResult.MatchedType.NEW));
            }
            return results;
        }

        Map<String, ProductVariant> existingVariantByKey = buildExistingVariantMap(existingVariants, commonTypeIds);

        List<VariantCombinationResult> results = new ArrayList<>(combinations.size());
        Set<String> usedKeys = new HashSet<>(combinations.size());

        for (VariantCombination combination : combinations) {
            String key = generateSortedKey(combination.getVariations(), commonTypeIds);
            ProductVariant match = existingVariantByKey.get(key);
            VariantCombinationResult result;

            if (match != null && !usedKeys.contains(key) && match.getVariations().size() == combination.getVariations().size()) {
                result = new VariantCombinationResult(combination, match, VariantCombinationResult.MatchedType.UNCHANGED);
            } else if (match != null) {
                result = new VariantCombinationResult(combination, match, VariantCombinationResult.MatchedType.EXTENDED);
            } else {
                result = new VariantCombinationResult(combination, null, VariantCombinationResult.MatchedType.NEW);
            }
            results.add(result);
            usedKeys.add(key);
        }

        return results;
    }

    private Set<Id> extractCommonTypeIds(List<ProductVariant> existingVariants, List<VariantCombination> combinations){
        Set<Id> existingTypeIds = new HashSet<>();
        for (ProductVariant variant : existingVariants) {
            for (ProductVariation v : variant.getVariations()) {
                existingTypeIds.add(v.getTypeId());
            }
        }
        Set<Id> combinationTypeIds = new HashSet<>();
        for (VariantCombination combo : combinations) {
            for (ProductVariation v : combo.getVariations()) {
                combinationTypeIds.add(v.getTypeId());
            }
        }
        Set<Id> commonTypeIds = new HashSet<>(existingTypeIds);
        commonTypeIds.retainAll(combinationTypeIds);
        return commonTypeIds;
    }

    private Map<String, ProductVariant> buildExistingVariantMap(List<ProductVariant> existingVariants, Set<Id> commonTypeIds) {
        Map<String, ProductVariant> existingVariantByKey = new LinkedHashMap<>(existingVariants.size());
        for (ProductVariant variant : existingVariants) {
            String key = generateSortedKey(new ArrayList<>(variant.getVariations()), commonTypeIds);
            existingVariantByKey.putIfAbsent(key, variant);
        }
        return existingVariantByKey;
    }

    private String generateSortedKey(List<ProductVariation> variations, Set<Id> commonTypeIds) {
        List<ProductVariation> filtered = variations.stream()
                .filter(v -> commonTypeIds.contains(v.getTypeId()))
                .sorted(VARIATION_COMPARATOR)
                .toList();
        return keyGenerator.generateVariationKey(filtered);
    }
}
