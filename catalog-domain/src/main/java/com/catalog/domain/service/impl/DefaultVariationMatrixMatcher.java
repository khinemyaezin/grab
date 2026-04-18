package com.catalog.domain.service.impl;

import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.VariationMatrixMatcher;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DefaultVariationMatrixMatcher implements VariationMatrixMatcher {
    private static final Logger log = Loggers.getLogger(DefaultVariationMatrixMatcher.class);

    private final MatrixKeyGenerator keyGenerator;

    @Override
    public <T> MatchingResult<T> match(
            List<VariantInput<T>> existingVariants,
            List<List<ProductVariation>> newCombinations) {

        log.info("Matching matrix: existingVariants={}, newCombinations={}",
                existingVariants.size(), newCombinations.size());

        if (newCombinations.isEmpty()) {
            return new MatchingResult<>(Collections.emptyList(), Collections.emptyList());
        }

        if (existingVariants.isEmpty()) {
            return allNew(newCombinations);
        }

        MatchContext<T> context = buildMatchContext(existingVariants, newCombinations);

        if (context.commonTypeIds.isEmpty()) {
            log.info("No common type IDs — marking all {} combinations as NEW", newCombinations.size());
            return allNew(newCombinations);
        }

        return matchCombinations(newCombinations, context);
    }

    private record MatchContext<T>(
            Set<Id> oldOptionIds,
            Set<Id> commonTypeIds,
            Map<String, List<VariantInput<T>>> inputsByProjection
    ) {}

    private <T> MatchContext<T> buildMatchContext(
            List<VariantInput<T>> existingVariants,
            List<List<ProductVariation>> newCombinations) {

        Set<Id> oldOptionIds = new HashSet<>();
        Set<Id> oldTypeIds = new HashSet<>();
        for (VariantInput<T> input : existingVariants) {
            for (ProductVariation variation : input.variations()) {
                oldOptionIds.add(variation.getOptionId());
                oldTypeIds.add(variation.getTypeId());
            }
        }

        Set<Id> newTypeIds = new HashSet<>();
        for (List<ProductVariation> combination : newCombinations) {
            for (ProductVariation variation : combination) {
                newTypeIds.add(variation.getTypeId());
            }
        }

        Set<Id> commonTypeIds = new HashSet<>(oldTypeIds);
        commonTypeIds.retainAll(newTypeIds);

        log.debug("Type analysis: oldTypes={}, newTypes={}, commonTypes={}",
                oldTypeIds.size(), newTypeIds.size(), commonTypeIds.size());

        Map<String, List<VariantInput<T>>> inputsByProjection = new LinkedHashMap<>();
        for (VariantInput<T> input : existingVariants) {
            String key = projectToCommonKey(input.variations(), commonTypeIds);
            inputsByProjection.computeIfAbsent(key, k -> new ArrayList<>()).add(input);
        }

        return new MatchContext<>(oldOptionIds, commonTypeIds, inputsByProjection);
    }

    private <T> MatchingResult<T> matchCombinations(
            List<List<ProductVariation>> newCombinations,
            MatchContext<T> context) {

        List<VariantMatch<T>> matches = new ArrayList<>();
        List<T> collapsed = new ArrayList<>();
        Set<String> usedKeys = new HashSet<>();

        for (List<ProductVariation> combination : newCombinations) {
            List<ProductVariation> commonProjection = filterByCommonTypes(combination, context.commonTypeIds());
            String projectionKey = keyGenerator.generateKey(commonProjection);

            boolean isNewOption = hasNewOption(commonProjection, context.oldOptionIds());
            boolean matchesExisting = context.inputsByProjection().containsKey(projectionKey);

            if (!isNewOption && !matchesExisting) {
                continue;
            }

            List<VariantInput<T>> existingMatches = context.inputsByProjection()
                    .getOrDefault(projectionKey, Collections.emptyList());

            MatchType type = classifyMatch(combination, existingMatches, usedKeys.contains(projectionKey));
            T matchedPayload = existingMatches.isEmpty() ? null : existingMatches.getFirst().payload();

            matches.add(new VariantMatch<>(combination, type, matchedPayload));
            usedKeys.add(projectionKey);

            if (existingMatches.size() > 1) {
                for (int i = 1; i < existingMatches.size(); i++) {
                    collapsed.add(existingMatches.get(i).payload());
                }
            }
        }

        log.info("Matching complete: matches={}, collapsed={}", matches.size(), collapsed.size());
        return new MatchingResult<>(matches, collapsed);
    }

    private <T> MatchType classifyMatch(
            List<ProductVariation> combination,
            List<VariantInput<T>> existingMatches,
            boolean keyAlreadyUsed) {

        if (existingMatches.isEmpty()) {
            return MatchType.NEW;
        }

        if (existingMatches.size() == 1 && !keyAlreadyUsed
                && hasExactMatch(existingMatches.getFirst().variations(), combination)) {
            return MatchType.UNCHANGED;
        }

        return MatchType.EXTENDED;
    }

    private <T> MatchingResult<T> allNew(List<List<ProductVariation>> combinations) {
        List<VariantMatch<T>> matches = combinations.stream()
                .map(c -> new VariantMatch<T>(c, MatchType.NEW, null))
                .toList();
        return new MatchingResult<>(matches, Collections.emptyList());
    }

    private String projectToCommonKey(List<ProductVariation> variations, Set<Id> commonTypeIds) {
        List<ProductVariation> filtered = variations.stream()
                .filter(v -> commonTypeIds.contains(v.getTypeId()))
                .toList();
        return keyGenerator.generateKey(filtered);
    }

    private List<ProductVariation> filterByCommonTypes(List<ProductVariation> variations, Set<Id> commonTypeIds) {
        return variations.stream()
                .filter(v -> commonTypeIds.contains(v.getTypeId()))
                .toList();
    }

    private boolean hasNewOption(List<ProductVariation> projection, Set<Id> oldOptionIds) {
        return projection.stream()
                .anyMatch(v -> !oldOptionIds.contains(v.getOptionId()));
    }

    private boolean hasExactMatch(List<ProductVariation> existingVariations, List<ProductVariation> combination) {
        if (existingVariations.size() != combination.size()) {
            return false;
        }
        return new HashSet<>(existingVariations).containsAll(combination);
    }
}
