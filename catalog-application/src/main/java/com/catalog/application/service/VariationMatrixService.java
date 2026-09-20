package com.catalog.application.service;

import com.catalog.application.port.inbound.VariationMatrixUseCase;

import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.VariationMatrixMatcher;
import com.catalog.domain.service.VariationMatrixMatcher.MatchingResult;
import com.catalog.domain.service.VariationMatrixMatcher.VariantInput;
import com.catalog.domain.service.VariationMatrixMatcher.VariantMatch;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.query.VariationMatrixQuery;
import com.catalog.application.query.VariationMatrixResult;
import lombok.RequiredArgsConstructor;

import java.util.*;

@lombok.RequiredArgsConstructor
public class VariationMatrixService implements VariationMatrixUseCase {
    private static final Logger log = Loggers.getLogger(VariationMatrixService.class);

    private final MatrixCombinationService matrixCombinationService;
    private final MatrixKeyGenerator matrixKeyGenerator;
    private final IdGenerator idGenerator;
    private final VariationMatrixMatcher matcher;

        public VariationMatrixResult execute(VariationMatrixQuery query) {
        log.debug("Handling VariationMatrixQuery");

        if (query.variantTypes() == null || query.variantTypes().isEmpty()) {
            return new VariationMatrixResult(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        List<List<ProductVariation>> combinations = generateCombinations(query.variantTypes());
        List<VariantInput<String>> existingInputs = toVariantInputs(query.variants());

        MatchingResult<String> matchResult = matcher.match(existingInputs, combinations);

        List<VariationMatrixResult.Variant> variants = matchResult.matches().stream()
                .map(this::toResultVariant)
                .toList();

        List<String> collapsedOriginal = matchResult.collapsed();

        return buildResult(query.variantTypes(), variants, collapsedOriginal);
    }

    private List<List<ProductVariation>> generateCombinations(List<VariationMatrixQuery.VariantType> variantTypes) {
        List<VariantTypeSelection> selections = variantTypes.stream()
                .map(vt -> new VariantTypeSelection(
                        idGenerator.convertIdFrom(vt.typeId()),
                        vt.options().stream()
                                .map(opt -> new VariantOptionSelection(
                                        idGenerator.convertIdFrom(opt.optionId()),
                                        idGenerator.convertIdFrom(vt.typeId())))
                                .toList()
                ))
                .toList();

        List<List<VariantOptionSelection>> rawCombinations =
                matrixCombinationService.generateMatrixCombination(selections);

        return rawCombinations.stream()
                .map(combo -> combo.stream()
                        .map(opt -> new ProductVariation(opt.valueId(), opt.typeId()))
                        .toList())
                .toList();
    }

    private List<VariantInput<String>> toVariantInputs(List<VariationMatrixQuery.Variant> variants) {
        if (variants == null || variants.isEmpty()) {
            return Collections.emptyList();
        }

        return variants.stream()
                .map(v -> new VariantInput<>(
                        v.variations().stream()
                                .map(var -> new ProductVariation(
                                        idGenerator.convertIdFrom(var.optionId()),
                                        idGenerator.convertIdFrom(var.typeId())))
                                .toList(),
                        v.matrixKey()
                ))
                .toList();
    }

    private VariationMatrixResult.Variant toResultVariant(VariantMatch<String> match) {
        String newKey = matrixKeyGenerator.generateKey(match.combination());
        return new VariationMatrixResult.Variant(
                matrixKeyGenerator.generateKey(match.combination()),
                match.matchedPayload() != null ? match.matchedPayload() : newKey,
                match.combination().stream()
                        .map(v -> new VariationMatrixResult.Variation(
                                v.getOptionId().getValue(),
                                v.getTypeId().getValue()
                        ))
                        .toList()
        );
    }

    private VariationMatrixResult buildResult(List<VariationMatrixQuery.VariantType> variantTypes,
                                              List<VariationMatrixResult.Variant> variants,
                                              List<String> collapsedSkus) {
        return new VariationMatrixResult(
                variants,
                collapsedSkus,
                variantTypes.stream()
                        .map(vt -> new VariationMatrixResult.VariantType(
                                vt.typeId(),
                                vt.options().stream()
                                        .map(opt -> new VariationMatrixResult.VariantOption(opt.optionId()))
                                        .toList()))
                        .toList()
        );
    }
}
