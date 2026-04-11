package com.catalog.domain.service.impl;

import com.catalog.domain.aggregate.ProductVariant;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.VariantCombination;
import com.catalog.domain.service.MatrixCombinationSynchronizer;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DefaultMatrixCombinationSynchronizer implements MatrixCombinationSynchronizer {
    private static final Logger log = Loggers.getLogger(DefaultMatrixCombinationSynchronizer.class);

    private final MatrixKeyGenerator keyGenerator;

    @Override
    public List<VariantCombinationResult> syncMatrixCombination(List<ProductVariant> existingVariants, List<VariantCombination> combinations) {
        log.info(
                "Syncing variant combinations: existingVariants={}, requestedCombinations={}",
                existingVariants.size(),
                combinations.size()
        );

        if (combinations.isEmpty()) {
            log.debug("No combinations provided for sync");
            return Collections.emptyList();
        }

        Set<Id> commonTypeIds = extractCommonTypeIds(existingVariants, combinations);

        if (commonTypeIds.isEmpty()) {
            log.info("No common variation type ids found; marking all {} combinations as new", combinations.size());
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
            String key = generateSortedKey(combination.variations(), commonTypeIds);
            ProductVariant match = existingVariantByKey.get(key);
            VariantCombinationResult result;

            if (match != null && !usedKeys.contains(key) && match.getVariations().size() == combination.variations().size()) {
                result = new VariantCombinationResult(combination, match, VariantCombinationResult.MatchedType.UNCHANGED);
            } else if (match != null) {
                result = new VariantCombinationResult(combination, match, VariantCombinationResult.MatchedType.EXTENDED);
            } else {
                result = new VariantCombinationResult(combination, null, VariantCombinationResult.MatchedType.NEW);
            }
            results.add(result);
            usedKeys.add(key);
        }

        long unchangedCount = results.stream()
                .filter(result -> result.matchedType() == VariantCombinationResult.MatchedType.UNCHANGED)
                .count();
        long extendedCount = results.stream()
                .filter(result -> result.matchedType() == VariantCombinationResult.MatchedType.EXTENDED)
                .count();
        long newCount = results.stream()
                .filter(result -> result.matchedType() == VariantCombinationResult.MatchedType.NEW)
                .count();
        log.info(
                "Variant combination sync completed: unchanged={}, extended={}, new={}",
                unchangedCount,
                extendedCount,
                newCount
        );
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
            for (ProductVariation v : combo.variations()) {
                combinationTypeIds.add(v.getTypeId());
            }
        }
        Set<Id> commonTypeIds = new HashSet<>(existingTypeIds);
        commonTypeIds.retainAll(combinationTypeIds);
        log.debug(
                "Extracted common variation type ids: existingTypeCount={}, incomingTypeCount={}, commonTypeCount={}",
                existingTypeIds.size(),
                combinationTypeIds.size(),
                commonTypeIds.size()
        );
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
                .toList();
        log.debug("Generating sorted variation key from {} filtered variations", filtered.size());
        return keyGenerator.generateKey(filtered);
    }
}
