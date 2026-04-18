package com.catalog.domain.service.impl;

import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.VariationMatrixMatcher;
import com.catalog.domain.service.VariationMatrixMatcher.MatchingResult;
import com.catalog.domain.service.VariationMatrixMatcher.VariantInput;
import com.catalog.domain.service.VariationMatrixMatcher.VariantMatch;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.domain.valueobject.VariantCombination;
import com.catalog.domain.service.MatrixCombinationSynchronizer;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DefaultMatrixCombinationSynchronizer implements MatrixCombinationSynchronizer {
    private static final Logger log = Loggers.getLogger(DefaultMatrixCombinationSynchronizer.class);

    private final MatrixKeyGenerator keyGenerator;
    private final VariationMatrixMatcher matcher;

    @Override
    public List<VariantCombinationResult> syncMatrixCombination(
            List<ProductVariant> existingVariants,
            List<VariantCombination> combinations) {

        log.info("Syncing variant combinations: existingVariants={}, requestedCombinations={}",
                existingVariants.size(), combinations.size());

        if (combinations.isEmpty()) {
            log.debug("No combinations provided for sync");
            return Collections.emptyList();
        }

        List<VariantInput<ProductVariant>> inputs = existingVariants.stream()
                .map(v -> new VariantInput<>(new ArrayList<>(v.getVariations()), v))
                .toList();

        List<List<ProductVariation>> combos = combinations.stream()
                .map(VariantCombination::variations)
                .toList();

        MatchingResult<ProductVariant> result = matcher.match(inputs, combos);

        return result.matches().stream()
                .map(match -> toVariantCombinationResult(match, combinations))
                .toList();
    }

    private VariantCombinationResult toVariantCombinationResult(
            VariantMatch<ProductVariant> match,
            List<VariantCombination> originalCombinations) {

        VariantCombination variantCombination = originalCombinations.stream()
                .filter(c -> c.variations().equals(match.combination()))
                .findFirst()
                .orElse(new VariantCombination(match.combination()));

        List<ProductVariant> matchedVariants = match.matchedPayload() != null
                ? List.of(match.matchedPayload())
                : null;

        return new VariantCombinationResult(
                variantCombination,
                matchedVariants,
                toMatchedType(match.type())
        );
    }

    private VariantCombinationResult.MatchedType toMatchedType(VariationMatrixMatcher.MatchType type) {
        return switch (type) {
            case UNCHANGED -> VariantCombinationResult.MatchedType.UNCHANGED;
            case EXTENDED -> VariantCombinationResult.MatchedType.EXTENDED;
            case NEW -> VariantCombinationResult.MatchedType.NEW;
        };
    }
}
